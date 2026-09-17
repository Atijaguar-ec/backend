package com.abelium.inatrace.components.company.plotimport;

import com.abelium.inatrace.components.company.types.PlotGeoJsonImportIssueType;
import com.abelium.inatrace.components.company.types.PlotGeoJsonImportScope;
import com.abelium.inatrace.types.CocoaVariety;
import com.mapbox.geojson.Point;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Decides, without touching the database, what a bulk GeoJSON import would do: which
 * plot each feature becomes, which features cannot be imported and why, and which
 * companies get their plots replaced.
 *
 * <p>Feature properties (the layout of the GIS layers delivered by UNOCACE):
 * <ul>
 *     <li>{@value #FARMER_ID_PROPERTY}: the farmer's internal id
 *     ({@code UserCustomer.farmerCompanyInternalId}), compared without case or
 *     surrounding blanks. Required.</li>
 *     <li>{@value #PLOT_NAME_PROPERTY}: plot code, stored as the plot name. Required and
 *     unique per farmer.</li>
 *     <li>{@value #SIZE_PROPERTY}: surveyed size in hectares. When missing, the
 *     geodesic area of the polygon is used.</li>
 *     <li>{@value #CROP_PROPERTY}: crop, matched by name against the product types
 *     ("CACAO CCN51", "CACAO NACIONAL"). It also sets the cocoa variety. When it
 *     matches no product type, the farmer's own product type is used.</li>
 * </ul>
 */
public final class PlotGeoJsonImportPlanner {

	public static final String FARMER_ID_PROPERTY = "ID_INTERNO";
	public static final String PLOT_NAME_PROPERTY = "COD_LOTE";
	public static final String SIZE_PROPERTY = "HECTAREA";
	public static final String CROP_PROPERTY = "CULTIVOPRI";

	public static final String HECTARE_UNIT = "ha";

	private PlotGeoJsonImportPlanner() {
	}

	/**
	 * A farmer of the import scope.
	 *
	 * @param defaultProductTypeId the farmer's product type with the lowest id, or
	 *                             {@code null} when none is assigned
	 */
	public record Farmer(Long id, Long companyId, String internalId, String name, Long defaultProductTypeId) {
	}

	public record ProductType(Long id, String name) {
	}

	public record Company(Long id, String name, long existingPlots) {
	}

	public record PlannedPlot(int featureNumber, Long farmerId, Long companyId, String plotName, Double size,
	                          Long cropId, CocoaVariety cocoaVariety, List<Point> coordinates) {
	}

	public record Issue(int featureNumber, String farmerInternalId, String plotName,
	                    PlotGeoJsonImportIssueType type) {
	}

	/**
	 * @param companiesToReplace ids of the companies whose existing plots are deleted
	 * @param farmersWithoutPlots farmers of companies that receive plots from the file
	 *                            but get none themselves: after the import they have no
	 *                            plot at all
	 */
	public record Plan(int featuresRead, List<PlannedPlot> plots, List<Issue> issues,
	                   List<Company> companies, Set<Long> companiesToReplace, List<Farmer> farmersWithoutPlots) {

		public long plotsToDelete() {
			return companies.stream()
					.filter(c -> companiesToReplace.contains(c.id()))
					.mapToLong(Company::existingPlots)
					.sum();
		}
	}

	public static Plan plan(List<PlotGeoJsonFeature> features,
	                        List<Company> scopeCompanies,
	                        List<Farmer> scopeFarmers,
	                        List<ProductType> productTypes,
	                        PlotGeoJsonImportScope scope) {

		Map<String, List<Farmer>> farmersByInternalId = scopeFarmers.stream()
				.filter(f -> normalizeId(f.internalId()) != null)
				.collect(Collectors.groupingBy(f -> normalizeId(f.internalId())));

		Map<String, Long> productTypeIdByName = new HashMap<>();
		productTypes.stream()
				.filter(p -> normalizeName(p.name()) != null)
				.sorted(Comparator.comparing(ProductType::id))
				.forEach(p -> productTypeIdByName.putIfAbsent(normalizeName(p.name()), p.id()));

		List<Issue> issues = new ArrayList<>();
		List<PlannedPlot> candidates = new ArrayList<>();

		for (PlotGeoJsonFeature feature : features) {

			String internalId = feature.text(FARMER_ID_PROPERTY);
			String plotName = feature.text(PLOT_NAME_PROPERTY);

			PlotGeoJsonImportIssueType issue = feature.geometryIssue();
			Farmer farmer = null;
			Long cropId = null;

			if (issue == null && internalId == null) {
				issue = PlotGeoJsonImportIssueType.MISSING_FARMER_ID;
			}
			if (issue == null && plotName == null) {
				issue = PlotGeoJsonImportIssueType.MISSING_PLOT_NAME;
			}
			if (issue == null) {
				List<Farmer> matches = farmersByInternalId.getOrDefault(normalizeId(internalId), List.of());
				if (matches.isEmpty()) {
					issue = PlotGeoJsonImportIssueType.FARMER_NOT_FOUND;
				} else if (matches.size() > 1) {
					issue = PlotGeoJsonImportIssueType.FARMER_AMBIGUOUS;
				} else {
					farmer = matches.get(0);
				}
			}
			if (issue == null) {
				String crop = normalizeName(feature.text(CROP_PROPERTY));
				cropId = crop != null && productTypeIdByName.containsKey(crop)
						? productTypeIdByName.get(crop)
						: farmer.defaultProductTypeId();
				if (cropId == null) {
					issue = PlotGeoJsonImportIssueType.CROP_NOT_FOUND;
				}
			}

			if (issue != null) {
				issues.add(new Issue(feature.number(), internalId, plotName, issue));
				continue;
			}

			Double size = feature.number(SIZE_PROPERTY);
			if (size == null || size <= 0) {
				size = feature.areaHectares();
			}

			candidates.add(new PlannedPlot(feature.number(), farmer.id(), farmer.companyId(), plotName, size,
					cropId, cocoaVariety(feature.text(CROP_PROPERTY)), feature.ring()));
		}

		List<PlannedPlot> plots = withoutDuplicatePlotNames(candidates, farmersByIdOf(scopeFarmers), issues);
		issues.sort(Comparator.comparingInt(Issue::featureNumber));

		Set<Long> receivingCompanies = plots.stream()
				.map(PlannedPlot::companyId)
				.collect(Collectors.toCollection(LinkedHashSet::new));

		Set<Long> companiesToReplace = scope == PlotGeoJsonImportScope.COMPANY_AND_CONNECTED
				? scopeCompanies.stream().map(Company::id).collect(Collectors.toCollection(LinkedHashSet::new))
				: receivingCompanies;

		Set<Long> farmersWithPlots = plots.stream().map(PlannedPlot::farmerId).collect(Collectors.toSet());
		List<Farmer> farmersWithoutPlots = scopeFarmers.stream()
				.filter(f -> receivingCompanies.contains(f.companyId()))
				.filter(f -> !farmersWithPlots.contains(f.id()))
				.sorted(Comparator.comparing(Farmer::companyId).thenComparing(Farmer::id))
				.toList();

		return new Plan(features.size(), plots, List.copyOf(issues), List.copyOf(scopeCompanies),
				companiesToReplace, farmersWithoutPlots);
	}

	/**
	 * A farmer cannot have two plots with the same code: every feature involved is
	 * reported, since there is no way to tell which one is right.
	 */
	private static List<PlannedPlot> withoutDuplicatePlotNames(List<PlannedPlot> candidates,
	                                                           Map<Long, Farmer> farmersById,
	                                                           List<Issue> issues) {

		Map<String, Long> occurrences = candidates.stream()
				.collect(Collectors.groupingBy(PlotGeoJsonImportPlanner::plotKey, Collectors.counting()));

		List<PlannedPlot> plots = new ArrayList<>();
		for (PlannedPlot candidate : candidates) {
			if (occurrences.get(plotKey(candidate)) > 1) {
				issues.add(new Issue(candidate.featureNumber(), farmersById.get(candidate.farmerId()).internalId(),
						candidate.plotName(), PlotGeoJsonImportIssueType.DUPLICATE_PLOT_NAME));
			} else {
				plots.add(candidate);
			}
		}
		return plots;
	}

	private static Map<Long, Farmer> farmersByIdOf(List<Farmer> farmers) {
		return farmers.stream().collect(Collectors.toMap(Farmer::id, f -> f, (a, b) -> a));
	}

	private static String plotKey(PlannedPlot plot) {
		return plot.farmerId() + "|" + normalizeName(plot.plotName());
	}

	/**
	 * Variety 1 is stored as {@code ORGANICO} and shown as "Nacional" to UNOCACE; see
	 * {@link CocoaVariety} and agent-context §18.
	 */
	static CocoaVariety cocoaVariety(String crop) {
		String normalized = normalizeName(crop);
		if (normalized == null) {
			return null;
		}
		if (normalized.matches(".*\\bCCN?-? ?51\\b.*")) {
			return CocoaVariety.CCN51;
		}
		if (normalized.contains("NACIONAL")) {
			return CocoaVariety.ORGANICO;
		}
		return null;
	}

	static String normalizeId(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim().toUpperCase(Locale.ROOT);
	}

	static String normalizeName(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		String withoutMarks = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
		return withoutMarks.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
	}

}
