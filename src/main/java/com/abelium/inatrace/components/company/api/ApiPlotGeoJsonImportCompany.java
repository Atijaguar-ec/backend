package com.abelium.inatrace.components.company.api;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A company in the scope of a bulk GeoJSON plot import.
 */
public class ApiPlotGeoJsonImportCompany {

	@Schema(description = "Company ID")
	private Long id;

	@Schema(description = "Company name")
	private String name;

	@Schema(description = "Plots the company has before the import")
	private Long existingPlots;

	@Schema(description = "Whether the existing plots are deleted by the import")
	private Boolean replaced;

	@Schema(description = "Plots the import creates for farmers of this company")
	private Integer newPlots;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getExistingPlots() {
		return existingPlots;
	}

	public void setExistingPlots(Long existingPlots) {
		this.existingPlots = existingPlots;
	}

	public Boolean getReplaced() {
		return replaced;
	}

	public void setReplaced(Boolean replaced) {
		this.replaced = replaced;
	}

	public Integer getNewPlots() {
		return newPlots;
	}

	public void setNewPlots(Integer newPlots) {
		this.newPlots = newPlots;
	}

}
