package com.abelium.inatrace.components.bi;

import com.abelium.inatrace.api.ApiStatus;
import com.abelium.inatrace.security.service.CustomUserDetails;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Internal read-only + guest-token HTTP client for Apache Superset 6.0.0.
 * Reuses the shared WebClient timeout configuration and authenticates strictly
 * with the minimal bi_embed_service account.
 */
@Component
public class SupersetClient {

	private static final Logger logger = LoggerFactory.getLogger(SupersetClient.class);
	private static final Duration TOKEN_MAX_AGE = Duration.ofMinutes(15);

	private final WebClient.Builder webClientBuilder;
	private final BiSupersetProperties properties;
	private final ObjectMapper objectMapper;
	private final ReentrantLock tokenLock = new ReentrantLock();

	private volatile String cachedAccessToken;
	private volatile Instant tokenAcquiredAt;

	@Autowired
	public SupersetClient(
			WebClient.Builder webClientBuilder,
			BiSupersetProperties properties,
			ObjectMapper objectMapper
	) {
		this.webClientBuilder = webClientBuilder;
		this.properties = properties;
		this.objectMapper = objectMapper;
	}

	public record EmbeddedInfo(String uuid, List<String> allowedDomains) {
	}

	public record DashboardMetadata(
			long dashboardId,
			String slug,
			Set<String> datasetKeys,
			int chartCount,
			boolean allChartsHaveQueryContext
	) {
	}

	public EmbeddedInfo getDashboardEmbedded(String slug) throws BiReportException {
		properties.requireConfigured();
		JsonNode root = executeGetWithAuthRetry("/api/v1/dashboard/" + slug + "/embedded",
				"El tablero '" + slug + "' no tiene embebido activado en Superset.");
		JsonNode result = root.path("result");
		String uuid = result.path("uuid").asText("");
		if (StringUtils.isBlank(uuid)) {
			throw new BiReportException(
					HttpStatus.NOT_FOUND,
					ApiStatus.NOT_FOUND,
					"El tablero '" + slug + "' no tiene un UUID embebido configurado."
			);
		}
		List<String> allowedDomains = new ArrayList<>();
		JsonNode domainsNode = result.path("allowed_domains");
		if (domainsNode.isArray()) {
			domainsNode.forEach(item -> {
				String d = StringUtils.removeEnd(StringUtils.trimToEmpty(item.asText("")), "/");
				if (!d.isEmpty()) {
					allowedDomains.add(d);
				}
			});
		}
		return new EmbeddedInfo(uuid, allowedDomains);
	}

	public DashboardMetadata getDashboardMetadata(String slug) throws BiReportException {
		properties.requireConfigured();
		JsonNode dashRoot = executeGetWithAuthRetry("/api/v1/dashboard/" + slug,
				"El tablero '" + slug + "' no existe en Superset.");
		JsonNode dashResult = dashRoot.path("result");
		long dashboardId = dashResult.path("id").asLong(-1L);
		if (dashboardId <= 0) {
			throw new BiReportException(HttpStatus.NOT_FOUND, ApiStatus.NOT_FOUND,
					"El tablero '" + slug + "' no existe en Superset.");
		}

		JsonNode dsRoot = executeGetWithAuthRetry("/api/v1/dashboard/" + slug + "/datasets",
				"No se pudieron consultar los datasets del tablero '" + slug + "'.");
		Set<String> datasetKeys = new LinkedHashSet<>();
		JsonNode dsResult = dsRoot.path("result");
		if (dsResult.isArray()) {
			for (JsonNode ds : dsResult) {
				String key = extractDatasetKey(ds);
				if (StringUtils.isNotBlank(key)) {
					datasetKeys.add(key);
				}
			}
		}

		JsonNode chartsRoot = executeGetWithAuthRetry("/api/v1/dashboard/" + slug + "/charts",
				"No se pudieron consultar los gráficos del tablero '" + slug + "'.");
		JsonNode chartsResult = chartsRoot.path("result");
		int chartCount = 0;
		boolean allHaveQueryContext = true;
		if (chartsResult.isArray()) {
			for (JsonNode chart : chartsResult) {
				chartCount++;
				if (!hasQueryContext(chart)) {
					long chartId = chart.path("id").asLong(-1L);
					if (chartId <= 0 || !fetchChartHasQueryContext(chartId)) {
						allHaveQueryContext = false;
					}
				}
			}
		}

		return new DashboardMetadata(dashboardId, slug, datasetKeys, chartCount, allHaveQueryContext);
	}

