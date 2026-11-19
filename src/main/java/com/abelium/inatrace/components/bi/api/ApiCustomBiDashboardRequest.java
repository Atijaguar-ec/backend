package com.abelium.inatrace.components.bi.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ApiCustomBiDashboardRequest {

	@NotNull
	@Schema(description = "Company ID", requiredMode = Schema.RequiredMode.REQUIRED)
	private Long companyId;

	@NotBlank
	@Pattern(regexp = "^[a-z0-9][a-z0-9-]{2,120}$", message = "Slug de tablero inválido (use minúsculas, números y guiones)")
	@Schema(description = "Superset dashboard slug created in Studio", requiredMode = Schema.RequiredMode.REQUIRED)
	private String slug;

	@NotBlank
	@Size(max = 120)
	@Schema(description = "Tab label in INATrace", requiredMode = Schema.RequiredMode.REQUIRED)
	private String label;

	@Size(max = 300)
	@Schema(description = "Optional dashboard description")
	private String description;

	@Schema(description = "Optional tab sort order")
	private Integer sortOrder;

	@NotEmpty(message = "Debe especificar al menos un rol autorizado")
	@Schema(description = "Allowed roles: USER, COMPANY_ADMIN, REGIONAL_ADMIN, SYSTEM_ADMIN", requiredMode = Schema.RequiredMode.REQUIRED)
	private List<String> allowedRoles;

	public Long getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Long companyId) {
		this.companyId = companyId;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(Integer sortOrder) {
		this.sortOrder = sortOrder;
	}

	public List<String> getAllowedRoles() {
		return allowedRoles;
	}

	public void setAllowedRoles(List<String> allowedRoles) {
		this.allowedRoles = allowedRoles;
	}
}
