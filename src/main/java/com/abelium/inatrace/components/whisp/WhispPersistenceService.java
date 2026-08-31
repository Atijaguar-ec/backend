package com.abelium.inatrace.components.whisp;

import com.abelium.inatrace.api.ApiStatus;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.common.BaseService;
import com.abelium.inatrace.components.whisp.api.ApiWhispPlotAnalysis;
import com.abelium.inatrace.components.whisp.mappers.WhispAnalysisMapper;
import com.abelium.inatrace.db.entities.common.Plot;
import com.abelium.inatrace.db.entities.common.PlotDeforestationAnalysis;
import com.abelium.inatrace.security.service.CustomUserDetails;
import com.abelium.inatrace.security.utils.PermissionsUtil;
import com.abelium.inatrace.tools.PlotGeometryTools;
import com.abelium.inatrace.types.DeforestationAnalysisStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Database side of the Whisp integration.
 *
 * Deliberately a separate bean from {@link WhispAnalysisService}: Spring's transaction
 * advice is a proxy, so a {@code @Transactional} method called from another method of the
 * same bean runs with no transaction at all. Keeping the two apart is also what lets the
 * orchestration call Whisp between two short transactions instead of holding a database
 * connection open for the whole round trip.
 */
@Service
public class WhispPersistenceService extends BaseService {

	private final ObjectMapper objectMapper;

	public WhispPersistenceService(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	/**
	 * Reads what Whisp needs about a plot, after checking the user may see it.
	 */
	@Transactional(readOnly = true)
	public PlotAnalysisTarget loadTarget(Long plotId, CustomUserDetails user) throws ApiException {

		Plot plot = fetchPlot(plotId, user);

		return new PlotAnalysisTarget(
				plot.getId(),
				plot.getPlotName(),
				StringUtils.trimToNull(plot.getGeoId()),
				PlotGeometryTools.toPolygonWkt(plot.getCoordinates()));
	}

	/**
	 * The most recent analysis of a plot, or {@code null} when it has never been analysed.
	 */
	@Transactional(readOnly = true)
	public ApiWhispPlotAnalysis findLatest(Long plotId, CustomUserDetails user) throws ApiException {

		fetchPlot(plotId, user);

		return WhispAnalysisMapper.toApiWhispPlotAnalysis(fetchLatestEntity(plotId), objectMapper);
	}

	/**
	 * Stores a freshly submitted job.
	 */
	@Transactional
	public ApiWhispPlotAnalysis createFromJob(Long plotId, WhispJob job, String submissionMode, String geoId) throws ApiException {

		Plot plot = em.find(Plot.class, plotId);
		if (plot == null) {
			throw new ApiException(ApiStatus.INVALID_REQUEST, "Invalid Plot ID");
		}

		PlotDeforestationAnalysis analysis = new PlotDeforestationAnalysis();
		analysis.setPlot(plot);
		analysis.setRequestedAt(new Date());
		analysis.setSubmissionMode(submissionMode);
		analysis.setGeoId(geoId);
		analysis.setStatus(DeforestationAnalysisStatus.PENDING);

		WhispAnalysisMapper.applyJob(analysis, job);

		em.persist(analysis);

		return WhispAnalysisMapper.toApiWhispPlotAnalysis(analysis, objectMapper);
	}

	/**
	 * Writes the latest state of a job that was already stored.
	 */
	@Transactional
	public ApiWhispPlotAnalysis updateFromJob(Long analysisId, WhispJob job) throws ApiException {

		PlotDeforestationAnalysis analysis = em.find(PlotDeforestationAnalysis.class, analysisId);
		if (analysis == null) {
			throw new ApiException(ApiStatus.INVALID_REQUEST, "Invalid analysis ID");
		}

		WhispAnalysisMapper.applyJob(analysis, job);

		return WhispAnalysisMapper.toApiWhispPlotAnalysis(analysis, objectMapper);
	}

	private PlotDeforestationAnalysis fetchLatestEntity(Long plotId) {

		return em.createQuery(
						"SELECT a FROM PlotDeforestationAnalysis a WHERE a.plot.id = :plotId ORDER BY a.id DESC",
						PlotDeforestationAnalysis.class)
				.setParameter("plotId", plotId)
				.setMaxResults(1)
				.getResultStream()
				.findFirst()
				.orElse(null);
	}

	/**
	 * A plot is visible to the users of the company its farmer belongs to - the same rule
	 * the plot endpoints of {@code CompanyService} apply.
	 */
	private Plot fetchPlot(Long plotId, CustomUserDetails user) throws ApiException {

		Plot plot = em.find(Plot.class, plotId);
		if (plot == null) {
			throw new ApiException(ApiStatus.INVALID_REQUEST, "Invalid Plot ID");
		}
		if (plot.getFarmer() == null || plot.getFarmer().getCompany() == null) {
			throw new ApiException(ApiStatus.INVALID_REQUEST, "The plot is not attached to a company");
		}

		PermissionsUtil.checkUserIfCompanyEnrolled(
				plot.getFarmer().getCompany().getUsers().stream().toList(), user);

		return plot;
	}

}
