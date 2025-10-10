package com.abelium.inatrace.components.agstack;

import com.abelium.inatrace.api.ApiStatus;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.agstack.api.ApiAuthRequest;
import com.abelium.inatrace.components.agstack.api.ApiAuthResponse;
import com.abelium.inatrace.components.agstack.api.ApiRegisterFieldBoundaryErrorResponse;
import com.abelium.inatrace.components.agstack.api.ApiRegisterFieldBoundaryRequest;
import com.abelium.inatrace.components.agstack.api.ApiRegisterFieldBoundaryResponse;
import com.abelium.inatrace.db.entities.common.PlotCoordinate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
@Service
public class AgStackClientService {

    private static final String FIELD_ALREADY_REGISTERED_MESSAGE = "Threshold matched for already registered Field Boundary(ies)";
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

    public ApiRegisterFieldBoundaryResponse registerFieldBoundaryResponse(List<PlotCoordinate> plotCoordinates) {

        ApiRegisterFieldBoundaryRequest request = new ApiRegisterFieldBoundaryRequest();
        request.setS2Index("8, 13");

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

        String accessToken = obtainAccessToken();

        WebClient webClient = WebClient.create(baseURL);

        return webClient
                .post()
                .uri(uriBuilder -> uriBuilder.path("/register-field-boundary").build())
                .body(Mono.just(request), ApiRegisterFieldBoundaryRequest.class)
                .header(AUTHORIZATION, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals,
                        clientResponse -> clientResponse
                                .bodyToMono(ApiRegisterFieldBoundaryErrorResponse.class)
                                .flatMap(error -> Mono.error(new ApiException(ApiStatus.ERROR, error.getError()))))
                .onStatus(HttpStatus.BAD_REQUEST::equals,
                        clientResponse -> clientResponse
                                .bodyToMono(ApiRegisterFieldBoundaryResponse.class)
                                .flatMap(resp -> Mono.error(new ApiException(ApiStatus.INVALID_REQUEST, resolveErrorMessage(resp)))) )
                .bodyToMono(ApiRegisterFieldBoundaryResponse.class)
                .map(this::handleBusinessResponse)
                .block();
    }

    private ApiRegisterFieldBoundaryResponse handleBusinessResponse(ApiRegisterFieldBoundaryResponse response) {
        if (response == null) {
            throw new ApiException(ApiStatus.ERROR, "Respuesta vacía del servicio AgStack");
        }

        String message = normalize(response.getMessage());

        if (equalsIgnoreCase(message, FIELD_AREA_EXCEEDED_MESSAGE)
                || isAreaExceeded(response.getFieldAreaAcres())) {
            throw new ApiException(ApiStatus.INVALID_REQUEST, FIELD_AREA_EXCEEDED_MESSAGE);
        }

        if (equalsIgnoreCase(message, FIELD_ALREADY_REGISTERED_MESSAGE)
                || (response.getMatchedGeoIDs() != null && !response.getMatchedGeoIDs().isEmpty())) {
            throw new ApiException(ApiStatus.INVALID_REQUEST, resolveErrorMessage(response));
        }

        return response;
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
            return token;
        }

        if (isBlank(email) || isBlank(password)) {
            throw new ApiException(ApiStatus.ERROR, "Credenciales de AgStack no configuradas");
        }

        WebClient authClient = WebClient.create(authUrl);

        ApiAuthResponse authResponse = authClient.post()
                .uri(uriBuilder -> uriBuilder.path("/").build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(new ApiAuthRequest(email, password)), ApiAuthRequest.class)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), 
                        clientResponse -> Mono.error(new ApiException(ApiStatus.UNAUTHORIZED, "Credenciales inválidas para AgStack")))
                .onStatus(status -> status.is5xxServerError(), 
                        clientResponse -> Mono.error(new ApiException(ApiStatus.ERROR, "Fallo en autenticación AgStack")))
                .bodyToMono(ApiAuthResponse.class)
                .block();

        if (authResponse == null || isBlank(authResponse.getAccessToken())) {
            throw new ApiException(ApiStatus.ERROR, "Autenticación AgStack no devolvió token");
        }

        cachedToken.set(authResponse.getAccessToken());
        Instant tokenExpiration = authResponse.getExpirationInstant();
        if (tokenExpiration == null) {
            tokenExpiration = clock.instant().plusSeconds(30 * 60); // fallback 30 minutos
        }
        tokenExpiry.set(tokenExpiration);

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
}
