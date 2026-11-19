package com.abelium.inatrace.components.bi;

import com.abelium.inatrace.api.ApiStatus;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.bi.api.ApiBiDashboard;
import com.abelium.inatrace.components.bi.api.ApiBiDashboardCatalogResponse;
import com.abelium.inatrace.components.bi.api.ApiBiGuestTokenRequest;
import com.abelium.inatrace.components.bi.api.ApiBiGuestTokenResponse;
import com.abelium.inatrace.components.bi.api.ApiCustomBiDashboardRequest;
import com.abelium.inatrace.components.common.BaseService;
import com.abelium.inatrace.components.company.CompanyQueries;
import com.abelium.inatrace.db.entities.company.Company;
import com.abelium.inatrace.security.service.CustomUserDetails;
import com.abelium.inatrace.types.UserRole;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Business service for authorized Superset dashboard discovery, guest-token issuance,
 * and admin registration of Studio dashboards in company.configuration.customBiDashboards.
 */
@Service
public class BiReportsService extends BaseService {

	public static final String CUSTOM_BI_DASHBOARDS_KEY = "customBiDashboards";

	private final CompanyQueries companyQueries;
	private final BiSupersetProperties properties;
	private final BiOfficialCatalog officialCatalog;
	private final SupersetClient supersetClient;

	@Autowired
	public BiReportsService(
			CompanyQueries companyQueries,
			BiSupersetProperties properties,
			BiOfficialCatalog officialCatalog,
			SupersetClient supersetClient
	) {
		this.companyQueries = companyQueries;
		this.properties = properties;
		this.officialCatalog = officialCatalog;
		this.supersetClient = supersetClient;
	}

	@Transactional
	public ApiBiDashboardCatalogResponse getDashboardCatalog(Long companyId, CustomUserDetails userDetails) throws ApiException {
		requireAuthenticatedUser(userDetails);
		Company company = companyQueries.fetchCompany(companyId);
		String orgSlug = properties.normalizedOrganizationSlug();
		Set<String> effectiveRoles = resolveEffectiveRoles(companyId, userDetails);

		List<ApiBiDashboard> visible = new ArrayList<>();
		for (ApiBiDashboard dashboard : collectTenantDashboards(company, orgSlug)) {
			if (isRoleAuthorized(effectiveRoles, dashboard.allowedRoles)) {
				visible.add(dashboard);
			}
		}
		visible.sort(Comparator.comparingInt((ApiBiDashboard d) -> d.sortOrder).thenComparing(d -> d.label));

		ApiBiDashboardCatalogResponse response = new ApiBiDashboardCatalogResponse();
		response.supersetDomain = properties.normalizedPublicDomain();
		response.organizationSlug = orgSlug;
		response.studioEnabled = isStudioAdmin(userDetails);
		response.allowedDatasets = officialCatalog.getAllowedDatasets(orgSlug);
		response.dashboards = visible;
		return response;
	}

	@Transactional
	public ApiBiGuestTokenResponse issueGuestToken(ApiBiGuestTokenRequest request, CustomUserDetails userDetails) throws ApiException {
		requireAuthenticatedUser(userDetails);
		properties.requireConfigured();

		Long companyId = request.getCompanyId();
		String slug = normalizeSlug(request.getSlug());
		String orgSlug = properties.normalizedOrganizationSlug();

		if (officialCatalog.isForbiddenForOrganization(orgSlug, slug)) {
			throw new BiReportException(
					HttpStatus.FORBIDDEN,
					ApiStatus.UNAUTHORIZED,
					"Este reporte no está habilitado para la organización activa."
			);
		}

		Company company = companyQueries.fetchCompany(companyId);
		ApiBiDashboard target = collectTenantDashboards(company, orgSlug).stream()
				.filter(d -> d.slug.equals(slug))
				.findFirst()
				.orElseThrow(() -> new BiReportException(
						HttpStatus.NOT_FOUND,
						ApiStatus.NOT_FOUND,
						"El tablero '" + slug + "' no está registrado para esta empresa."
				));

		Set<String> effectiveRoles = resolveEffectiveRoles(companyId, userDetails);
		if (!isRoleAuthorized(effectiveRoles, target.allowedRoles)) {
			throw new BiReportException(
					HttpStatus.FORBIDDEN,
					ApiStatus.UNAUTHORIZED,
					"No tiene permisos para visualizar este tablero."
			);
		}

		SupersetClient.EmbeddedInfo embedded = supersetClient.getDashboardEmbedded(slug);
		String portalOrigin = properties.normalizedPortalOrigin();
		if (embedded.allowedDomains().stream().noneMatch(domain -> domain.equalsIgnoreCase(portalOrigin))) {
			throw new BiReportException(
					HttpStatus.UNPROCESSABLE_ENTITY,
					ApiStatus.VALIDATION_ERROR,
					"El tablero '" + slug + "' no autoriza el dominio del portal (" + portalOrigin + ") en Superset."
			);
		}

		String guestToken = supersetClient.createGuestToken(embedded.uuid(), userDetails);
		return new ApiBiGuestTokenResponse(
				properties.normalizedPublicDomain(),
				embedded.uuid(),
				guestToken,
				properties.getGuestTokenTtlSeconds()
		);
	}

