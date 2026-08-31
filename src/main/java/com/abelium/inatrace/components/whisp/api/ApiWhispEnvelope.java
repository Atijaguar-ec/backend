package com.abelium.inatrace.components.whisp.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The envelope every Whisp endpoint answers with: {@code {code, message, cause, data}}.
 *
 * {@code code} and {@code data} are kept as raw nodes on purpose. Whisp types them as
 * "anything" and the payload of {@code data} differs per endpoint and per job state
 * (a token, a progress snapshot, or a GeoJSON FeatureCollection), so binding them to a
 * fixed class would turn a Whisp release into a deserialization failure here.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Raw response envelope of the Whisp API.")
public class ApiWhispEnvelope {

	private JsonNode code;

	private String message;

	private String cause;

	private JsonNode data;

	public JsonNode getCode() {
		return code;
	}

	public void setCode(JsonNode code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getCause() {
		return cause;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}

	public JsonNode getData() {
		return data;
	}

	public void setData(JsonNode data) {
		this.data = data;
	}

	/**
	 * Best-effort human readable description of a failure, for the error column and the
	 * log.
	 */
	public String describeError() {
		StringBuilder sb = new StringBuilder();
		if (message != null) {
			sb.append(message);
		}
		if (cause != null && !cause.isBlank()) {
			if (sb.length() > 0) {
				sb.append(" - ");
			}
			sb.append(cause);
		}
		if (sb.length() == 0 && code != null) {
			sb.append(code.asText());
		}
		return sb.toString();
	}

}
