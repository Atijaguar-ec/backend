package com.abelium.inatrace.components.agstack.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "API model for AgStack register field boundary call.")
public class ApiRegisterFieldBoundaryResponse {

	@JsonProperty("Geo Id")
	private String geoID;

	@JsonProperty("matched geo ids")
	private List<String> matchedGeoIDs;

	@JsonProperty("message")
	private String message;

	@JsonProperty("Field area (acres)")
	private BigDecimal fieldAreaAcres;

	public String getGeoID() {
		return geoID;
	}

	public void setGeoID(String geoID) {
		this.geoID = geoID;
	}

	public List<String> getMatchedGeoIDs() {
		return matchedGeoIDs;
	}

	public void setMatchedGeoIDs(List<String> matchedGeoIDs) {
		this.matchedGeoIDs = matchedGeoIDs;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public BigDecimal getFieldAreaAcres() {
		return fieldAreaAcres;
	}

	public void setFieldAreaAcres(BigDecimal fieldAreaAcres) {
		this.fieldAreaAcres = fieldAreaAcres;
	}

}
