package com.abelium.inatrace.components.whisp;

import com.abelium.inatrace.api.ApiStatus;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.agstack.AgStackClientTokenManager;
import com.abelium.inatrace.components.whisp.api.ApiWhispAnalysisOptions;
import com.abelium.inatrace.components.whisp.api.ApiWhispEnvelope;
import com.abelium.inatrace.components.whisp.api.ApiWhispSubmitGeoIdsRequest;
import com.abelium.inatrace.components.whisp.api.ApiWhispSubmitWktRequest;
import com.abelium.inatrace.types.DeforestationAnalysisStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * HTTP client for the Whisp API (Open Foris / Forest Data Partnership), the service that
 * scores a geometry against the deforestation datasets behind the EUDR indicators.
 *
 * Flow, as Whisp defines it:
 * <ol>
 *   <li>submit a geometry - as an AgStack geo id, or as a WKT polygon - and get a job token;</li>
 *   <li>poll {@code /status/{token}}: 202 while the analysis runs, 200 with a GeoJSON
 *       FeatureCollection when it is done;</li>
 *   <li>persist the result, because Whisp's storage is temporary.</li>
 * </ol>
 *
 * Submitting by geo id additionally needs the AgStack asset registry token, which is why
 * this client shares {@link AgStackClientTokenManager} with the geo id registration.
 */
@Service
public class WhispClientService {

	/**
	 * Submitting only queues the job, but the request still travels to Earth Engine.
	 */
	private static final Duration SUBMIT_TIMEOUT = Duration.ofSeconds(60);

	private static final Duration STATUS_TIMEOUT = Duration.ofSeconds(30);

	/**
	 * A finished FeatureCollection carries every dataset column plus processing
	 * metadata, well past WebClient's 256 KB default.
	 */
	private static final int MAX_RESPONSE_BYTES = 8 * 1024 * 1024;

	private final Logger logger = LoggerFactory.getLogger(WhispClientService.class);

	@Value("${INATrace.whisp.baseURL:https://whisp.openforis.org/api}")
	private String baseURL;

	@Value("${INATrace.whisp.apiKey:}")
	private String apiKey;

	private final AgStackClientTokenManager agStackTokenManager;

	private final ObjectMapper objectMapper;

	private WebClient webClient;

	public WhispClientService(AgStackClientTokenManager agStackTokenManager, ObjectMapper objectMapper) {
		this.agStackTokenManager = agStackTokenManager;
		this.objectMapper = objectMapper;
	}

	@PostConstruct
	void initWebClient() {
		// One client for the whole application: WebClient.create() per call, as the
		// AgStack integration originally did, builds a new connection pool every time.
		this.webClient = WebClient.builder()
				.baseUrl(StringUtils.defaultIfBlank(baseURL, "https://whisp.openforis.org/api"))
				.codecs(c -> c.defaultCodecs().maxInMemorySize(MAX_RESPONSE_BYTES))
				.build();
	}

	/**
	 * Whisp issues its own API keys from the user account page; without one no analysis
	 * endpoint answers, so the whole feature stays switched off.
	 */
	public boolean isEnabled() {
		return StringUtils.isNotBlank(apiKey) && StringUtils.isNotBlank(baseURL);
	}

	public String getBaseURL() {
		return baseURL;
	}

	/**
	 * Submitting by geo id needs the AgStack token on top of the Whisp key. When AgStack
	 * is not configured the plot is analysed by its polygon instead.
	 */
	public boolean isGeoIdSubmissionAvailable() {
		return agStackTokenManager.isEnabled();
	}

	/**
	 * Queues an analysis for already registered AgStack geo ids.
	 */
	public WhispJob submitGeoIds(List<String> geoIds) throws ApiException {

		requireEnabled();

		String geoIdToken = agStackTokenManager.retrieveToken();
		if (geoIdToken == null) {
			throw new ApiException(ApiStatus.ERROR, "Could not obtain the AgStack token required to submit geo ids to Whisp");
		}

		ApiWhispSubmitGeoIdsRequest request = new ApiWhispSubmitGeoIdsRequest();
		request.setGeoIds(geoIds);
		request.setAnalysisOptions(defaultAnalysisOptions());

		return submit("/submit/geo-ids", request, geoIdToken);
	}

	/**
	 * Queues an analysis for a polygon, for plots that have no geo id.
	 */
	public WhispJob submitWkt(String wkt) throws ApiException {

		requireEnabled();

		ApiWhispSubmitWktRequest request = new ApiWhispSubmitWktRequest();
		request.setWkt(wkt);
		request.setAnalysisOptions(defaultAnalysisOptions());

		return submit("/submit/wkt", request, null);
	}

	/**
	 * Reads the current state of a job.
	 */
	public WhispJob fetchJob(String token) throws ApiException {

		requireEnabled();

		RawResponse raw = exchange(webClient.get()
				.uri(uriBuilder -> uriBuilder.path("/status/{token}").build(token))
				.header("x-api-key", apiKey)
				.accept(MediaType.APPLICATION_JSON), STATUS_TIMEOUT, "status");

		WhispJob job = interpret(raw);
		if (job.getToken() == null) {
			job.setToken(token);
		}
		return job;
	}

