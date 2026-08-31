package com.abelium.inatrace.components.whisp.mappers;

import com.abelium.inatrace.components.whisp.WhispJob;
import com.abelium.inatrace.components.whisp.api.ApiWhispPlotAnalysis;
import com.abelium.inatrace.db.entities.common.PlotDeforestationAnalysis;
import com.abelium.inatrace.types.DeforestationAnalysisStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Translation between Whisp's result properties and the stored analysis.
 *
 * Whisp names its columns after the dataset that produced them and has renamed them
 * between releases ({@code Ind_1_treecover} became {@code Ind_01_treecover}), so every
 * lookup here matches case-insensitively and falls back to a substring rule. A column
 * that cannot be matched is not lost: the whole property set is archived in
 * {@code resultJson} and returned to the client in {@code results}.
 */
public class WhispAnalysisMapper {

	private WhispAnalysisMapper() {
	}

	public static ApiWhispPlotAnalysis toApiWhispPlotAnalysis(PlotDeforestationAnalysis analysis, ObjectMapper objectMapper) {

		if (analysis == null) {
			return null;
		}

		ApiWhispPlotAnalysis api = new ApiWhispPlotAnalysis();
		api.setId(analysis.getId());
		api.setPlotId(analysis.getPlot() != null ? analysis.getPlot().getId() : null);
		api.setPlotName(analysis.getPlot() != null ? analysis.getPlot().getPlotName() : null);
		api.setStatus(analysis.getStatus());
		api.setWhispToken(analysis.getWhispToken());
		api.setSubmissionMode(analysis.getSubmissionMode());
		api.setGeoId(analysis.getGeoId());
		api.setRequestedAt(analysis.getRequestedAt());
		api.setCompletedAt(analysis.getCompletedAt());
		api.setProgressPercent(analysis.getProgressPercent());
		api.setRiskPcrop(analysis.getRiskPcrop());
		api.setIndicatorTreeCover(analysis.getIndicatorTreeCover());
		api.setIndicatorCommodities(analysis.getIndicatorCommodities());
		api.setIndicatorDisturbanceBefore2020(analysis.getIndicatorDisturbanceBefore2020());
		api.setIndicatorDisturbanceAfter2020(analysis.getIndicatorDisturbanceAfter2020());
		api.setArea(analysis.getArea());
		api.setAreaUnit(analysis.getAreaUnit());
		api.setCountry(analysis.getCountry());
		api.setErrorMessage(analysis.getErrorMessage());
		api.setResults(readResults(analysis.getResultJson(), objectMapper));

		return api;
	}

	/**
	 * Copies the latest job state onto the stored analysis.
	 */
	public static void applyJob(PlotDeforestationAnalysis analysis, WhispJob job) {

		if (analysis == null || job == null) {
			return;
		}

		if (StringUtils.isNotBlank(job.getToken())) {
			analysis.setWhispToken(job.getToken());
		}
		analysis.setStatus(job.getStatus());
		analysis.setProgressPercent(job.getPercent());

		if (job.getStatus() == DeforestationAnalysisStatus.FAILED) {
			analysis.setErrorMessage(StringUtils.abbreviate(job.getMessage(), 2000));
			return;
		}

		// A job that moved on from a previous failure should not keep showing the old
		// reason next to a fresh result.
		analysis.setErrorMessage(null);

		JsonNode properties = job.getFeatureProperties();
		if (properties == null) {
			return;
		}

		analysis.setResultJson(job.getRawResult());
		analysis.setCompletedAt(new Date());
		analysis.setProgressPercent(100);

		analysis.setRiskPcrop(text(properties, "risk_pcrop", key -> key.contains("pcrop")));
		analysis.setIndicatorTreeCover(text(properties, "Ind_01_treecover", key -> key.contains("treecover")));
		analysis.setIndicatorCommodities(text(properties, "Ind_02_commodities", key -> key.contains("commodities")));
		analysis.setIndicatorDisturbanceBefore2020(
				text(properties, "Ind_03_disturbance_before_2020", key -> key.contains("disturbance_before")));
		analysis.setIndicatorDisturbanceAfter2020(
				text(properties, "Ind_04_disturbance_after_2020", key -> key.contains("disturbance_after")));
		analysis.setAreaUnit(text(properties, "Unit", key -> key.equals("unit")));
		analysis.setCountry(text(properties, "Country", key -> key.equals("country")));

		JsonNode area = find(properties, "Area", key -> key.equals("area"));
		if (area != null && area.isNumber()) {
			analysis.setArea(area.asDouble());
		}
	}

	private static Map<String, Object> readResults(String resultJson, ObjectMapper objectMapper) {

		if (StringUtils.isBlank(resultJson)) {
			return null;
		}
		try {
			return objectMapper.readValue(resultJson, new TypeReference<Map<String, Object>>() {
			});
		} catch (Exception e) {
			// Archived text that no longer parses must not take the whole plot screen
			// down; the columns extracted at analysis time are still served.
			return null;
		}
	}

	private static String text(JsonNode properties, String exactKey, Predicate<String> fallback) {
		JsonNode value = find(properties, exactKey, fallback);
		if (value == null || value.isNull()) {
			return null;
		}
		return StringUtils.abbreviate(value.isValueNode() ? value.asText() : value.toString(), 255);
	}

	/**
	 * Case-insensitive exact match first, then the substring rule, over the lower-cased
	 * property names.
	 */
	private static JsonNode find(JsonNode properties, String exactKey, Predicate<String> fallback) {

		JsonNode exact = properties.get(exactKey);
		if (exact != null) {
			return exact;
		}

		JsonNode fallbackMatch = null;
		for (Iterator<String> it = properties.fieldNames(); it.hasNext(); ) {
			String name = it.next();
			String lower = name.toLowerCase();
			if (lower.equals(exactKey.toLowerCase())) {
				return properties.get(name);
			}
			if (fallbackMatch == null && fallback.test(lower)) {
				fallbackMatch = properties.get(name);
			}
		}
		return fallbackMatch;
	}

}
