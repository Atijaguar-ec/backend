package com.abelium.inatrace.components.bi;

import com.abelium.inatrace.components.bi.api.ApiBiDashboard;
import com.abelium.inatrace.components.bi.api.ApiBiDashboardCatalogResponse;
import com.abelium.inatrace.components.bi.api.ApiBiGuestTokenRequest;
import com.abelium.inatrace.components.bi.api.ApiBiGuestTokenResponse;
import com.abelium.inatrace.components.bi.api.ApiCustomBiDashboardRequest;
import com.abelium.inatrace.components.company.CompanyQueries;
import com.abelium.inatrace.db.entities.company.Company;
import com.abelium.inatrace.security.service.CustomUserDetails;
import com.abelium.inatrace.types.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BiReportsServiceTest {

	@Mock
	private CompanyQueries companyQueries;

	@Mock
	private SupersetClient supersetClient;

	private BiSupersetProperties properties;
	private BiOfficialCatalog officialCatalog;
	private BiReportsService service;

	private CustomUserDetails regularUser;
	private CustomUserDetails regionalAdmin;
	private Company fortalezaCompany;

	@BeforeEach
	void setUp() throws Exception {
		properties = new BiSupersetProperties();
		properties.setInternalUrl("http://inatrace-bi-superset-fortaleza:8088/bi");
		properties.setPublicDomain("https://testinatrace.espam.edu.ec/bi");
		properties.setAllowedPortalOrigin("https://testinatrace.espam.edu.ec");
		properties.setOrganizationSlug("fortaleza");
		properties.setEnvironment("staging");
		properties.setServiceUsername("bi_embed_service");
		properties.setServicePassword("secret-pass");
		properties.setGuestTokenTtlSeconds(300);

		officialCatalog = new BiOfficialCatalog();
		service = new BiReportsService(companyQueries, properties, officialCatalog, supersetClient);

		regularUser = new CustomUserDetails(10L, "op@fv.ec", "Operador", "FV", UserRole.USER);
		regionalAdmin = new CustomUserDetails(20L, "admin@fv.ec", "Admin", "FV", UserRole.REGIONAL_ADMIN);

		fortalezaCompany = new Company();
		fortalezaCompany.setName("Fortaleza del Valle");
		fortalezaCompany.setConfiguration(new LinkedHashMap<>(Map.of("enableQuotaBalance", true)));

		lenient().when(companyQueries.fetchCompany(5L)).thenReturn(fortalezaCompany);
		lenient().when(companyQueries.fetchCompanyIdsForUserAdmin(10L)).thenReturn(List.of());
		lenient().when(companyQueries.fetchCompanyIdsForUserAdmin(20L)).thenReturn(List.of(5L));
	}

	@Test
	void getDashboardCatalog_fortaleza_returns7OfficialAndExcludesComprasCertificadas() throws Exception {
		fortalezaCompany.getConfiguration().put(
				BiReportsService.CUSTOM_BI_DASHBOARDS_KEY,
				List.of(
						Map.of(
								"slug", "unocace-cacao-compras-certificadas-staging",
								"label", "Compras Certificadas",
								"allowedRoles", List.of("USER")
						),
						Map.of(
								"slug", "fv-resumen-ejecutivo",
								"label", "Resumen Ejecutivo",
								"description", "Tablero propio",
								"sortOrder", 120,
								"allowedRoles", List.of("REGIONAL_ADMIN", "SYSTEM_ADMIN")
						),
						Map.of(
								"slug", "fv-arqueo-operativo",
								"label", "Arqueo Operativo",
								"allowedRoles", List.of("USER")
						)
				)
		);

		ApiBiDashboardCatalogResponse userCatalog = service.getDashboardCatalog(5L, regularUser);
		assertEquals("https://testinatrace.espam.edu.ec/bi", userCatalog.supersetDomain);
		assertEquals("fortaleza", userCatalog.organizationSlug);
		assertFalse(userCatalog.studioEnabled);
		assertEquals(5, userCatalog.allowedDatasets.size());
		// 7 official + fv-arqueo-operativo (compras-certificadas is blocked; fv-resumen-ejecutivo is admin-only)
		assertEquals(8, userCatalog.dashboards.size());
		assertTrue(userCatalog.dashboards.stream().noneMatch(d -> d.slug.contains("compras-certificadas")));
		assertTrue(userCatalog.dashboards.stream().noneMatch(d -> d.slug.equals("fv-resumen-ejecutivo")));

		ApiBiDashboardCatalogResponse adminCatalog = service.getDashboardCatalog(5L, regionalAdmin);
		assertTrue(adminCatalog.studioEnabled);
		assertEquals(8, adminCatalog.dashboards.size());
		assertTrue(adminCatalog.dashboards.stream().anyMatch(d -> d.slug.equals("fv-resumen-ejecutivo")));
		assertTrue(adminCatalog.dashboards.stream().noneMatch(d -> d.slug.equals("fv-arqueo-operativo")));
	}

	@Test
	void issueGuestToken_rejectsComprasCertificadasInFortaleza() {
		ApiBiGuestTokenRequest req = new ApiBiGuestTokenRequest();
		req.setCompanyId(5L);
		req.setSlug("fortaleza-cacao-compras-certificadas-staging");

		BiReportException ex = assertThrows(BiReportException.class, () -> service.issueGuestToken(req, regularUser));
		assertEquals(HttpStatus.FORBIDDEN, ex.getHttpStatus());
	}

	@Test
	void issueGuestToken_rejectsMissingPortalDomainInAllowedDomains() throws Exception {
		ApiBiGuestTokenRequest req = new ApiBiGuestTokenRequest();
		req.setCompanyId(5L);
		req.setSlug("fortaleza-cacao-arqueo-bascula-staging");

		when(supersetClient.getDashboardEmbedded("fortaleza-cacao-arqueo-bascula-staging"))
				.thenReturn(new SupersetClient.EmbeddedInfo("uuid-1", List.of("https://otro-dominio.ec")));

		BiReportException ex = assertThrows(BiReportException.class, () -> service.issueGuestToken(req, regularUser));
		assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getHttpStatus());
	}

	@Test
	void issueGuestToken_rejectsUnauthorizedRoleForCustomDashboard() {
		fortalezaCompany.getConfiguration().put(
				BiReportsService.CUSTOM_BI_DASHBOARDS_KEY,
				List.of(Map.of(
						"slug", "fv-solo-admin",
						"label", "Solo Admin",
						"allowedRoles", List.of("SYSTEM_ADMIN")
				))
		);

		ApiBiGuestTokenRequest req = new ApiBiGuestTokenRequest();
		req.setCompanyId(5L);
		req.setSlug("fv-solo-admin");

		BiReportException ex = assertThrows(BiReportException.class, () -> service.issueGuestToken(req, regularUser));
		assertEquals(HttpStatus.FORBIDDEN, ex.getHttpStatus());
	}

	@Test
	void issueGuestToken_returnsEmbeddedUuidAndTokenWhenAuthorized() throws Exception {
		ApiBiGuestTokenRequest req = new ApiBiGuestTokenRequest();
		req.setCompanyId(5L);
		req.setSlug("fortaleza-cacao-arqueo-bascula-staging");

		when(supersetClient.getDashboardEmbedded("fortaleza-cacao-arqueo-bascula-staging"))
				.thenReturn(new SupersetClient.EmbeddedInfo("uuid-fv-1", List.of("https://testinatrace.espam.edu.ec")));
		when(supersetClient.createGuestToken("uuid-fv-1", regularUser)).thenReturn("guest.jwt.token");

		ApiBiGuestTokenResponse res = service.issueGuestToken(req, regularUser);
		assertEquals("https://testinatrace.espam.edu.ec/bi", res.supersetDomain);
		assertEquals("uuid-fv-1", res.embeddedUuid);
		assertEquals("guest.jwt.token", res.guestToken);
		assertEquals(300, res.expiresInSeconds);
	}

	@Test
	void issueGuestToken_returns503WhenSupersetUnconfigured() {
		properties.setServicePassword("");
		ApiBiGuestTokenRequest req = new ApiBiGuestTokenRequest();
		req.setCompanyId(5L);
		req.setSlug("fortaleza-cacao-arqueo-bascula-staging");

		BiReportException ex = assertThrows(BiReportException.class, () -> service.issueGuestToken(req, regularUser));
		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getHttpStatus());
	}

	@Test
	void registerCustomDashboard_rejectsReservedPrefix() {
		ApiCustomBiDashboardRequest req = validCustomRequest("fortaleza-cacao-mi-tablero");
		BiReportException ex = assertThrows(BiReportException.class,
				() -> service.registerCustomDashboard(req, regionalAdmin));
		assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getHttpStatus());
	}

	@Test
	void registerCustomDashboard_rejectsUnauthorizedDatasetOutsideFortalezaBiViews() throws Exception {
		ApiCustomBiDashboardRequest req = validCustomRequest("fv-custom-report");
		when(supersetClient.getDashboardMetadata("fv-custom-report"))
				.thenReturn(new SupersetClient.DashboardMetadata(
						99L,
						"fv-custom-report",
						Set.of("public.stockorder", "bi.rpt_cacao_beneficio_fortaleza"),
						1,
						true
				));

		BiReportException ex = assertThrows(BiReportException.class,
				() -> service.registerCustomDashboard(req, regionalAdmin));
		assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getHttpStatus());
		assertTrue(ex.getMessage().contains("public.stockorder"));
	}

	@Test
	void registerCustomDashboard_rejectsChartsWithoutQueryContext() throws Exception {
		ApiCustomBiDashboardRequest req = validCustomRequest("fv-custom-report");
		when(supersetClient.getDashboardMetadata("fv-custom-report"))
				.thenReturn(new SupersetClient.DashboardMetadata(
						99L,
						"fv-custom-report",
						Set.of("bi.rpt_cacao_beneficio_fortaleza"),
						2,
						false
				));

		BiReportException ex = assertThrows(BiReportException.class,
				() -> service.registerCustomDashboard(req, regionalAdmin));
		assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getHttpStatus());
	}

	@Test
	void registerCustomDashboard_savesValidDashboardAndPreservesExistingConfig() throws Exception {
		ApiCustomBiDashboardRequest req = validCustomRequest("fv-custom-report");
		when(supersetClient.getDashboardMetadata("fv-custom-report"))
				.thenReturn(new SupersetClient.DashboardMetadata(
						99L,
						"fv-custom-report",
						Set.of("bi.rpt_cacao_beneficio_fortaleza"),
						1,
						true
				));
		when(supersetClient.getDashboardEmbedded("fv-custom-report"))
				.thenReturn(new SupersetClient.EmbeddedInfo("uuid-custom", List.of("https://testinatrace.espam.edu.ec")));

		ApiBiDashboard saved = service.registerCustomDashboard(req, regionalAdmin);
		assertEquals("fv-custom-report", saved.slug);
		assertEquals(Boolean.TRUE, fortalezaCompany.getConfiguration().get("enableQuotaBalance"));
		assertTrue(fortalezaCompany.getConfiguration().get(BiReportsService.CUSTOM_BI_DASHBOARDS_KEY) instanceof List<?>);
	}

	@Test
	void removeCustomDashboard_forbidsOfficialDashboard() {
		BiReportException ex = assertThrows(BiReportException.class,
				() -> service.removeCustomDashboard(5L, "fortaleza-cacao-arqueo-bascula-staging", regionalAdmin));
		assertEquals(HttpStatus.FORBIDDEN, ex.getHttpStatus());
	}

	@Test
	void supersetClient_renewsJwtOn401AndForwardsCsrfCookieForGuestToken() throws Exception {
		AtomicInteger loginCalls = new AtomicInteger();
		AtomicInteger csrfCalls = new AtomicInteger();
		AtomicInteger guestTokenCalls = new AtomicInteger();
		List<String> observedCookies = new ArrayList<>();

		ExchangeFunction exchange = request -> {
			String path = request.url().getPath();
			if (path.endsWith("/api/v1/security/login")) {
				int call = loginCalls.incrementAndGet();
				return jsonResponse(HttpStatus.OK, "{\"access_token\":\"jwt-" + call + "\"}", null);
			}
			if (path.endsWith("/api/v1/security/csrf_token/")) {
				csrfCalls.incrementAndGet();
				String auth = request.headers().getFirst(HttpHeaders.AUTHORIZATION);
				if ("Bearer jwt-1".equals(auth)) {
					return jsonResponse(HttpStatus.UNAUTHORIZED, "{\"msg\":\"Token expired\"}", null);
				}
				return jsonResponse(HttpStatus.OK, "{\"result\":\"csrf-abc\"}", "session=sess-xyz; Path=/; HttpOnly; Secure");
			}
			if (path.endsWith("/api/v1/security/guest_token/")) {
				guestTokenCalls.incrementAndGet();
				observedCookies.add(request.headers().getFirst(HttpHeaders.COOKIE));
				assertEquals("csrf-abc", request.headers().getFirst("X-CSRFToken"));
				return jsonResponse(HttpStatus.OK, "{\"token\":\"guest-final-jwt\"}", null);
			}
			return jsonResponse(HttpStatus.NOT_FOUND, "{}", null);
		};

		SupersetClient client = new SupersetClient(
				WebClient.builder().exchangeFunction(exchange),
				properties,
				new ObjectMapper()
		);

		String token = client.createGuestToken("uuid-fv-1", regularUser);
		assertEquals("guest-final-jwt", token);
		assertEquals(2, loginCalls.get());
		assertEquals(2, csrfCalls.get());
		assertEquals(1, guestTokenCalls.get());
		assertEquals(List.of("session=sess-xyz"), observedCookies);
	}

	@Test
	void metricsRecordGuestTokenSuccessAndForbiddenOutcomes() throws Exception {
		SimpleMeterRegistry registry = new SimpleMeterRegistry();
		BiReportsService meteredService = new BiReportsService(
				companyQueries, properties, officialCatalog, supersetClient, registry
		);

		when(supersetClient.getDashboardEmbedded("fortaleza-cacao-arqueo-bascula-staging"))
				.thenReturn(new SupersetClient.EmbeddedInfo("uuid-1", List.of("https://testinatrace.espam.edu.ec")));
		when(supersetClient.createGuestToken("uuid-1", regularUser)).thenReturn("guest-jwt");

		ApiBiGuestTokenRequest validReq = new ApiBiGuestTokenRequest();
		validReq.setCompanyId(5L);
		validReq.setSlug("fortaleza-cacao-arqueo-bascula-staging");
		ApiBiGuestTokenResponse resp = meteredService.issueGuestToken(validReq, regularUser);
		assertEquals("guest-jwt", resp.guestToken);

		ApiBiGuestTokenRequest forbiddenReq = new ApiBiGuestTokenRequest();
		forbiddenReq.setCompanyId(5L);
		forbiddenReq.setSlug("fortaleza-cacao-compras-certificadas-staging");
		assertThrows(BiReportException.class, () -> meteredService.issueGuestToken(forbiddenReq, regularUser));

		assertEquals(1.0, registry.get("inatrace.bi.guest_token.requests")
				.tag("organization", "fortaleza")
				.tag("outcome", "success")
				.counter().count());
		assertEquals(1.0, registry.get("inatrace.bi.guest_token.requests")
				.tag("organization", "fortaleza")
				.tag("outcome", "forbidden")
				.counter().count());
	}

	private static Mono<ClientResponse> jsonResponse(HttpStatus status, String body, String setCookie) {
		ClientResponse.Builder builder = ClientResponse.create(status)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.body(body);
		if (setCookie != null) {
			builder.header(HttpHeaders.SET_COOKIE, setCookie);
		}
		return Mono.just(builder.build());
	}

	private static ApiCustomBiDashboardRequest validCustomRequest(String slug) {
		ApiCustomBiDashboardRequest req = new ApiCustomBiDashboardRequest();
		req.setCompanyId(5L);
		req.setSlug(slug);
		req.setLabel("Mi Reporte FV");
		req.setDescription("Reporte personalizado");
		req.setAllowedRoles(List.of("USER", "REGIONAL_ADMIN"));
		return req;
	}
}