	public String createGuestToken(String embeddedUuid, CustomUserDetails userDetails) throws BiReportException {
		properties.requireConfigured();
		String firstName = StringUtils.defaultIfBlank(userDetails.getName(), userDetails.getUsername());
		String lastName = StringUtils.defaultString(userDetails.getSurname(), "");
		Map<String, Object> body = Map.of(
				"user", Map.of(
						"username", userDetails.getUsername(),
						"first_name", firstName,
						"last_name", lastName
				),
				"resources", List.of(Map.of("type", "dashboard", "id", embeddedUuid)),
				"rls", List.of()
		);

		try {
			return postGuestTokenWithAuth(body, true);
		} catch (WebClientResponseException.Unauthorized exc) {
			invalidateToken();
			try {
				return postGuestTokenWithAuth(body, false);
			} catch (WebClientResponseException retryExc) {
				throw mapUpstreamResponseException(retryExc, "No se pudo emitir el guest token en Superset.");
			}
		} catch (WebClientResponseException exc) {
			throw mapUpstreamResponseException(exc, "No se pudo emitir el guest token en Superset.");
		} catch (WebClientRequestException exc) {
			throw upstreamUnavailable("No hay conexión con Superset.", exc);
		}
	}

	private String postGuestTokenWithAuth(Map<String, Object> body, boolean retryOnUnauthorized) throws BiReportException {
		String token = getValidAccessToken();
		WebClient client = buildClient();
		ResponseEntity<String> csrfResp = client.get()
				.uri("/api/v1/security/csrf_token/")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
				.retrieve()
				.toEntity(String.class)
				.block();

		if (csrfResp == null || csrfResp.getBody() == null) {
			throw upstreamUnavailable("Superset no devolvió token CSRF.", null);
		}

		String csrfToken = parseJson(csrfResp.getBody()).path("result").asText("");
		List<String> cookiePairs = extractSessionCookiePairs(csrfResp.getHeaders().get(HttpHeaders.SET_COOKIE));

		WebClient.RequestBodySpec req = client.post()
				.uri("/api/v1/security/guest_token/")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
				.header(HttpHeaders.REFERER, properties.normalizedInternalUrl() + "/")
				.contentType(MediaType.APPLICATION_JSON);

		if (StringUtils.isNotBlank(csrfToken)) {
			req = req.header("X-CSRFToken", csrfToken);
		}
		if (!cookiePairs.isEmpty()) {
			req = req.header(HttpHeaders.COOKIE, String.join("; ", cookiePairs));
		}

		String respBody = req.bodyValue(body)
				.retrieve()
				.bodyToMono(String.class)
				.block();

		String guestToken = parseJson(respBody).path("token").asText("");
		if (StringUtils.isBlank(guestToken)) {
			throw upstreamUnavailable("Superset respondió sin token de invitado.", null);
		}
		return guestToken;
	}

	private boolean fetchChartHasQueryContext(long chartId) throws BiReportException {
		JsonNode root = executeGetWithAuthRetry("/api/v1/chart/" + chartId,
				"No se pudo verificar el gráfico " + chartId + " en Superset.");
		return hasQueryContext(root.path("result"));
	}

	private JsonNode executeGetWithAuthRetry(String path, String notFoundMessage) throws BiReportException {
		try {
			return executeGet(path);
		} catch (WebClientResponseException.Unauthorized exc) {
			invalidateToken();
			try {
				return executeGet(path);
			} catch (WebClientResponseException retryExc) {
				throw mapUpstreamResponseException(retryExc, notFoundMessage);
			}
		} catch (WebClientResponseException exc) {
			throw mapUpstreamResponseException(exc, notFoundMessage);
		} catch (WebClientRequestException exc) {
			throw upstreamUnavailable("No hay conexión con Superset.", exc);
		}
	}

	private JsonNode executeGet(String path) throws BiReportException {
		String token = getValidAccessToken();
		String raw = buildClient().get()
				.uri(path)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
				.retrieve()
				.bodyToMono(String.class)
				.block();
		return parseJson(raw);
	}

