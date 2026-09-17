package com.abelium.inatrace.components.company.plotimport;

import com.abelium.inatrace.api.ApiStatus;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.agstack.PlotGeoIdGenerator;
import com.abelium.inatrace.components.common.BaseService;
import com.abelium.inatrace.components.company.CompanyQueries;
import com.abelium.inatrace.components.company.api.ApiPlotGeoJsonImportCompany;
import com.abelium.inatrace.components.company.api.ApiPlotGeoJsonImportFarmer;
import com.abelium.inatrace.components.company.api.ApiPlotGeoJsonImportIssue;
import com.abelium.inatrace.components.company.api.ApiPlotGeoJsonImportResponse;
import com.abelium.inatrace.components.company.plotimport.PlotGeoJsonImportPlanner.Plan;
import com.abelium.inatrace.components.company.plotimport.PlotGeoJsonImportPlanner.PlannedPlot;
import com.abelium.inatrace.components.company.types.PlotGeoJsonImportScope;
import com.abelium.inatrace.db.entities.codebook.ProductType;
import com.abelium.inatrace.db.entities.common.Plot;
import com.abelium.inatrace.db.entities.common.PlotCoordinate;
import com.abelium.inatrace.db.entities.common.UserCustomer;
import com.abelium.inatrace.security.service.CustomUserDetails;
import com.abelium.inatrace.types.UserCustomerType;
import com.mapbox.geojson.Point;
import jakarta.annotation.PreDestroy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Bulk replacement of farmer plots from a GeoJSON file (a validated GIS survey).
 *
 * <p>The same method previews and applies: the preview is the plan, and applying
 * executes that plan in one transaction, so either every change is saved or none is.
 * Applying requires the numbers the user saw in the preview; if the data changed in
 * between, nothing is applied.
 *
 * <p>AgStack geo ids are generated after the commit, one plot at a time on a single
 * background thread: registering a hundred plots takes minutes, and an HTTP call must
 * not hold the transaction open (agent-context §14.5).
 *
 * <p>Rules and file layout: {@link PlotGeoJsonImportPlanner}; operation notes:
 * agent-context §19.
 */
@Service
public class PlotGeoJsonImportService extends BaseService {

	private final CompanyQueries companyQueries;

	private final PlotGeoIdGenerator plotGeoIdGenerator;

	private final TransactionTemplate transactionTemplate;

	private final ExecutorService geoIdExecutor = Executors.newSingleThreadExecutor(runnable -> {
		Thread thread = new Thread(runnable, "plot-geoid-backfill");
		thread.setDaemon(true);
		return thread;
	});

	public PlotGeoJsonImportService(CompanyQueries companyQueries,
	                                PlotGeoIdGenerator plotGeoIdGenerator,
	                                PlatformTransactionManager transactionManager) {
		this.companyQueries = companyQueries;
		this.plotGeoIdGenerator = plotGeoIdGenerator;
		this.transactionTemplate = new TransactionTemplate(transactionManager);
	}

	@PreDestroy
	void shutdown() {
		geoIdExecutor.shutdownNow();
	}

	/**
	 * @param expectedPlotsToDelete required when applying: the preview's {@code plotsToDelete}
	 * @param expectedPlotsToCreate required when applying: the preview's {@code plotsToCreate}
	 */
	@Transactional(rollbackFor = Exception.class)
	public ApiPlotGeoJsonImportResponse importPlots(CustomUserDetails authUser,
	                                                Long companyId,
	                                                byte[] content,
	                                                PlotGeoJsonImportScope scope,
	                                                boolean apply,
	                                                boolean skipInvalidFeatures,
	                                                Long expectedPlotsToDelete,
	                                                Integer expectedPlotsToCreate) throws ApiException {

		if (scope == null) {
			throw new ApiException(ApiStatus.INVALID_REQUEST, "The import scope is required");
		}

		List<PlotGeoJsonFeature> features = PlotGeoJsonReader.read(content);
		companyQueries.fetchCompany(companyId);

		List<Long> scopeCompanyIds = new ArrayList<>();
		scopeCompanyIds.add(companyId);
		scopeCompanyIds.addAll(fetchConnectedCompanyIds(companyId));

		Plan plan = PlotGeoJsonImportPlanner.plan(
				features,
				fetchCompanies(scopeCompanyIds),
				fetchFarmers(scopeCompanyIds),
				fetchProductTypes(),
				scope);

		int geoIdsPending = 0;
		if (apply) {
			checkApplicable(plan, skipInvalidFeatures, expectedPlotsToDelete, expectedPlotsToCreate);
			geoIdsPending = apply(plan);
			logger.info("Plot GeoJSON import by user {} on company {} ({}): deleted {} plots in companies {}, created {}, skipped {} features",
					authUser.getUserId(), companyId, scope, plan.plotsToDelete(), plan.companiesToReplace(),
					plan.plots().size(), plan.issues().size());
		}

		return toApiResponse(plan, scope, apply, geoIdsPending);
	}

