package com.abelium.inatrace.components.agstack.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "API model for AgStack register field boundary request.")
public class ApiRegisterFieldBoundaryRequest {

	@JsonProperty("s2_index")
	private String s2Index;

	private String wkt;

	@JsonProperty("resolution_level")
	private Integer resolutionLevel;

	private Integer threshold;

	@JsonProperty("return_s2_indices")
	private Boolean returnS2Indices;

	public String getS2Index() {
		return s2Index;
	}

	public void setS2Index(String s2Index) {
		this.s2Index = s2Index;
	}

	public String getWkt() {
		return wkt;
	}

	public void setWkt(String wkt) {
		this.wkt = wkt;
	}

	public Integer getResolutionLevel() {
		return resolutionLevel;
	}

	public void setResolutionLevel(Integer resolutionLevel) {
		this.resolutionLevel = resolutionLevel;
	}

	public Integer getThreshold() {
		return threshold;
	}

	public void setThreshold(Integer threshold) {
		this.threshold = threshold;
	}

	public Boolean getReturnS2Indices() {
		return returnS2Indices;
	}

	public void setReturnS2Indices(Boolean returnS2Indices) {
		this.returnS2Indices = returnS2Indices;
	}

}