	@Transactional
	public ApiBiDashboard registerCustomDashboard(ApiCustomBiDashboardRequest request, CustomUserDetails userDetails) throws ApiException {
		requireStudioAdmin(userDetails);
		properties.requireConfigured();

		String orgSlug = properties.normalizedOrganizationSlug();
		String slug = normalizeSlug(request.getSlug());

		if (officialCatalog.isForbiddenForOrganization(orgSlug, slug)) {
			throw new BiReportException(
					HttpStatus.FORBIDDEN,
					ApiStatus.UNAUTHORIZED,
					"No está permitido registrar este reporte para la organización activa."
			);
		}
		if (officialCatalog.isReservedOfficialPrefix(orgSlug, slug)) {
			throw new BiReportException(
					HttpStatus.UNPROCESSABLE_ENTITY,
					ApiStatus.VALIDATION_ERROR,
					"El slug no puede iniciar con el prefijo reservado '" + orgSlug + "-cacao-'."
			);
		}

		List<String> normalizedRoles = normalizeAndValidateAllowedRoles(request.getAllowedRoles());

		// Validate dashboard exists, is embedded for portalOrigin, uses only allowed bi.* views,
		// and has query_context saved on all charts before publishing.
		SupersetClient.DashboardMetadata metadata = supersetClient.getDashboardMetadata(slug);
		if (metadata.datasetKeys().isEmpty()) {
			throw new BiReportException(
					HttpStatus.UNPROCESSABLE_ENTITY,
					ApiStatus.VALIDATION_ERROR,
					"El tablero '" + slug + "' no contiene datasets configurados."
			);
		}

		Set<String> allowedDatasetSet = Set.copyOf(officialCatalog.getAllowedDatasets(orgSlug));
		List<String> unauthorizedDatasets = metadata.datasetKeys().stream()
				.filter(ds -> !allowedDatasetSet.contains(ds))
				.sorted()
				.toList();
		if (!unauthorizedDatasets.isEmpty()) {
			throw new BiReportException(
					HttpStatus.UNPROCESSABLE_ENTITY,
					ApiStatus.VALIDATION_ERROR,
					"El tablero usa datasets no autorizados para EmbeddedGuest: " + String.join(", ", unauthorizedDatasets)
			);
		}

		if (metadata.chartCount() == 0 || !metadata.allChartsHaveQueryContext()) {
			throw new BiReportException(
					HttpStatus.UNPROCESSABLE_ENTITY,
					ApiStatus.VALIDATION_ERROR,
					"Todos los gráficos del tablero deben guardarse en Superset (con query_context) antes de publicarlo."
			);
		}

		SupersetClient.EmbeddedInfo embedded = supersetClient.getDashboardEmbedded(slug);
		String portalOrigin = properties.normalizedPortalOrigin();
		if (embedded.allowedDomains().stream().noneMatch(domain -> domain.equalsIgnoreCase(portalOrigin))) {
			throw new BiReportException(
					HttpStatus.UNPROCESSABLE_ENTITY,
					ApiStatus.VALIDATION_ERROR,
					"Active 'Embed dashboard' en Superset agregando el dominio '" + portalOrigin + "'."
			);
		}

		Company company = companyQueries.fetchCompany(request.getCompanyId());
		List<ApiBiDashboard> existingCustom = readCustomDashboards(company, orgSlug);
		int defaultOrder = 100 + existingCustom.size() * 10;
		int sortOrder = request.getSortOrder() != null ? request.getSortOrder() : defaultOrder;
		ApiBiDashboard newEntry = new ApiBiDashboard(
				slug,
				StringUtils.trimToEmpty(request.getLabel()),
				StringUtils.trimToEmpty(request.getDescription()),
				false,
				sortOrder,
				normalizedRoles
		);

		List<Map<String, Object>> serialized = new ArrayList<>();
		boolean replaced = false;
		for (ApiBiDashboard item : existingCustom) {
			if (item.slug.equals(slug)) {
				serialized.add(toConfigMap(newEntry));
				replaced = true;
			} else {
				serialized.add(toConfigMap(item));
			}
		}
		if (!replaced) {
			serialized.add(toConfigMap(newEntry));
		}

		writeCustomDashboards(company, serialized);
		return newEntry;
	}

