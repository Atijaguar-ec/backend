package com.abelium.inatrace.components.bi;

import com.abelium.inatrace.components.company.CompanyApiTools;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompanyApiToolsConfigurationMergeTest {

	@Test
	void preservesCustomBiDashboardsWhenCompanyFormOmitsThatKey() {
		List<Map<String, Object>> customDashboards = List.of(Map.of(
				"slug", "fv-resumen-mensual",
				"label", "Resumen Mensual",
				"allowedRoles", List.of("USER", "REGIONAL_ADMIN")
		));

		Map<String, Object> existingConfig = new LinkedHashMap<>();
		existingConfig.put("enableQuotaBalance", true);
		existingConfig.put("customBiDashboards", customDashboards);

		Map<String, Object> incomingConfig = new LinkedHashMap<>();
		incomingConfig.put("enableQuotaBalance", true);
		incomingConfig.put("enableDeliveryReceipt", true);

		Map<String, Object> merged = CompanyApiTools.mergePreservedBiConfiguration(existingConfig, incomingConfig);
		assertNotNull(merged);
		assertEquals(Boolean.TRUE, merged.get("enableDeliveryReceipt"));
		assertEquals(customDashboards, merged.get("customBiDashboards"));
	}

	@Test
	void preservesCustomBiDashboardsEvenWhenIncomingConfigIsNull() {
		Map<String, Object> existingConfig = Map.of(
				"customBiDashboards", List.of(Map.of("slug", "fv-tab", "label", "Tab"))
		);

		Map<String, Object> merged = CompanyApiTools.mergePreservedBiConfiguration(existingConfig, null);
		assertNotNull(merged);
		assertTrue(merged.containsKey("customBiDashboards"));
	}
}
