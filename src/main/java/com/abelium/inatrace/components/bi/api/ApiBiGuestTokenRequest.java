package com.abelium.inatrace.components.bi.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class ApiBiGuestTokenRequest {

	@NotNull
	@Schema(description = "Company ID", requiredMode = Schema.RequiredMode.REQUIRED)
	private Long companyId;

	@NotBlank
	@Pattern(regexp = "^[a-z0-9][a-z0-9-]{2,120}$", message = "Slug de tablero inválido")
	@Schema(description = "Superset dashboard slug", requiredMode = Schema.RequiredMode.REQUIRED)
	private String slug;

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
}