	private void checkApplicable(Plan plan,
	                             boolean skipInvalidFeatures,
	                             Long expectedPlotsToDelete,
	                             Integer expectedPlotsToCreate) throws ApiException {

		if (plan.plots().isEmpty()) {
			throw new ApiException(ApiStatus.VALIDATION_ERROR, "The file has no feature that can be imported");
		}
		if (!plan.issues().isEmpty() && !skipInvalidFeatures) {
			throw new ApiException(ApiStatus.VALIDATION_ERROR,
					plan.issues().size() + " features cannot be imported; fix them or skip them explicitly");
		}
		if (expectedPlotsToDelete == null || expectedPlotsToCreate == null
				|| expectedPlotsToDelete != plan.plotsToDelete()
				|| expectedPlotsToCreate != plan.plots().size()) {
			throw new ApiException(ApiStatus.VALIDATION_ERROR,
					"The data changed since the preview (now " + plan.plotsToDelete() + " plots to delete and "
							+ plan.plots().size() + " to create); preview the file again");
		}
	}

	/**
	 * @return how many plots wait for a geo id
	 */
	private int apply(Plan plan) {

		if (!plan.companiesToReplace().isEmpty()) {
			deletePlotsOfCompanies(plan.companiesToReplace());
		}

		Map<Long, List<PlotCoordinate>> polygonsByPlotId = new LinkedHashMap<>();
		Date now = new Date();

		for (PlannedPlot planned : plan.plots()) {

			Plot plot = new Plot();
			plot.setFarmer(em.getReference(UserCustomer.class, planned.farmerId()));
			plot.setPlotName(planned.plotName());
			plot.setSize(planned.size());
			plot.setUnit(planned.size() != null ? PlotGeoJsonImportPlanner.HECTARE_UNIT : null);
			plot.setCrop(em.getReference(ProductType.class, planned.cropId()));
			plot.setCocoaVariety(planned.cocoaVariety());
			plot.setLastUpdated(now);

			for (Point point : planned.coordinates()) {
				PlotCoordinate coordinate = new PlotCoordinate();
				coordinate.setLatitude(point.latitude());
				coordinate.setLongitude(point.longitude());
				coordinate.setPlot(plot);
				plot.getCoordinates().add(coordinate);
			}

			em.persist(plot);

			if (planned.coordinates().size() > 1) {
				polygonsByPlotId.put(plot.getId(), plot.getCoordinates().stream().map(c -> {
					PlotCoordinate copy = new PlotCoordinate();
					copy.setLatitude(c.getLatitude());
					copy.setLongitude(c.getLongitude());
					return copy;
				}).toList());
			}
		}

		em.flush();

		if (!plotGeoIdGenerator.isEnabled() || polygonsByPlotId.isEmpty()) {
			return 0;
		}

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				geoIdExecutor.execute(() -> generateGeoIds(polygonsByPlotId));
			}
		});
		return polygonsByPlotId.size();
	}

	/**
	 * Bulk deletes: the analyses and coordinates first, because neither is removed by a
	 * JPQL delete of the plot (cascades only apply to entity removal).
	 */
	private void deletePlotsOfCompanies(Iterable<Long> companyIds) {

		List<Long> ids = new ArrayList<>();
		companyIds.forEach(ids::add);

		String plotsOfCompanies = "SELECT p.id FROM Plot p WHERE p.farmer.company.id IN :companyIds";

		em.createQuery("DELETE FROM PlotDeforestationAnalysis a WHERE a.plot.id IN (" + plotsOfCompanies + ")")
				.setParameter("companyIds", ids)
				.executeUpdate();
		em.createQuery("DELETE FROM PlotCoordinate c WHERE c.plot.id IN (" + plotsOfCompanies + ")")
				.setParameter("companyIds", ids)
				.executeUpdate();
		em.createQuery("DELETE FROM Plot p WHERE p.farmer.id IN "
						+ "(SELECT uc.id FROM UserCustomer uc WHERE uc.company.id IN :companyIds)")
				.setParameter("companyIds", ids)
				.executeUpdate();

		// Farmers loaded earlier in this transaction still hold the deleted plots.
		em.clear();
	}

	private void generateGeoIds(Map<Long, List<PlotCoordinate>> polygonsByPlotId) {

		int generated = 0;
		for (Map.Entry<Long, List<PlotCoordinate>> entry : polygonsByPlotId.entrySet()) {
			String geoId = plotGeoIdGenerator.generate(entry.getValue());
			if (StringUtils.isBlank(geoId)) {
				continue;
			}
			Boolean saved = transactionTemplate.execute(status -> {
				Plot plot = em.find(Plot.class, entry.getKey());
				// The plot may have been deleted or given a geo id from the map meanwhile.
				if (plot == null || StringUtils.isNotBlank(plot.getGeoId())) {
					return false;
				}
				plot.setGeoId(geoId);
				return true;
			});
			if (Boolean.TRUE.equals(saved)) {
				generated++;
			}
		}
		logger.info("Plot GeoJSON import: generated {} of {} AgStack geo ids", generated, polygonsByPlotId.size());
	}

	private List<Long> fetchConnectedCompanyIds(Long companyId) {
		return em.createQuery(
						"SELECT DISTINCT other.company.id FROM ProductCompany other "
								+ "WHERE other.company.id <> :companyId AND other.product.id IN "
								+ "(SELECT own.product.id FROM ProductCompany own WHERE own.company.id = :companyId) "
								+ "ORDER BY other.company.id",
						Long.class)
				.setParameter("companyId", companyId)
				.getResultList();
	}

	private List<PlotGeoJsonImportPlanner.Company> fetchCompanies(List<Long> companyIds) {

		Map<Long, Long> plotsByCompany = new HashMap<>();
		em.createQuery(
						"SELECT p.farmer.company.id, COUNT(p) FROM Plot p "
								+ "WHERE p.farmer.company.id IN :companyIds GROUP BY p.farmer.company.id",
						Object[].class)
				.setParameter("companyIds", companyIds)
				.getResultList()
				.forEach(row -> plotsByCompany.put((Long) row[0], (Long) row[1]));

		Map<Long, String> namesById = em.createQuery(
						"SELECT c.id, c.name FROM Company c WHERE c.id IN :companyIds", Object[].class)
				.setParameter("companyIds", companyIds)
				.getResultList()
				.stream()
				.collect(Collectors.toMap(row -> (Long) row[0], row -> (String) row[1]));

		// Keep the selected company first, then the connected ones by id.
		return companyIds.stream()
				.map(id -> new PlotGeoJsonImportPlanner.Company(id, namesById.get(id), plotsByCompany.getOrDefault(id, 0L)))
				.toList();
	}

	private List<PlotGeoJsonImportPlanner.Farmer> fetchFarmers(List<Long> companyIds) {

		Map<Long, Long> productTypeByFarmer = new HashMap<>();
		em.createQuery(
						"SELECT t.userCustomer.id, MIN(t.productType.id) FROM UserCustomerProductType t "
								+ "WHERE t.userCustomer.company.id IN :companyIds GROUP BY t.userCustomer.id",
						Object[].class)
				.setParameter("companyIds", companyIds)
				.getResultList()
				.forEach(row -> productTypeByFarmer.put((Long) row[0], (Long) row[1]));

		return em.createQuery(
						"SELECT uc.id, uc.company.id, uc.farmerCompanyInternalId, uc.name, uc.surname "
								+ "FROM UserCustomer uc WHERE uc.company.id IN :companyIds AND uc.type = :type",
						Object[].class)
				.setParameter("companyIds", companyIds)
				.setParameter("type", UserCustomerType.FARMER)
				.getResultList()
				.stream()
				.map(row -> new PlotGeoJsonImportPlanner.Farmer(
						(Long) row[0],
						(Long) row[1],
						(String) row[2],
						StringUtils.joinWith(" ", StringUtils.defaultString((String) row[3]),
								StringUtils.defaultString((String) row[4])).trim(),
						productTypeByFarmer.get((Long) row[0])))
				.toList();
	}

	private List<PlotGeoJsonImportPlanner.ProductType> fetchProductTypes() {
		return em.createQuery("SELECT p.id, p.name FROM ProductType p", Object[].class)
				.getResultList()
				.stream()
				.map(row -> new PlotGeoJsonImportPlanner.ProductType((Long) row[0], (String) row[1]))
				.toList();
	}

	private static ApiPlotGeoJsonImportResponse toApiResponse(Plan plan,
	                                                          PlotGeoJsonImportScope scope,
	                                                          boolean applied,
	                                                          int geoIdsPending) {

		Map<Long, Long> newPlotsByCompany = plan.plots().stream()
				.collect(Collectors.groupingBy(PlannedPlot::companyId, Collectors.counting()));
		Map<Long, String> companyNames = plan.companies().stream()
				.collect(Collectors.toMap(PlotGeoJsonImportPlanner.Company::id,
						c -> StringUtils.defaultString(c.name())));

		ApiPlotGeoJsonImportResponse response = new ApiPlotGeoJsonImportResponse();
		response.setApplied(applied);
		response.setScope(scope);
		response.setFeaturesRead(plan.featuresRead());
		response.setPlotsToCreate(plan.plots().size());
		response.setPlotsToDelete(plan.plotsToDelete());
		response.setFarmersWithNewPlots((int) plan.plots().stream().map(PlannedPlot::farmerId).distinct().count());
		response.setGeoIdsPending(geoIdsPending);

		response.setCompanies(plan.companies().stream().map(company -> {
			ApiPlotGeoJsonImportCompany apiCompany = new ApiPlotGeoJsonImportCompany();
			apiCompany.setId(company.id());
			apiCompany.setName(company.name());
			apiCompany.setExistingPlots(company.existingPlots());
			apiCompany.setReplaced(plan.companiesToReplace().contains(company.id()));
			apiCompany.setNewPlots(newPlotsByCompany.getOrDefault(company.id(), 0L).intValue());
			return apiCompany;
		}).toList());

		response.setIssues(plan.issues().stream().map(issue -> {
			ApiPlotGeoJsonImportIssue apiIssue = new ApiPlotGeoJsonImportIssue();
			apiIssue.setFeatureNumber(issue.featureNumber());
			apiIssue.setFarmerInternalId(issue.farmerInternalId());
			apiIssue.setPlotName(issue.plotName());
			apiIssue.setType(issue.type());
			return apiIssue;
		}).toList());

		response.setFarmersWithoutPlots(plan.farmersWithoutPlots().stream().map(farmer -> {
			ApiPlotGeoJsonImportFarmer apiFarmer = new ApiPlotGeoJsonImportFarmer();
			apiFarmer.setId(farmer.id());
			apiFarmer.setCompanyId(farmer.companyId());
			apiFarmer.setCompanyName(companyNames.get(farmer.companyId()));
			apiFarmer.setInternalId(farmer.internalId());
			apiFarmer.setName(farmer.name());
			return apiFarmer;
		}).toList());

		return response;
	}

}
