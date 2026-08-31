package com.abelium.inatrace.components.whisp.api;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Whisp {@code analysisOptions} block, sent with every submission.
 */
@Schema(description = "Analysis options sent to the Whisp API.")
public class ApiWhispAnalysisOptions {

	/**
	 * Run the analysis in the background. Whisp evaluates the geometry against Earth
	 * Engine datasets, which takes far longer than an HTTP request may block, so the
	 * backend always submits asynchronously and polls the job afterwards.
	 */
	private Boolean async = Boolean.TRUE;

	/**
	 * Unit the reported area is expressed in.
	 */
	private String unitType;

	/**
	 * Name of the property carrying our own identifier, so the result features can be
	 * matched back to the plot they came from.
	 */
	private String externalIdColumn;

	public Boolean getAsync() {
		return async;
	}

	public void setAsync(Boolean async) {
		this.async = async;
	}

	public String getUnitType() {
		return unitType;
	}

	public void setUnitType(String unitType) {
		this.unitType = unitType;
	}

	public String getExternalIdColumn() {
		return externalIdColumn;
	}

	public void setExternalIdColumn(String externalIdColumn) {
		this.externalIdColumn = externalIdColumn;
	}

}
