package com.abelium.inatrace.components.company.api;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A farmer that is left without plots by a bulk GeoJSON plot import.
 */
public class ApiPlotGeoJsonImportFarmer {

	@Schema(description = "Farmer ID")
	private Long id;

	@Schema(description = "Company ID of the farmer")
	private Long companyId;

	@Schema(description = "Company name of the farmer")
	private String companyName;

	@Schema(description = "Farmer internal ID")
	private String internalId;

	@Schema(description = "Farmer name and surname")
	private String name;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Long companyId) {
		this.companyId = companyId;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getInternalId() {
		return internalId;
	}

	public void setInternalId(String internalId) {
		this.internalId = internalId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
