package com.abelium.inatrace.components.whisp.api;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Diagnostics for the Whisp connection, so an operator can tell an unconfigured
 * deployment from an unreachable service without reading the container log.
 */
@Schema(description = "State of the connection with the Whisp API.")
public class ApiWhispIntegrationStatus {

	@Schema(description = "Whether an API key is configured; without one no analysis can be submitted")
	private boolean enabled;

	@Schema(description = "Base URL the backend calls")
	private String baseURL;

	@Schema(description = "Whether analyses can be submitted by AgStack geo id "
			+ "(requires the AgStack credentials); when false, plots are submitted as polygons")
	private boolean geoIdSubmissionAvailable;

	@Schema(description = "Whether the Whisp API answered the reachability call")
	private boolean reachable;

	@Schema(description = "Reason the connection is not usable, when it is not")
	private String message;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getBaseURL() {
		return baseURL;
	}

	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}

	public boolean isGeoIdSubmissionAvailable() {
		return geoIdSubmissionAvailable;
	}

	public void setGeoIdSubmissionAvailable(boolean geoIdSubmissionAvailable) {
		this.geoIdSubmissionAvailable = geoIdSubmissionAvailable;
	}

	public boolean isReachable() {
		return reachable;
	}

	public void setReachable(boolean reachable) {
		this.reachable = reachable;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
