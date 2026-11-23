package com.abelium.inatrace.components.integration.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authorized active organization in INATrace")
public class ApiCompanyIntegration {

    @JsonProperty("id")
    @Schema(description = "Unique organization ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @JsonProperty("name")
    @Schema(description = "Organization or affiliate association name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @JsonProperty("business_name")
    @Schema(description = "Registered legal business name")
    private String businessName;

    @JsonProperty("tax_id")
    @Schema(description = "Ecuadorian 13-digit taxpayer ID (RUC)")
    private String taxId;

    public ApiCompanyIntegration() {}

    public ApiCompanyIntegration(Long id, String name, String businessName, String taxId) {
        this.id = id;
        this.name = name;
        this.businessName = businessName;
        this.taxId = taxId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }
}
