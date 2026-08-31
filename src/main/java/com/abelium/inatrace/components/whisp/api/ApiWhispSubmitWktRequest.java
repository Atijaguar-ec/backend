package com.abelium.inatrace.components.whisp.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "API model for a Whisp submission by WKT polygon.")
public class ApiWhispSubmitWktRequest {

	private String wkt;

	private ApiWhispAnalysisOptions analysisOptions;

	public String getWkt() {
		return wkt;
	}

	public void setWkt(String wkt) {
		this.wkt = wkt;
	}

	public ApiWhispAnalysisOptions getAnalysisOptions() {
		return analysisOptions;
	}

	public void setAnalysisOptions(ApiWhispAnalysisOptions analysisOptions) {
		this.analysisOptions = analysisOptions;
	}

}
