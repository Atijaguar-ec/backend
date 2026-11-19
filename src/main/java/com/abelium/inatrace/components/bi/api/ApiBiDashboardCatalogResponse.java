package com.abelium.inatrace.components.bi.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class ApiBiDashboardCatalogResponse {

	@Schema(description = "Public Superset base URL including application root (/bi)")
	public String supersetDomain;

	@Schema(description = "Active tenant organization slug")
	public String organizationSlug;

	@Schema(description = "True if the authenticated user may open the BI Design Studio")
	public boolean studioEnabled;

	@Schema(description = "Read-only bi.* views authorized for this organization")
	public List<String> allowedDatasets;

	@Schema(description = "Dashboards authorized for the requesting user")
	public List<ApiBiDashboard> dashboards;
}