	/**
	 * The catalogue of datasets and result fields Whisp can report - the analysis schemas
	 * a client needs in order to label the indicators it renders. Public endpoint, no key
	 * required, so it also works as a reachability check.
	 *
	 * Unlike every other endpoint this one answers with CSV, not with the JSON envelope
	 * (verified against the live API on 2026-08-27), so the rows are converted here and
	 * the rest of the backend only ever sees JSON.
	 */
	public JsonNode fetchAnalysisSchemas() throws ApiException {

		RawResponse raw = exchange(webClient.get()
				.uri(uriBuilder -> uriBuilder.path("/result-fields/lookup-datasets").build())
				.accept(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON), STATUS_TIMEOUT, "lookup-datasets");

		if (!raw.statusCode.is2xxSuccessful()) {
			throw new ApiException(ApiStatus.UPSTREAM_HTTP_ERROR,
					"Whisp returned " + raw.statusCode.value() + " for the analysis schemas");
		}

		JsonNode asJson = parse(raw.body);
		if (asJson != null && (asJson.isObject() || asJson.isArray())) {
			return asJson;
		}

		JsonNode asCsv = parseCsv(raw.body);
		if (asCsv == null) {
			throw new ApiException(ApiStatus.UPSTREAM_HTTP_ERROR,
					"Whisp answered the analysis schemas in an unrecognised format");
		}
		return asCsv;
	}

	/**
	 * Turns the dataset CSV into an array of objects keyed by its header row.
	 */
	JsonNode parseCsv(String body) {

		if (StringUtils.isBlank(body)) {
			return null;
		}

		String[] lines = body.split("\\r?\\n");
		if (lines.length < 2) {
			return null;
		}

		List<String> header = splitCsvLine(lines[0]);
		if (header.size() < 2) {
			return null;
		}

		ArrayNode rows = objectMapper.createArrayNode();
		for (int i = 1; i < lines.length; i++) {
			if (StringUtils.isBlank(lines[i])) {
				continue;
			}
			List<String> values = splitCsvLine(lines[i]);
			ObjectNode row = rows.addObject();
			for (int c = 0; c < header.size(); c++) {
				String value = c < values.size() ? values.get(c) : null;
				if (StringUtils.isEmpty(value)) {
					row.putNull(header.get(c));
				} else {
					row.put(header.get(c), value);
				}
			}
		}
		return rows;
	}