	@Transactional
	public void removeCustomDashboard(Long companyId, String rawSlug, CustomUserDetails userDetails) throws ApiException {
		requireStudioAdmin(userDetails);
		String orgSlug = properties.normalizedOrganizationSlug();
		String slug = normalizeSlug(rawSlug);

		boolean isOfficial = officialCatalog.getOfficialDashboards(orgSlug, properties.normalizedEnvironment())
				.stream()
				.anyMatch(d -> d.slug.equals(slug));
		if (isOfficial) {
			throw new BiReportException(
					HttpStatus.FORBIDDEN,
					ApiStatus.UNAUTHORIZED,
					"Los tableros oficiales no pueden eliminarse desde esta sección."
			);
		}

		Company company = companyQueries.fetchCompany(companyId);
		List<ApiBiDashboard> existingCustom = readCustomDashboards(company, orgSlug);
		List<Map<String, Object>> remaining = new ArrayList<>();
		boolean removed = false;
		for (ApiBiDashboard item : existingCustom) {
			if (item.slug.equals(slug)) {
				removed = true;
			} else {
				remaining.add(toConfigMap(item));
			}
		}
		if (!removed) {
			throw new BiReportException(
					HttpStatus.NOT_FOUND,
					ApiStatus.NOT_FOUND,
					"El tablero personalizado '" + slug + "' no está registrado en esta empresa."
			);
		}
		writeCustomDashboards(company, remaining);
	}

	private List<ApiBiDashboard> collectTenantDashboards(Company company, String orgSlug) {
		List<ApiBiDashboard> all = new ArrayList<>(
				officialCatalog.getOfficialDashboards(orgSlug, properties.normalizedEnvironment())
		);
		Set<String> officialSlugs = new LinkedHashSet<>();
		for (ApiBiDashboard d : all) {
			officialSlugs.add(d.slug);
		}
		for (ApiBiDashboard custom : readCustomDashboards(company, orgSlug)) {
			if (!officialSlugs.contains(custom.slug)
					&& !officialCatalog.isForbiddenForOrganization(orgSlug, custom.slug)) {
				all.add(custom);
			}
		}
		return all;
	}

	@SuppressWarnings("unchecked")
	private List<ApiBiDashboard> readCustomDashboards(Company company, String orgSlug) {
		Map<String, Object> config = company.getConfiguration();
		if (config == null || !(config.get(CUSTOM_BI_DASHBOARDS_KEY) instanceof List<?> rawList)) {
			return List.of();
		}
		List<ApiBiDashboard> result = new ArrayList<>();
		int index = 0;
		for (Object entryObj : rawList) {
			if (!(entryObj instanceof Map<?, ?> rawMap)) {
				continue;
			}
			String slug = normalizeSlug(Objects.toString(rawMap.get("slug"), ""));
			String label = StringUtils.trimToEmpty(Objects.toString(rawMap.get("label"), ""));
			if (StringUtils.isAnyBlank(slug, label)
					|| officialCatalog.isForbiddenForOrganization(orgSlug, slug)
					|| officialCatalog.isReservedOfficialPrefix(orgSlug, slug)) {
				continue;
			}
			String description = StringUtils.trimToEmpty(Objects.toString(rawMap.get("description"), ""));
			int sortOrder = parseInt(rawMap.get("sortOrder"), 100 + index * 10);
			List<String> allowedRoles = parseStoredAllowedRoles(rawMap.get("allowedRoles"));
			if (allowedRoles.isEmpty()) {
				continue;
			}
			result.add(new ApiBiDashboard(slug, label, description, false, sortOrder, allowedRoles));
			index++;
		}
		return result;
	}

