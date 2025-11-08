package com.abelium.inatrace.components.agstack;

import com.abelium.inatrace.api.ApiStatus;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.agstack.api.ApiAuthRequest;
import com.abelium.inatrace.components.agstack.api.ApiAuthResponse;
import com.abelium.inatrace.components.agstack.api.ApiRegisterFieldBoundaryErrorResponse;
import com.abelium.inatrace.components.agstack.api.ApiRegisterFieldBoundaryRequest;
import com.abelium.inatrace.components.agstack.api.ApiRegisterFieldBoundaryResponse;
import com.abelium.inatrace.db.entities.common.PlotCoordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
@Service
public class AgStackClientService {

    private static final Logger log = LoggerFactory.getLogger(AgStackClientService.class);
    private static final String FIELD_ALREADY_REGISTERED_MESSAGE = "Threshold matched for already registered Field Boundary(ies)";
    private static final String FIELD_ALREADY_REGISTERED_ALT_MESSAGE = "field already registered previously";
    private static final String FIELD_AREA_EXCEEDED_MESSAGE = "Cannot register a field with Area greater than 1000 acres";

    @Value("${INATrace.agstack.authUrl:https://api.terrapipe.io}")
    private String authUrl;

    @Value("${INATrace.agstack.email:}")
    private String email;

    @Value("${INATrace.agstack.password:}")
    private String password;

    @Value("${INATrace.agstack.baseURL:https://api-ar.agstack.org}")
    private String baseURL;

    private final AtomicReference<String> cachedToken = new AtomicReference<>();
    private final AtomicReference<Instant> tokenExpiry = new AtomicReference<>(Instant.EPOCH);
    private final Clock clock;

    public AgStackClientService() {
        this(Clock.systemUTC());
    }

    AgStackClientService(Clock clock) {
        this.clock = clock;
    }

    @PostConstruct
    public void validateConfiguration() {
        log.info("=== Configuración de AgStack ===");
        log.info("Auth URL: {}", authUrl);
        log.info("Base URL: {}", baseURL);
        log.info("Email configurado: {}", isBlank(email) ? "❌ NO" : "✅ SÍ");
        log.info("Password configurado: {}", isBlank(password) ? "❌ NO" : "✅ SÍ");

        if (isBlank(email) || isBlank(password)) {
            log.warn("⚠️ ADVERTENCIA: Credenciales de AgStack no configuradas. Configure INATRACE_AGSTACK_EMAIL y INATRACE_AGSTACK_PASSWORD");
        } else {
            log.info("✅ Credenciales de AgStack configuradas correctamente");
        }
        log.info("=================================");
    }

    public ApiRegisterFieldBoundaryResponse registerFieldBoundaryResponse(List<PlotCoordinate> plotCoordinates) {
        log.debug("Iniciando registro de field boundary con {} coordenadas", plotCoordinates.size());

        ApiRegisterFieldBoundaryRequest request = new ApiRegisterFieldBoundaryRequest();
        request.setS2Index("8,13");
        request.setResolutionLevel(13);
        request.setThreshold(90);
        request.setReturnS2Indices(Boolean.FALSE);

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < plotCoordinates.size(); i++) {
            stringBuilder
                    .append(plotCoordinates.get(i).getLongitude()).append(" ")
                    .append(plotCoordinates.get(i).getLatitude());
            if (i < plotCoordinates.size() - 1) {
                stringBuilder.append(", ");
            }
        }
        request.setWkt("POLYGON ((" + stringBuilder + "))");
        log.debug("WKT generado: {}", request.getWkt());

        String accessToken = obtainAccessToken();
        log.debug("Token de acceso obtenido exitosamente");

        WebClient webClient = WebClient.create(baseURL);