	/**
	 * Splits one CSV line, honouring double-quoted fields. Today's catalogue contains no
	 * quotes at all, but a single dataset description with a comma in it would otherwise
	 * silently shift every column after it.
	 */
	private List<String> splitCsvLine(String line) {

		List<String> fields = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		boolean quoted = false;

		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (quoted) {
				if (c == '"') {
					// A doubled quote inside a quoted field is an escaped quote.
					if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
						current.append('"');
						i++;
					} else {
						quoted = false;
					}
				} else {
					current.append(c);
				}
			} else if (c == '"') {
				quoted = true;
			} else if (c == ',') {
				fields.add(current.toString().trim());
				current.setLength(0);
			} else {
				current.append(c);
			}
		}
		fields.add(current.toString().trim());

		return fields;
	}

	private ApiWhispAnalysisOptions defaultAnalysisOptions() {
		ApiWhispAnalysisOptions options = new ApiWhispAnalysisOptions();
		options.setAsync(Boolean.TRUE);
		options.setUnitType("ha");
		return options;
	}

	private void requireEnabled() throws ApiException {
		if (!isEnabled()) {
			throw new ApiException(ApiStatus.INVALID_REQUEST,
					"The Whisp integration is not configured (INATrace.whisp.apiKey is empty)");
		}
	}

	private WhispJob submit(String path, Object body, String geoIdToken) throws ApiException {

		WebClient.RequestHeadersSpec<?> spec = webClient.post()
				.uri(uriBuilder -> uriBuilder.path(path).build())
				.header("x-api-key", apiKey)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(body);

		if (geoIdToken != null) {
			spec = spec.header("x-geoid-token", geoIdToken);
		}

		RawResponse raw = exchange(spec, SUBMIT_TIMEOUT, "submit");

		WhispJob job = interpret(raw);
		if (job.getStatus() == DeforestationAnalysisStatus.FAILED) {
			throw new ApiException(ApiStatus.UPSTREAM_HTTP_ERROR,
					StringUtils.defaultIfBlank(job.getMessage(), "Whisp rejected the submission"));
		}
		if (job.getToken() == null && job.getStatus() != DeforestationAnalysisStatus.COMPLETED) {
			throw new ApiException(ApiStatus.UPSTREAM_HTTP_ERROR, "Whisp did not return an analysis token");
		}
		return job;
	}

	/**
	 * Performs the call and hands back status plus body as text.
	 *
	 * The body is read as a string rather than bound to {@link ApiWhispEnvelope}
	 * directly because an error can also come from something in front of Whisp - a
	 * gateway, a proxy - answering HTML. Binding first would turn that into an opaque
	 * deserialization exception instead of a message naming the status code.
	 */
	private RawResponse exchange(WebClient.RequestHeadersSpec<?> spec, Duration timeout, String operation) throws ApiException {

		try {
			RawResponse response = spec
					.exchangeToMono(clientResponse -> clientResponse
							.bodyToMono(String.class)
							.defaultIfEmpty("")
							.map(body -> new RawResponse(clientResponse.statusCode(), body)))
					.block(timeout);

			if (response == null) {
				throw new ApiException(ApiStatus.UPSTREAM_HTTP_ERROR, "Empty response from Whisp (" + operation + ")");
			}
			return response;

		} catch (ApiException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Whisp {} call failed: {}", operation, e.getMessage());
			throw new ApiException(ApiStatus.UPSTREAM_HTTP_ERROR,
					"Whisp is not reachable (" + operation + "): " + e.getMessage());
		}
	}

	/**
	 * Normalises a raw response into a job state.
	 *
	 * Whisp answers 202 with a progress snapshot and 200 with the finished
	 * FeatureCollection. A submission with {@code async=true} answers with a token, but
	 * the completed shape is handled here too: if a Whisp version ever resolves a small
	 * geometry inline, the result is used instead of polling for a token that will never
	 * exist.
	 */
	WhispJob interpret(RawResponse raw) {

		WhispJob job = new WhispJob();

		JsonNode root = parse(raw.body);
		ApiWhispEnvelope envelope = toEnvelope(root);
		JsonNode data = envelope != null && envelope.getData() != null ? envelope.getData() : root;

		if (envelope != null) {
			job.setMessage(envelope.describeError());
		}

		if (!raw.statusCode.is2xxSuccessful()) {
			job.setStatus(DeforestationAnalysisStatus.FAILED);
			job.setMessage(StringUtils.defaultIfBlank(job.getMessage(),
					"Whisp returned HTTP " + raw.statusCode.value()));
			return job;
		}

		job.setToken(extractToken(data));
		job.setPercent(extractPercent(data));

		JsonNode featureProperties = extractFeatureProperties(data);
		if (featureProperties != null) {
			job.setStatus(DeforestationAnalysisStatus.COMPLETED);
			job.setFeatureProperties(featureProperties);
			// The feature properties are the analysis; the geometry in the enclosing
			// FeatureCollection is the one we sent and is already stored on the plot.
			job.setRawResult(featureProperties.toString());
			return job;
		}

		job.setStatus(job.getPercent() != null
				? DeforestationAnalysisStatus.IN_PROGRESS
				: DeforestationAnalysisStatus.PENDING);
		return job;
	}

	/**
	 * Recognises the {@code {code, message, cause, data}} wrapper. A body that is already
	 * a FeatureCollection is not an envelope, and an error body carries {@code code} and
	 * {@code message} without any {@code data} at all - both shapes were seen live.
	 */
	private ApiWhispEnvelope toEnvelope(JsonNode root) {
		if (root == null || !root.isObject() || root.has("features")) {
			return null;
		}
		if (!root.has("code") && !root.has("message")) {
			return null;
		}
		try {
			return objectMapper.treeToValue(root, ApiWhispEnvelope.class);
		} catch (Exception e) {
			return null;
		}
	}

	private JsonNode parse(String body) {
		if (StringUtils.isBlank(body)) {
			return null;
		}
		try {
			return objectMapper.readTree(body);
		} catch (Exception e) {
			return null;
		}
	}

	private String extractToken(JsonNode data) {
		if (data == null) {
			return null;
		}
		if (data.isTextual()) {
			return StringUtils.trimToNull(data.asText());
		}
		JsonNode token = data.get("token");
		return token != null && token.isTextual() ? StringUtils.trimToNull(token.asText()) : null;
	}

	private Integer extractPercent(JsonNode data) {
		if (data == null || !data.isObject()) {
			return null;
		}
		JsonNode percent = data.get("percent");
		return percent != null && percent.isNumber() ? (int) Math.round(percent.asDouble()) : null;
	}

	/**
	 * A plot is submitted on its own, so the analysis has exactly one feature.
	 */
	private JsonNode extractFeatureProperties(JsonNode data) {
		if (data == null || !data.isObject()) {
			return null;
		}
		JsonNode features = data.get("features");
		if (features == null || !features.isArray() || features.isEmpty()) {
			return null;
		}
		JsonNode properties = features.get(0).get("properties");
		return properties != null && properties.isObject() ? properties : null;
	}

	/**
	 * HTTP status plus body, before any interpretation. Package-private, together with
	 * {@code interpret} and {@code parseCsv}, so the three shapes Whisp answers with can
	 * be tested without a network round trip.
	 */
	static final class RawResponse {

		private final HttpStatusCode statusCode;

		private final String body;

		RawResponse(HttpStatusCode statusCode, String body) {
			this.statusCode = statusCode;
			this.body = body;
		}
	}

}
