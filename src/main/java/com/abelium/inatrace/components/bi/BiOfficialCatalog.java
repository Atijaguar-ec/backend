package com.abelium.inatrace.components.bi;

import com.abelium.inatrace.components.bi.api.ApiBiDashboard;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Immutable catalog of official Superset dashboards and authorized bi.* views per tenant.
 */
@Component
public class BiOfficialCatalog {

	public static final List<String> ALL_COMPANY_ROLES = List.of(
			"USER", "COMPANY_ADMIN", "REGIONAL_ADMIN", "SYSTEM_ADMIN"
	);

	public static final Set<String> VALID_ALLOWED_ROLES = Set.copyOf(ALL_COMPANY_ROLES);

	private static final List<String> FORTALEZA_ALLOWED_DATASETS = List.of(
			"bi.rpt_cacao_arqueo_bascula_fortaleza",
			"bi.rpt_cacao_beneficio_fortaleza",
			"bi.rpt_cacao_existencia_bodega_fortaleza",
			"bi.rpt_cacao_salidas_despachos_fortaleza",
			"bi.rpt_cacao_cuarentena_rendimiento_fortaleza"
	);

	private static final List<String> UNOCACE_ALLOWED_DATASETS = List.of(
			"bi.fact_cacao_purchase",
			"bi.rpt_cacao_acopio_semanal",
			"bi.fact_cacao_process",
			"bi.rpt_cacao_process_performance",
			"bi.fact_cacao_plot",
			"bi.fact_cacao_dispatch_export",
			"bi.rpt_cacao_capacity_anomalies",
			"bi.rpt_cacao_payment_reconciliation",
			"bi.rpt_cacao_agrocalidad_guia",
			"bi.rpt_cacao_compras_certificadas"
	);

	public List<ApiBiDashboard> getOfficialDashboards(String organizationSlug, String environment) {
		String org = normalize(organizationSlug);
		String env = StringUtils.defaultIfBlank(normalize(environment), "staging");

		if ("fortaleza".equals(org)) {
			return List.of(
					new ApiBiDashboard(
							"fortaleza-cacao-arqueo-bascula-" + env,
							"REGISTRO DE PESO POR PRODUCTOR",
							"Informe de Arqueo de Peso de Bascula.",
							true,
							10,
							ALL_COMPANY_ROLES
					),
					new ApiBiDashboard(
							"fortaleza-cacao-fermentacion-" + env,
							"REG. RECEPCIÓN Y FERMENTACIÓN",
							"REG. DE RECEPCIÓN Y PROCESO DE FERMENTACIÓN.",
							true,
							20,
							ALL_COMPANY_ROLES
					),
					new ApiBiDashboard(
							"fortaleza-cacao-almacenamiento-" + env,
							"REG. INGRESO ALMACENAMIENTO",
							"DATOS DE INGRESO EN ALMACENAMIENTO.",
							true,
							30,
							ALL_COMPANY_ROLES
					),
					new ApiBiDashboard(
							"fortaleza-cacao-secado-" + env,
							"REG. SECADO",
							"REG. DE PROCESO DE SECADO.",
							true,
							40,
							ALL_COMPANY_ROLES
					),
					new ApiBiDashboard(
							"fortaleza-cacao-existencia-" + env,
							"CACAO EN EXISTENCIA",
							"SALDO / EXISTENCIA.",
							true,
							50,
							ALL_COMPANY_ROLES
					),
					new ApiBiDashboard(
							"fortaleza-cacao-salidas-" + env,
							"REGISTRO DE SALIDA",
							"DATOS DE SALIDA.",
							true,
							60,
							ALL_COMPANY_ROLES
					),
					new ApiBiDashboard(
							"fortaleza-cacao-cuarentena-calificacion-" + env,
							"Cuarentena y Calificación",
							"PRODUCTORES EN CUARENTENA y REGISTRO DE INGRESO DE CACAO EN BABA Y OBTENIDO EN SECO POR SEMANA Y CALIFICACION.",
							true,
							70,
							ALL_COMPANY_ROLES
					)
			);
		}

		if ("unocace".equals(org)) {
			return List.of(
					new ApiBiDashboard(
							"unocace-cacao-agrocalidad-guia-" + env,
							"Agrocalidad (Sistema GUIA)",
							"Reporte regulatorio oficial para exportación al Sistema GUIA.",
							true,
							10,
							ALL_COMPANY_ROLES
					),
					new ApiBiDashboard(
							"unocace-cacao-compras-certificadas-" + env,
							"Compras Certificadas",
							"Desglose por certificación (Orgánico, Transición, Convencional Fairtrade).",
							true,
							20,
							ALL_COMPANY_ROLES
					),
					new ApiBiDashboard(
							"unocace-cacao-acopio-calidad-" + env,
							"Acopio Semanal & Calidad",
							"Evolución semanal de compras por variedad.",
							true,
							30,
							ALL_COMPANY_ROLES
					),
					new ApiBiDashboard(
							"unocace-cacao-procesos-rendimientos-" + env,
							"Rendimientos & Procesamiento",
							"Rendimientos de transformación y trazabilidad de lotes procesados.",
							true,
							40,
							ALL_COMPANY_ROLES
					),
					new ApiBiDashboard(
							"unocace-cacao-productores-parcelas-" + env,
							"Productores & Parcelas",
							"Georreferenciación y distribución de parcelas de productores.",
							true,
							50,
							ALL_COMPANY_ROLES
					),
					new ApiBiDashboard(
							"unocace-cacao-pagos-conciliacion-" + env,
							"Liquidación & Pagos",
							"Conciliación de pagos frente a costos de compra registrados.",
							true,
							60,
							ALL_COMPANY_ROLES
					)
			);
		}

		return List.of();
	}

	public List<String> getAllowedDatasets(String organizationSlug) {
		String org = normalize(organizationSlug);
		if ("fortaleza".equals(org)) {
			return FORTALEZA_ALLOWED_DATASETS;
		}
		if ("unocace".equals(org)) {
			return UNOCACE_ALLOWED_DATASETS;
		}
		return List.of();
	}

	public boolean isForbiddenForOrganization(String organizationSlug, String slug) {
		if ("fortaleza".equals(normalize(organizationSlug)) && slug != null) {
			return normalize(slug).contains("compras-certificadas");
		}
		return false;
	}

	public boolean isReservedOfficialPrefix(String organizationSlug, String slug) {
		String org = normalize(organizationSlug);
		return StringUtils.isNotBlank(org) && slug != null && normalize(slug).startsWith(org + "-cacao-");
	}

	private static String normalize(String value) {
		return StringUtils.lowerCase(StringUtils.trimToEmpty(value));
	}
}