	private String getValidAccessToken() throws BiReportException {
		String token = cachedAccessToken;
		Instant acquired = tokenAcquiredAt;
		if (StringUtils.isNotBlank(token) && acquired != null && Instant.now().isBefore(acquired.plus(TOKEN_MAX_AGE))) {
			return token;
		}
		tokenLock.lock();
		try {
			if (StringUtils.isNotBlank(cachedAccessToken) && tokenAcquiredAt != null
					&& Instant.now().isBefore(tokenAcquiredAt.plus(TOKEN_MAX_AGE))) {
				return cachedAccessToken;
			}
			Map<String, Object> loginBody = Map.of(
					"username", properties.getServiceUsername(),
					"password", properties.getServicePassword(),
					"provider", "db",
					"refresh", true
			);
			String raw = buildClient().post()
					.uri("/api/v1/security/login")
					.contentType(MediaType.APPLICATION_JSON)
					.bodyValue(loginBody)
					.retrieve()
					.bodyToMono(String.class)
					.block();
			String accessToken = parseJson(raw).path("access_token").asText("");
			if (StringUtils.isBlank(accessToken)) {
				throw upstreamUnavailable("Superset no devolvió access_token para la cuenta de servicio BI.", null);
			}
			cachedAccessToken = accessToken;
			tokenAcquiredAt = Instant.now();
			return accessToken;
		} catch (WebClientResponseException exc) {
			throw upstreamUnavailable("Falló la autenticación de la cuenta de servicio BI en Superset (HTTP "
					+ exc.getStatusCode().value() + ").", exc);
		} catch (WebClientRequestException exc) {
			throw upstreamUnavailable("No hay conexión con Superset al autenticar la cuenta de servicio BI.", exc);
		} finally {
			tokenLock.unlock();
		}
	}

	private void invalidateToken() {
		tokenLock.lock();
		try {
			cachedAccessToken = null;
			tokenAcquiredAt = null;
		} finally {
			tokenLock.unlock();
		}
	}

	private WebClient buildClient() {
		return webClientBuilder.clone().baseUrl(properties.normalizedInternalUrl()).build();
	}

	private JsonNode parseJson(String raw) throws BiReportException {
		if (StringUtils.isBlank(raw)) {
			return objectMapper.createObjectNode();
		}
		try {
			return objectMapper.readTree(raw);
		} catch (Exception exc) {
			throw upstreamUnavailable("Respuesta JSON inválida desde Superset.", exc);
		}
	}

	private static String extractDatasetKey(JsonNode ds) {
		String schema = StringUtils.trimToEmpty(ds.path("schema").asText(""));
		String tableName = StringUtils.trimToEmpty(ds.path("table_name").asText(""));
		if (StringUtils.isNotBlank(schema) && StringUtils.isNotBlank(tableName)) {
			return (schema + "." + tableName).toLowerCase();
		}
		String datasourceName = StringUtils.trimToEmpty(ds.path("datasource_name").asText(""));
		if (datasourceName.contains(".")) {
			return datasourceName.toLowerCase();
		}
		if (StringUtils.isNotBlank(tableName)) {
			return tableName.toLowerCase();
		}
		return "";
	}

	private static boolean hasQueryContext(JsonNode chart) {
		JsonNode qc = chart.path("query_context");
		return !qc.isMissingNode() && !qc.isNull() && StringUtils.isNotBlank(qc.asText(""));
	}

	private static List<String> extractSessionCookiePairs(List<String> setCookies) {
		List<String> pairs = new ArrayList<>();
		if (setCookies == null) {
			return pairs;
		}
		for (String header : setCookies) {
			if (StringUtils.isBlank(header)) {
				continue;
			}
			int semi = header.indexOf(';');
			String pair = (semi >= 0 ? header.substring(0, semi) : header).trim();
			if (!pair.isEmpty()) {
				pairs.add(pair);
			}
		}
		return pairs;
	}

	private BiReportException mapUpstreamResponseException(WebClientResponseException exc, String notFoundMessage) {
		int code = exc.getStatusCode().value();
		if (code == 404) {
			return new BiReportException(HttpStatus.NOT_FOUND, ApiStatus.NOT_FOUND, notFoundMessage);
		}
		if (code == 401 || code == 403) {
			return upstreamUnavailable(
					"La cuenta de servicio de Superset no tiene permisos suficientes (HTTP " + code + ").", exc);
		}
		return upstreamUnavailable("Error de Superset (HTTP " + code + ").", exc);
	}

	private BiReportException upstreamUnavailable(String message, Throwable cause) {
		if (cause != null) {
			logger.warn("Superset gateway error: {} ({})", message, cause.getMessage());
		} else {
			logger.warn("Superset gateway error: {}", message);
		}
		return new BiReportException(HttpStatus.SERVICE_UNAVAILABLE, ApiStatus.UPSTREAM_HTTP_ERROR, message);
	}
}
