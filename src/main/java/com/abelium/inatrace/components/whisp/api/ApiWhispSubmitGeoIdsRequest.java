package com.abelium.inatrace.components.whisp.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "API model for a Whisp submission by AgStack geo id.")
public class ApiWhispSubmitGeoIdsRequest {

	private List<String> geoIds;

	private ApiWhispAnalysisOptions analysisOptions;

	public List<String> getGeoIds() {
		return geoIds;
	}

	public void setGeoIds(List<String> geoIds) {
		this.geoIds = geoIds;
	}

	public ApiWhispAnalysisOptions getAnalysisOptions() {
		return analysisOptions;
	}

	public void setAnalysisOptions(ApiWhispAnalysisOptions analysisOptions) {
		this.analysisOptions = analysisOptions;
	}

}
