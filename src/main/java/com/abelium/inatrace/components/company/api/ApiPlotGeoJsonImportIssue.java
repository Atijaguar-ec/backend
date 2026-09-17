package com.abelium.inatrace.components.company.api;

import com.abelium.inatrace.components.company.types.PlotGeoJsonImportIssueType;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A GeoJSON feature that cannot be imported as a plot.
 */
public class ApiPlotGeoJsonImportIssue {

	@Schema(description = "1-based position of the feature in the file")
	private Integer featureNumber;

	@Schema(description = "Farmer internal ID read from the feature, if any")
	private String farmerInternalId;

	@Schema(description = "Plot code read from the feature, if any")
	private String plotName;

	@Schema(description = "Why the feature cannot be imported")
	private PlotGeoJsonImportIssueType type;

	public Integer getFeatureNumber() {
		return featureNumber;
	}

	public void setFeatureNumber(Integer featureNumber) {
		this.featureNumber = featureNumber;
	}

	public String getFarmerInternalId() {
		return farmerInternalId;
	}

	public void setFarmerInternalId(String farmerInternalId) {
		this.farmerInternalId = farmerInternalId;
	}

	public String getPlotName() {
		return plotName;
	}

	public void setPlotName(String plotName) {
		this.plotName = plotName;
	}

	public PlotGeoJsonImportIssueType getType() {
		return type;
	}

	public void setType(PlotGeoJsonImportIssueType type) {
		this.type = type;
	}

}