        return webClient
                .post()
                .uri(uriBuilder -> uriBuilder.path("/register-field-boundary").build())
                .body(Mono.just(request), ApiRegisterFieldBoundaryRequest.class)
                .header(AUTHORIZATION, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(clientResponse -> {
                    HttpStatusCode status = clientResponse.statusCode();

                    if (status.is2xxSuccessful()) {
                        return clientResponse.bodyToMono(ApiRegisterFieldBoundaryResponse.class);
                    }

                    if (status.isSameCodeAs(HttpStatus.BAD_REQUEST)) {
                        return clientResponse.bodyToMono(ApiRegisterFieldBoundaryResponse.class)
                                .flatMap(resp -> {
                                    if (isFieldAlreadyRegistered(resp)) {
                                        return Mono.just(resp);
                                    }
                                    if (resp != null && isAreaExceeded(resp.getFieldAreaAcres())) {
                                        return Mono.error(new ApiException(ApiStatus.INVALID_REQUEST, FIELD_AREA_EXCEEDED_MESSAGE));
                                    }
                                    return Mono.error(new ApiException(ApiStatus.INVALID_REQUEST, resolveErrorMessage(resp)));
                                });
                    }

                    if (status.isSameCodeAs(HttpStatus.INTERNAL_SERVER_ERROR)) {
                        return clientResponse.bodyToMono(ApiRegisterFieldBoundaryErrorResponse.class)
                                .flatMap(error -> Mono.error(new ApiException(ApiStatus.ERROR,
                                        error != null ? error.getError() : "Error desconocido devuelto por AgStack")));
                    }

                    return clientResponse.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> Mono.error(new ApiException(ApiStatus.ERROR, abbreviateBody(body))));
                })
                .map(this::handleBusinessResponse)
                .block();
    }

    private ApiRegisterFieldBoundaryResponse handleBusinessResponse(ApiRegisterFieldBoundaryResponse response) {
        if (response == null) {
            log.error("Respuesta vacía recibida de AgStack");
            throw new ApiException(ApiStatus.ERROR, "Respuesta vacía del servicio AgStack");
        }

        String message = normalize(response.getMessage());

        if (equalsIgnoreCase(message, FIELD_AREA_EXCEEDED_MESSAGE)
                || isAreaExceeded(response.getFieldAreaAcres())) {
            log.warn("Campo rechazado: área excede 1000 acres ({})", response.getFieldAreaAcres());
            throw new ApiException(ApiStatus.INVALID_REQUEST, FIELD_AREA_EXCEEDED_MESSAGE);
        }

        if (isFieldAlreadyRegistered(response)) {
            log.info("Campo ya registrado. IDs coincidentes: {}", response.getMatchedGeoIDs());
            return response;
        }

        log.info("✅ Field boundary registrado exitosamente. Geo ID: {}", response.getGeoID());

        return response;
    }

    private boolean isFieldAlreadyRegistered(ApiRegisterFieldBoundaryResponse response) {
        if (response == null) {
            return false;
        }

        if (response.getMatchedGeoIDs() != null && !response.getMatchedGeoIDs().isEmpty()) {
            return true;
        }

        String message = normalize(response.getMessage());
        return equalsIgnoreCase(message, FIELD_ALREADY_REGISTERED_MESSAGE)
                || equalsIgnoreCase(message, FIELD_ALREADY_REGISTERED_ALT_MESSAGE);
    }

    private boolean isAreaExceeded(BigDecimal fieldAreaAcres) {
        return fieldAreaAcres != null && fieldAreaAcres.compareTo(BigDecimal.valueOf(1000L)) > 0;
    }

    private String resolveErrorMessage(ApiRegisterFieldBoundaryResponse response) {
        String message = response != null ? normalize(response.getMessage()) : "";
        if (!message.isEmpty()) {
            return message;
        }
        if (response != null && response.getMatchedGeoIDs() != null && !response.getMatchedGeoIDs().isEmpty()) {
            return FIELD_ALREADY_REGISTERED_MESSAGE + " - IDs: " + response.getMatchedGeoIDs();
        }
        if (response != null && isAreaExceeded(response.getFieldAreaAcres())) {
            return FIELD_AREA_EXCEEDED_MESSAGE;
        }
        return "Error desconocido devuelto por AgStack";
    }

    private String obtainAccessToken() {
        Instant expiry = tokenExpiry.get();
        String token = cachedToken.get();
        if (token != null && expiry != null && expiry.isAfter(clock.instant().minusSeconds(30))) {
            log.debug("Usando token en caché (expira en {})", expiry);
            return token;
        }

        log.debug("Token expirado o no disponible, solicitando nuevo token...");

        if (isBlank(email) || isBlank(password)) {
            log.error("❌ Credenciales de AgStack no configuradas. Verifique INATRACE_AGSTACK_EMAIL y INATRACE_AGSTACK_PASSWORD");
            throw new ApiException(ApiStatus.ERROR, 
                "Credenciales de AgStack no configuradas. Configure INATRACE_AGSTACK_EMAIL y INATRACE_AGSTACK_PASSWORD en las variables de entorno o application.properties");
        }

        WebClient authClient = WebClient.create(authUrl);

        ApiAuthResponse authResponse = authClient.post()
                .uri(uriBuilder -> uriBuilder.path("/").build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(new ApiAuthRequest(email, password)), ApiAuthRequest.class)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> {
                                    log.error("❌ Autenticación AgStack falló ({}): {}", clientResponse.statusCode(), abbreviateBody(body));
                                    return Mono.error(new ApiException(ApiStatus.UNAUTHORIZED, "Credenciales inválidas para AgStack"));
                                }))
                .onStatus(status -> status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> {
                                    log.error("❌ Error de servidor AgStack ({}): {}", clientResponse.statusCode(), abbreviateBody(body));
                                    return Mono.error(new ApiException(ApiStatus.ERROR, "Fallo en autenticación AgStack"));
                                }))
                .bodyToMono(ApiAuthResponse.class)
                .block();

        if (authResponse == null || isBlank(authResponse.getAccessToken())) {
            log.error("❌ Autenticación AgStack falló: respuesta vacía o sin token");
            throw new ApiException(ApiStatus.ERROR, "Autenticación AgStack no devolvió token");
        }

        cachedToken.set(authResponse.getAccessToken());
        Instant tokenExpiration = authResponse.getExpirationInstant();
        if (tokenExpiration == null) {
            tokenExpiration = clock.instant().plusSeconds(30 * 60); // fallback 30 minutos
        }
        tokenExpiry.set(tokenExpiration);

        log.info("✅ Token de acceso obtenido exitosamente (expira: {})", tokenExpiration);

        return authResponse.getAccessToken();
    }

    private boolean equalsIgnoreCase(String value, String target) {
        return value != null && target != null && value.equalsIgnoreCase(target);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String abbreviateBody(String body) {
        if (body == null) {
            return "";
        }
        String normalized = body.trim();
        if (normalized.length() > 500) {
            return normalized.substring(0, 500) + "... (truncated)";
        }
        return normalized;
    }
}
