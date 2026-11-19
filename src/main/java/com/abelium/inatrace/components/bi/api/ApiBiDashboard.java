package com.abelium.inatrace.components.bi.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class ApiBiDashboard {

	@Schema(description = "Superset dashboard slug")
	public String slug;

	@Schema(description = "Display tab label")
	public String label;

	@Schema(description = "Short description of the report")
	public String description;

	@Schema(description = "True when defined by the official BI catalog")
	public boolean isOfficial;

	@Schema(description = "Display order in tabs")
	public int sortOrder;

	@Schema(description = "Authorized INATrace roles allowed to access this dashboard")
	public List<String> allowedRoles;

	public ApiBiDashboard() {
	}

	public ApiBiDashboard(
			String slug,
			String label,
			String description,
			boolean isOfficial,
			int sortOrder,
			List<String> allowedRoles
	) {
		this.slug = slug;
		this.label = label;
		this.description = description;
		this.isOfficial = isOfficial;
		this.sortOrder = sortOrder;
		this.allowedRoles = allowedRoles;
	}
}
