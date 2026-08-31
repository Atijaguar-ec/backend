package com.abelium.inatrace.components.whisp;

import com.abelium.inatrace.components.agstack.AgStackClientTokenManager;
import com.abelium.inatrace.types.DeforestationAnalysisStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the three shapes the Whisp API answers with. The bodies are the real ones:
 * the 401 was captured from the live API on 2026-08-27, the others follow the documented
 * envelope.
 */
class WhispClientServiceTest {

	private final WhispClientService client =
			new WhispClientService(new AgStackClientTokenManager(), new ObjectMapper());

	@Test
	void shouldReportTheApiKeyProblemInsteadOfAnOpaqueFailure() {

		WhispJob job = client.interpret(new WhispClientService.RawResponse(
				HttpStatus.UNAUTHORIZED,
				"{\"code\":\"auth_invalid_api_key\",\"message\":\"Invalid or expired API key.\"}"));

		assertEquals(DeforestationAnalysisStatus.FAILED, job.getStatus());
		assertTrue(job.getMessage().contains("Invalid or expired API key."));
	}

	@Test
	void shouldReadTheTokenOfAQueuedJob() {

		WhispJob job = client.interpret(new WhispClientService.RawResponse(
				HttpStatus.OK,
				"{\"code\":\"ok\",\"message\":\"queued\",\"data\":{\"token\":\"abc-123\"}}"));

		assertEquals("abc-123", job.getToken());
		assertEquals(DeforestationAnalysisStatus.PENDING, job.getStatus());
		assertNull(job.getFeatureProperties());
	}

	@Test
	void shouldReadTheProgressOfARunningJob() {

		WhispJob job = client.interpret(new WhispClientService.RawResponse(
				HttpStatus.ACCEPTED,
				"{\"code\":\"in_progress\",\"message\":\"running\",\"data\":{\"token\":\"abc-123\","
						+ "\"percent\":42,\"processStatusMessage\":[\"analysing\"]}}"));

		assertEquals(DeforestationAnalysisStatus.IN_PROGRESS, job.getStatus());
		assertEquals(42, job.getPercent());
	}

	@Test
	void shouldTakeThePlotPropertiesFromAFinishedFeatureCollection() {

		WhispJob job = client.interpret(new WhispClientService.RawResponse(
				HttpStatus.OK,
				"{\"code\":\"ok\",\"message\":\"done\",\"data\":{\"type\":\"FeatureCollection\",\"features\":["
						+ "{\"type\":\"Feature\",\"geometry\":null,"
						+ "\"properties\":{\"risk_pcrop\":\"low\",\"Area\":2.5}}]}}"));

		assertEquals(DeforestationAnalysisStatus.COMPLETED, job.getStatus());
		assertNotNull(job.getFeatureProperties());
		assertEquals("low", job.getFeatureProperties().get("risk_pcrop").asText());
		// Only the properties are archived; the geometry is the one we sent.
		assertTrue(job.getRawResult().contains("risk_pcrop"));
		assertTrue(!job.getRawResult().contains("FeatureCollection"));
	}

	@Test
	void shouldAcceptABareFeatureCollectionWithoutTheEnvelope() {

		WhispJob job = client.interpret(new WhispClientService.RawResponse(
				HttpStatus.OK,
				"{\"type\":\"FeatureCollection\",\"features\":[{\"properties\":{\"risk_pcrop\":\"high\"}}]}"));

		assertEquals(DeforestationAnalysisStatus.COMPLETED, job.getStatus());
		assertEquals("high", job.getFeatureProperties().get("risk_pcrop").asText());
	}

	@Test
	void shouldTurnTheDatasetCsvIntoJsonRows() {

		// The lookup-datasets endpoint answers CSV, unlike every other Whisp endpoint.
		JsonNode rows = client.parseCsv("""
				name,order,theme,use_for_risk_pcrop,col_type
				EUFO_2020,10,treecover,1,float32
				GFC_TC_2020,40,treecover,0,float32
				""");

		assertNotNull(rows);
		assertEquals(2, rows.size());
		assertEquals("EUFO_2020", rows.get(0).get("name").asText());
		assertEquals("treecover", rows.get(0).get("theme").asText());
		assertEquals("float32", rows.get(1).get("col_type").asText());
	}

	@Test
	void shouldNotShiftColumnsWhenACsvFieldContainsAComma() {

		JsonNode rows = client.parseCsv("""
				name,description,col_type
				EUFO_2020,"Forest cover, EU definition",float32
				""");

		assertEquals("Forest cover, EU definition", rows.get(0).get("description").asText());
		assertEquals("float32", rows.get(0).get("col_type").asText());
	}

	@Test
	void shouldLeaveEmptyCsvCellsAsNull() {

		JsonNode rows = client.parseCsv("""
				name,ISO2_code,col_type
				EUFO_2020,,float32
				""");

		assertTrue(rows.get(0).get("ISO2_code").isNull());
	}

}
