package com.abelium.inatrace.components.whisp;

import com.abelium.inatrace.api.ApiStatus;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.whisp.api.ApiWhispIntegrationStatus;
import com.abelium.inatrace.components.whisp.api.ApiWhispPlotAnalysis;
import com.abelium.inatrace.security.service.CustomUserDetails;
import com.abelium.inatrace.types.DeforestationAnalysisStatus;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Runs a plot's deforestation analysis through Whisp and keeps the result.
 *
 * The two calls to Whisp (submit, poll) happen between short transactions, never inside
 * one: an Earth Engine analysis takes minutes, and holding a database connection for it
 * is what exhausts the pool when several plots are analysed at once.
 */
@Service
public class WhispAnalysisService {

	private static final String MODE_GEO_ID = "GEO_ID";

	private static final String MODE_WKT = "WKT";

	private final Logger logger = LoggerFactory.getLogger(WhispAnalysisService.class);

	private final WhispClientService whispClientService;

	private final WhispPersistenceService whispPersistenceService;

	public WhispAnalysisService(WhispClientService whispClientService,
	                            WhispPersistenceService whispPersistenceService) {
		this.whispClientService = whispClientService;
		this.whispPersistenceService = whispPersistenceService;
	}

	/**
	 * Submits a plot for analysis and stores the resulting job.
	 *
	 * Preference is the AgStack geo id, which is the identifier the EUDR paperwork refers
	 * to. When the plot has none, or AgStack is not configured, the polygon is sent
	 * instead so the feature still works on its own.
	 */
	public ApiWhispPlotAnalysis requestAnalysis(Long plotId, CustomUserDetails user) throws ApiException {

		PlotAnalysisTarget target = whispPersistenceService.loadTarget(plotId, user);

		boolean canUseGeoId = StringUtils.isNotBlank(target.getGeoId()) && whispClientService.isGeoIdSubmissionAvailable();

		if (!canUseGeoId && target.getWkt() == null) {
			throw new ApiException(ApiStatus.INVALID_REQUEST,
					"The plot cannot be analysed: it has neither a geo id nor a polygon of at least three valid coordinates");
		}

		WhispJob job;
		String submissionMode;
		String usedGeoId = null;

		if (canUseGeoId) {
			try {
				job = whispClientService.submitGeoIds(List.of(target.getGeoId()));
				submissionMode = MODE_GEO_ID;
				usedGeoId = target.getGeoId();
			} catch (ApiException e) {
				if (target.getWkt() == null) {
					throw e;
				}
				// A geo id registered against an older AgStack instance, or one Whisp
				// cannot resolve, must not leave the plot unanalysable: the geometry is
				// the same either way.
				logger.warn("Whisp rejected geo id {} for plot {} ({}); retrying with the polygon",
						target.getGeoId(), plotId, e.getMessage());
				job = whispClientService.submitWkt(target.getWkt());
				submissionMode = MODE_WKT;
			}
		} else {
			job = whispClientService.submitWkt(target.getWkt());
			submissionMode = MODE_WKT;
		}

		return whispPersistenceService.createFromJob(plotId, job, submissionMode, usedGeoId);
	}

	/**
	 * The stored analysis of a plot, refreshed from Whisp when the job is still running.
	 *
	 * @return {@code null} when the plot has never been analysed
	 */
	public ApiWhispPlotAnalysis getAnalysis(Long plotId, CustomUserDetails user) throws ApiException {

		ApiWhispPlotAnalysis latest = whispPersistenceService.findLatest(plotId, user);

		if (latest == null || !isRunning(latest.getStatus()) || StringUtils.isBlank(latest.getWhispToken())) {
			return latest;
		}

		try {
			WhispJob job = whispClientService.fetchJob(latest.getWhispToken());
			return whispPersistenceService.updateFromJob(latest.getId(), job);
		} catch (ApiException e) {
			// A Whisp outage is not a failed analysis: the job may well finish. The
			// stored state is left untouched and the reason travels back to the caller
			// on this response only.
			logger.warn("Could not refresh Whisp job {} for plot {}: {}",
					latest.getWhispToken(), plotId, e.getMessage());
			latest.setErrorMessage(e.getMessage());
			return latest;
		}
	}

	/**
	 * The datasets and result fields Whisp can report - the analysis schemas a client
	 * needs to label what it renders.
	 */
	public JsonNode getAnalysisSchemas() throws ApiException {
		return whispClientService.fetchAnalysisSchemas();
	}

	/**
	 * Configuration and reachability of the integration.
	 */
	public ApiWhispIntegrationStatus getIntegrationStatus() {

		ApiWhispIntegrationStatus status = new ApiWhispIntegrationStatus();
		status.setEnabled(whispClientService.isEnabled());
		status.setBaseURL(whispClientService.getBaseURL());
		status.setGeoIdSubmissionAvailable(whispClientService.isGeoIdSubmissionAvailable());

		if (!status.isEnabled()) {
			status.setMessage("No Whisp API key configured (INATrace.whisp.apiKey)");
			return status;
		}

		try {
			// The dataset catalogue is public, so this checks the network path without
			// queueing an analysis.
			whispClientService.fetchAnalysisSchemas();
			status.setReachable(true);
		} catch (ApiException e) {
			status.setMessage(e.getMessage());
		}

		if (!status.isGeoIdSubmissionAvailable()) {
			status.setMessage(StringUtils.defaultIfBlank(status.getMessage(),
					"AgStack is not configured; plots are submitted to Whisp as polygons instead of geo ids"));
		}

		return status;
	}

	private boolean isRunning(DeforestationAnalysisStatus status) {
		return status == DeforestationAnalysisStatus.PENDING || status == DeforestationAnalysisStatus.IN_PROGRESS;
	}

}
