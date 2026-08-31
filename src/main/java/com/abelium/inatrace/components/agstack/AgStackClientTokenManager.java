package com.abelium.inatrace.components.agstack;

import com.abelium.inatrace.api.ApiStatus;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.agstack.api.ApiLoginErrorResponse;
import com.abelium.inatrace.components.agstack.api.ApiLoginRequest;
import com.abelium.inatrace.components.agstack.api.ApiLoginResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

/**
 * Holds the AgStack asset registry access token, refreshing it when it ages out.
 *
 * The token is also what Whisp requires in its {@code x-geoid-token} header when an
 * analysis is submitted by geo id, so this manager is shared by both integrations.
 */
@Service
public class AgStackClientTokenManager {

    private static final long TOKEN_REFRESH_SECONDS = 3 * 3600;

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private final Logger logger = LoggerFactory.getLogger(AgStackClientTokenManager.class);

    @Value("${INATrace.agstack.email:}")
    private String email;

    @Value("${INATrace.agstack.password:}")
    private String password;

    @Value("${INATrace.agstack.loginBaseURL:}")
    private String baseURL;

    private volatile String token;

    private volatile Instant lastUpdated;

    /**
     * Whether the integration has credentials configured. Deployments without AgStack
     * secrets must not attempt the call: the login would fail on every plot that is
     * saved and turn a missing optional integration into a stream of errors in the log.
     */
    public boolean isEnabled() {
        return StringUtils.isNotBlank(email) && StringUtils.isNotBlank(password) && StringUtils.isNotBlank(baseURL);
    }

    /**
     * @return a valid access token, or {@code null} when the integration is disabled or
     *         the login failed
     */
    public String retrieveToken() {

        if (!isEnabled()) {
            return null;
        }

        Instant now = Instant.now();
        if (!shouldRefreshToken(now)) {
            return token;
        }

        // Synchronized so that a burst of plot saves does not fire one login per request.
        // The state is re-checked inside the lock: whoever waited here now finds a fresh
        // token and returns it.
        synchronized (this) {
            Instant insideLock = Instant.now();
            if (shouldRefreshToken(insideLock)) {
                refreshToken(insideLock);
            }
            return token;
        }
    }

    private void refreshToken(Instant instant) {

        try {
            ApiLoginResponse response = this.login(email, password);
            // The API answers 200 with an empty body on some failure modes, so neither
            // the response nor the token inside it can be dereferenced blindly.
            if (response == null || StringUtils.isBlank(response.getAccessToken())) {
                logger.error("AgStack login returned no access token; keeping the previous one");
                return;
            }
            this.token = response.getAccessToken();
            this.lastUpdated = instant;
        } catch (Exception e) {
            logger.error("AgStack login failed: {}", e.getMessage());
        }
    }

    private boolean shouldRefreshToken(Instant instant) {
        if (this.token == null || this.lastUpdated == null) {
            return true;
        }
        return Duration.between(this.lastUpdated, instant).toSeconds() > TOKEN_REFRESH_SECONDS;
    }

    private ApiLoginResponse login(String username, String password) {

        ApiLoginRequest apiLoginRequest = new ApiLoginRequest();
        apiLoginRequest.setEmail(username);
        apiLoginRequest.setPassword(password);

        WebClient webClient = WebClient.create(baseURL);
        return webClient
                .post()
                .uri(uriBuilder -> uriBuilder.path("/login").build())
                .body(Mono.just(apiLoginRequest), ApiLoginRequest.class)
                .header("User-Agent", "Postman") // Fix for API to return JSON
                .header("X-FROM-ASSET-REGISTRY", "True")
                .header("Content-Type", "application/json")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(
                        HttpStatus.INTERNAL_SERVER_ERROR::equals,
                        clientResponse -> clientResponse
                                .bodyToMono(ApiLoginErrorResponse.class)
                                .flatMap(error -> Mono.error(new ApiException(ApiStatus.ERROR, error.getMessage()))))
                .onStatus(HttpStatus.BAD_REQUEST::equals,
                        clientResponse -> clientResponse
                                .bodyToMono(ApiLoginErrorResponse.class)
                                .flatMap(error -> Mono.error(new ApiException(ApiStatus.INVALID_REQUEST, error.getMessage()))))
                .bodyToMono(ApiLoginResponse.class)
                // Without this the calling request thread waits forever when the asset
                // registry stops answering, and the connection pool drains.
                .block(REQUEST_TIMEOUT);
    }
}