	private void writeCustomDashboards(Company company, List<Map<String, Object>> serialized) {
		Map<String, Object> updated = new LinkedHashMap<>();
		if (company.getConfiguration() != null) {
			updated.putAll(company.getConfiguration());
		}
		updated.put(CUSTOM_BI_DASHBOARDS_KEY, serialized);
		company.setConfiguration(updated);
	}

	private Set<String> resolveEffectiveRoles(Long companyId, CustomUserDetails userDetails) {
		Set<String> roles = new LinkedHashSet<>();
		if (userDetails.getUserRole() != null) {
			roles.add(userDetails.getUserRole().name());
		}
		if (userDetails.getUserId() != null) {
			List<Long> adminCompanies = companyQueries.fetchCompanyIdsForUserAdmin(userDetails.getUserId());
			if (adminCompanies != null && adminCompanies.contains(companyId)) {
				roles.add("COMPANY_ADMIN");
			}
		}
		return roles;
	}

	private static boolean isRoleAuthorized(Set<String> userRoles, List<String> allowedRoles) {
		if (allowedRoles == null || allowedRoles.isEmpty()) {
			return false;
		}
		for (String role : allowedRoles) {
			if (userRoles.contains(role)) {
				return true;
			}
		}
		return false;
	}

	private static List<String> normalizeAndValidateAllowedRoles(List<String> rawRoles) throws BiReportException {
		if (rawRoles == null || rawRoles.isEmpty()) {
			throw new BiReportException(
					HttpStatus.UNPROCESSABLE_ENTITY,
					ApiStatus.VALIDATION_ERROR,
					"Debe asignar al menos un rol autorizado."
			);
		}
		Set<String> cleaned = new LinkedHashSet<>();
		for (String role : rawRoles) {
			String normalized = StringUtils.upperCase(StringUtils.trimToEmpty(role));
			if (!BiOfficialCatalog.VALID_ALLOWED_ROLES.contains(normalized)) {
				throw new BiReportException(
						HttpStatus.UNPROCESSABLE_ENTITY,
						ApiStatus.VALIDATION_ERROR,
						"Rol autorizado inválido: '" + role + "'."
				);
			}
			cleaned.add(normalized);
		}
		return new ArrayList<>(cleaned);
	}

	private static List<String> parseStoredAllowedRoles(Object raw) {
		if (!(raw instanceof List<?> list)) {
			return List.of();
		}
		List<String> roles = new ArrayList<>();
		for (Object item : list) {
			String normalized = StringUtils.upperCase(StringUtils.trimToEmpty(Objects.toString(item, "")));
			if (BiOfficialCatalog.VALID_ALLOWED_ROLES.contains(normalized) && !roles.contains(normalized)) {
				roles.add(normalized);
			}
		}
		return roles;
	}

	private static Map<String, Object> toConfigMap(ApiBiDashboard dashboard) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("slug", dashboard.slug);
		map.put("label", dashboard.label);
		map.put("description", StringUtils.defaultString(dashboard.description));
		map.put("sortOrder", dashboard.sortOrder);
		map.put("allowedRoles", dashboard.allowedRoles);
		return map;
	}

	private static void requireAuthenticatedUser(CustomUserDetails userDetails) throws BiReportException {
		if (userDetails == null || userDetails.getUserRole() == null) {
			throw new BiReportException(HttpStatus.UNAUTHORIZED, ApiStatus.AUTH_ERROR,
					"Se requiere sesión autenticada.");
		}
	}

	private static void requireStudioAdmin(CustomUserDetails userDetails) throws BiReportException {
		requireAuthenticatedUser(userDetails);
		if (!isStudioAdmin(userDetails)) {
			throw new BiReportException(HttpStatus.FORBIDDEN, ApiStatus.UNAUTHORIZED,
					"Solo administradores del sistema o regionales pueden publicar tableros BI.");
		}
	}

	private static boolean isStudioAdmin(CustomUserDetails userDetails) {
		return userDetails != null && (
				userDetails.getUserRole() == UserRole.SYSTEM_ADMIN
						|| userDetails.getUserRole() == UserRole.REGIONAL_ADMIN
		);
	}

	private static String normalizeSlug(String raw) {
		return StringUtils.lowerCase(StringUtils.trimToEmpty(raw));
	}

	private static int parseInt(Object raw, int fallback) {
		if (raw instanceof Number n) {
			return n.intValue();
		}
		try {
			return Integer.parseInt(Objects.toString(raw, ""));
		} catch (NumberFormatException ignored) {
			return fallback;
		}
	}
}
