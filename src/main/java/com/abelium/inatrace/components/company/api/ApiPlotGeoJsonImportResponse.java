package com.abelium.inatrace.components.company.api;

import com.abelium.inatrace.components.company.types.PlotGeoJsonImportScope;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Result of a bulk GeoJSON plot import: what it would do (preview) or what it did.
 * 
 * A preview and the import that follows are computed the same way, so the numbers the
 * user confirmed are the numbers applied.
 */
public class ApiPlotGeoJsonImportResponse {

	@Schema(description = "False for a preview, true when the changes were saved")
	private Boolean applied;

	@Schema(description = "Which existing plots are replaced")
	private PlotGeoJsonImportScope scope;

	@Schema(description = "Features in the file")
	private Integer featuresRead;

	@Schema(description = "Plots created from the file (or to be created, in a preview)")
	private Integer plotsToCreate;

	@Schema(description = "Existing plots deleted (or to be deleted, in a preview)")
	private Long plotsToDelete;

	@Schema(description = "Farmers that receive at least one plot")
	private Integer farmersWithNewPlots;

	@Schema(description = "Plots whose AgStack geo ID is being generated in the background after the import")
	private Integer geoIdsPending;

	@Schema(description = "Companies in the scope of the import")
	private List<ApiPlotGeoJsonImportCompany> companies;

	@Schema(description = "Features that cannot be imported")
	private List<ApiPlotGeoJsonImportIssue> issues;

	@Schema(description = "Farmers of the companies that receive plots, left without any plot")
	private List<ApiPlotGeoJsonImportFarmer> farmersWithoutPlots;

	public Boolean getApplied() {
		return applied;
	}

	public void setApplied(Boolean applied) {
		this.applied = applied;
	}

	public PlotGeoJsonImportScope getScope() {
		return scope;
	}

	public void setScope(PlotGeoJsonImportScope scope) {
		this.scope = scope;
	}

	public Integer getFeaturesRead() {
		return featuresRead;
	}

	public void setFeaturesRead(Integer featuresRead) {
		this.featuresRead = featuresRead;
	}

	public Integer getPlotsToCreate() {
		return plotsToCreate;
	}

	public void setPlotsToCreate(Integer plotsToCreate) {
		this.plotsToCreate = plotsToCreate;
	}

	public Long getPlotsToDelete() {
		return plotsToDelete;
	}

	public void setPlotsToDelete(Long plotsToDelete) {
		this.plotsToDelete = plotsToDelete;
	}

	public Integer getFarmersWithNewPlots() {
		return farmersWithNewPlots;
	}

	public void setFarmersWithNewPlots(Integer farmersWithNewPlots) {
		this.farmersWithNewPlots = farmersWithNewPlots;
	}

	public Integer getGeoIdsPending() {
		return geoIdsPending;
	}

	public void setGeoIdsPending(Integer geoIdsPending) {
		this.geoIdsPending = geoIdsPending;
	}

	public List<ApiPlotGeoJsonImportCompany> getCompanies() {
		return companies;
	}

	public void setCompanies(List<ApiPlotGeoJsonImportCompany> companies) {
		this.companies = companies;
	}

	public List<ApiPlotGeoJsonImportIssue> getIssues() {
		return issues;
	}

	public void setIssues(List<ApiPlotGeoJsonImportIssue> issues) {
		this.issues = issues;
	}

	public List<ApiPlotGeoJsonImportFarmer> getFarmersWithoutPlots() {
		return farmersWithoutPlots;
	}

	public void setFarmersWithoutPlots(List<ApiPlotGeoJsonImportFarmer> farmersWithoutPlots) {
		this.farmersWithoutPlots = farmersWithoutPlots;
	}

}
