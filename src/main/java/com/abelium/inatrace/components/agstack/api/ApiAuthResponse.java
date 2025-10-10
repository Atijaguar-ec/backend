package com.abelium.inatrace.components.agstack.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class ApiAuthResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("exp")
    private Long expiresAtEpoch;

    @JsonProperty("expires_in")
    private Long expiresInSeconds;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getExpiresAtEpoch() {
        return expiresAtEpoch;
    }

    public void setExpiresAtEpoch(Long expiresAtEpoch) {
        this.expiresAtEpoch = expiresAtEpoch;
    }

    public Long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(Long expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }

    public Instant getExpirationInstant() {
        if (expiresAtEpoch != null) {
            return Instant.ofEpochSecond(expiresAtEpoch);
        }
        if (expiresInSeconds != null) {
            return Instant.now().plusSeconds(expiresInSeconds);
        }
        return null;
    }
}
