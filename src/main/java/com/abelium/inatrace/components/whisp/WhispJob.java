package com.abelium.inatrace.components.whisp;

import com.abelium.inatrace.types.DeforestationAnalysisStatus;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * State of one Whisp analysis job as the client last saw it.
 *
 * Not an API model: it is the client's normalised view over the three shapes Whisp can
 * answer with (a token, a progress snapshot, or the finished FeatureCollection).
 */
public class WhispJob {

	private String token;

	private DeforestationAnalysisStatus status;

	private Integer percent;

	/**
	 * Properties of the first feature of the result, which is the analysed plot. Null
	 * until the job completes.
	 */
	private JsonNode featureProperties;

	/**
	 * The full result body, kept verbatim for the audit trail.
	 */
	private String rawResult;

	private String message;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public DeforestationAnalysisStatus getStatus() {
		return status;
	}

	public void setStatus(DeforestationAnalysisStatus status) {
		this.status = status;
	}

	public Integer getPercent() {
		return percent;
	}

	public void setPercent(Integer percent) {
		this.percent = percent;
	}

	public JsonNode getFeatureProperties() {
		return featureProperties;
	}

	public void setFeatureProperties(JsonNode featureProperties) {
		this.featureProperties = featureProperties;
	}

	public String getRawResult() {
		return rawResult;
	}

	public void setRawResult(String rawResult) {
		this.rawResult = rawResult;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
