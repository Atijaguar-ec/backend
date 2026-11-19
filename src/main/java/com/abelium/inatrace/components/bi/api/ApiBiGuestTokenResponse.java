package com.abelium.inatrace.components.bi.api;

import io.swagger.v3.oas.annotations.media.Schema;

public class ApiBiGuestTokenResponse {

	@Schema(description = "Public Superset domain including /bi prefix")
	public String supersetDomain;

	@Schema(description = "Superset embedded dashboard UUID")
	public String embeddedUuid;

	@Schema(description = "Short-lived Superset guest JWT token")
	public String guestToken;

	@Schema(description = "Token lifetime in seconds")
	public int expiresInSeconds;

	public ApiBiGuestTokenResponse() {
	}

	public ApiBiGuestTokenResponse(String supersetDomain, String embeddedUuid, String guestToken, int expiresInSeconds) {
		this.supersetDomain = supersetDomain;
		this.embeddedUuid = embeddedUuid;
		this.guestToken = guestToken;
		this.expiresInSeconds = expiresInSeconds;
	}
}
