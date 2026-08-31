package com.abelium.inatrace.components.whisp;

import com.abelium.inatrace.components.whisp.mappers.WhispAnalysisMapper;
import com.abelium.inatrace.db.entities.common.PlotDeforestationAnalysis;
import com.abelium.inatrace.types.DeforestationAnalysisStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class WhispAnalysisMapperTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void shouldExtractTheEudrIndicatorsFromACompletedAnalysis() throws Exception {

		PlotDeforestationAnalysis analysis = new PlotDeforestationAnalysis();

		WhispAnalysisMapper.applyJob(analysis, completedJob("""
				{
				  "plotId": "1",
				  "Area": 3.42,
				  "Country": "Ecuador",
				  "Unit": "ha",
				  "GFC_TC_2020": 87.1,
				  "Ind_01_treecover": "yes",
				  "Ind_02_commodities": "no",
				  "Ind_03_disturbance_before_2020": "no",
				  "Ind_04_disturbance_after_2020": "yes",
				  "risk_pcrop": "high"
				}"""));

		assertEquals(DeforestationAnalysisStatus.COMPLETED, analysis.getStatus());
		assertEquals("high", analysis.getRiskPcrop());
		assertEquals("yes", analysis.getIndicatorTreeCover());
		assertEquals("no", analysis.getIndicatorCommodities());
		assertEquals("no", analysis.getIndicatorDisturbanceBefore2020());
		assertEquals("yes", analysis.getIndicatorDisturbanceAfter2020());
		assertEquals(3.42, analysis.getArea());
		assertEquals("ha", analysis.getAreaUnit());
		assertEquals("Ecuador", analysis.getCountry());
		assertEquals(100, analysis.getProgressPercent());
		assertNotNull(analysis.getCompletedAt());
		// Datasets without a column of their own stay recoverable from the archive.
		assertNotNull(analysis.getResultJson());
		assertEquals("87.1", objectMapper.readTree(analysis.getResultJson()).get("GFC_TC_2020").asText());
	}

	@Test
	void shouldStillMatchIndicatorsWhenWhispRenamesTheColumns() throws Exception {

		PlotDeforestationAnalysis analysis = new PlotDeforestationAnalysis();

		// Whisp has shipped these columns as Ind_1_* and with different capitalisation.
		WhispAnalysisMapper.applyJob(analysis, completedJob("""
				{
				  "Ind_1_treecover": "no",
				  "Ind_4_disturbance_after_2020": "no",
				  "AREA": 1.5,
				  "risk_pcrop": "low"
				}"""));

		assertEquals("no", analysis.getIndicatorTreeCover());
		assertEquals("no", analysis.getIndicatorDisturbanceAfter2020());
		assertEquals(1.5, analysis.getArea());
		assertEquals("low", analysis.getRiskPcrop());
	}

	@Test
	void shouldNotConfuseTheTimberRiskWithThePerennialCropRisk() throws Exception {

		PlotDeforestationAnalysis analysis = new PlotDeforestationAnalysis();

		WhispAnalysisMapper.applyJob(analysis, completedJob("""
				{
				  "risk_timber": "high",
				  "risk_acrop": "high"
				}"""));

		assertNull(analysis.getRiskPcrop());
	}

	@Test
	void shouldKeepTheReasonWhenTheJobFailed() {

		PlotDeforestationAnalysis analysis = new PlotDeforestationAnalysis();

		WhispJob job = new WhispJob();
		job.setStatus(DeforestationAnalysisStatus.FAILED);
		job.setMessage("Invalid or expired API key.");

		WhispAnalysisMapper.applyJob(analysis, job);

		assertEquals(DeforestationAnalysisStatus.FAILED, analysis.getStatus());
		assertEquals("Invalid or expired API key.", analysis.getErrorMessage());
		assertNull(analysis.getResultJson());
	}

	@Test
	void shouldClearAPreviousErrorOnceTheJobProgresses() throws Exception {

		PlotDeforestationAnalysis analysis = new PlotDeforestationAnalysis();
		analysis.setErrorMessage("Whisp is not reachable");

		WhispAnalysisMapper.applyJob(analysis, completedJob("{\"risk_pcrop\": \"low\"}"));

		assertNull(analysis.getErrorMessage());
		assertEquals("low", analysis.getRiskPcrop());
	}

	private WhispJob completedJob(String properties) throws Exception {
		JsonNode node = objectMapper.readTree(properties);
		WhispJob job = new WhispJob();
		job.setStatus(DeforestationAnalysisStatus.COMPLETED);
		job.setFeatureProperties(node);
		job.setRawResult(node.toString());
		return job;
	}

}
