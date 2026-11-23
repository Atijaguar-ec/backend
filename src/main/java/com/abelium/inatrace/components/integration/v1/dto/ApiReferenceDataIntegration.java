package com.abelium.inatrace.components.integration.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Master reference catalogs and exact mass conversion factors")
public class ApiReferenceDataIntegration {

    @JsonProperty("units")
    @Schema(description = "Normalized mass units with exact kilogram conversion factors", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<UnitDto> units;

    @JsonProperty("semi_products")
    @Schema(description = "Cocoa semi-products registered in INATrace", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<SemiProductDto> semiProducts;

    @JsonProperty("cocoa_varieties")
    @Schema(description = "Supported cocoa varieties", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<VarietyDto> cocoaVarieties;

    @JsonProperty("certification_types")
    @Schema(description = "Active certification types", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CertificationTypeDto> certificationTypes;

    @Schema(name = "ReferenceUnitDto", description = "Measurement unit catalog entry with display label and exact kilogram factor")
    public static class UnitDto {
        @JsonProperty("code")
        @Schema(description = "Normalized unit code", allowableValues = {"LB", "KG", "QQ"}, requiredMode = Schema.RequiredMode.REQUIRED)
        private String code;

        @JsonProperty("label")
        @Schema(description = "Human-readable unit label", requiredMode = Schema.RequiredMode.REQUIRED)
        private String label;

        @JsonProperty("kg_factor")
        @Schema(description = "Exact multiplicative conversion factor to kilograms", requiredMode = Schema.RequiredMode.REQUIRED)
        private BigDecimal kgFactor;

        public UnitDto() {}
        public UnitDto(String code, String label, BigDecimal kgFactor) {
            this.code = code;
            this.label = label;
            this.kgFactor = kgFactor;
        }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public BigDecimal getKgFactor() { return kgFactor; }
        public void setKgFactor(BigDecimal kgFactor) { this.kgFactor = kgFactor; }
    }

    @Schema(name = "SemiProductDto", description = "Semi-product catalog entry (use id as unique key)")
    public static class SemiProductDto {
        @JsonProperty("id")
        @Schema(description = "Unique semi-product ID", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long id;

        @JsonProperty("code")
        @Schema(description = "Grouping stage code (BABA, SECO, or SEMI_{id})", requiredMode = Schema.RequiredMode.REQUIRED)
        private String code;

        @JsonProperty("name")
        @Schema(description = "Semi-product display name", requiredMode = Schema.RequiredMode.REQUIRED)
        private String name;

        public SemiProductDto() {}
        public SemiProductDto(Long id, String code, String name) {
            this.id = id;
            this.code = code;
            this.name = name;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    @Schema(name = "VarietyDto", description = "Cocoa variety catalog entry")
    public static class VarietyDto {
        @JsonProperty("code")
        @Schema(description = "Normalized cocoa variety code", allowableValues = {"NACIONAL", "CCN51"}, requiredMode = Schema.RequiredMode.REQUIRED)
        private String code;

        @JsonProperty("label")
        @Schema(description = "Variety display label", requiredMode = Schema.RequiredMode.REQUIRED)
        private String label;

        public VarietyDto() {}
        public VarietyDto(String code, String label) {
            this.code = code;
            this.label = label;
        }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }

    @Schema(name = "CertificationTypeDto", description = "Active certification catalog entry (code is unique)")
    public static class CertificationTypeDto {
        @JsonProperty("id")
        @Schema(description = "Unique certification ID", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long id;

        @JsonProperty("code")
        @Schema(description = "Unique certification code", requiredMode = Schema.RequiredMode.REQUIRED)
        private String code;

        @JsonProperty("label")
        @Schema(description = "Certification display name", requiredMode = Schema.RequiredMode.REQUIRED)
        private String label;

        public CertificationTypeDto() {}
        public CertificationTypeDto(Long id, String code, String label) {
            this.id = id;
            this.code = code;
            this.label = label;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }

    public ApiReferenceDataIntegration() {}

    public List<UnitDto> getUnits() { return units; }
    public void setUnits(List<UnitDto> units) { this.units = units; }

    public List<SemiProductDto> getSemiProducts() { return semiProducts; }
    public void setSemiProducts(List<SemiProductDto> semiProducts) { this.semiProducts = semiProducts; }

    public List<VarietyDto> getCocoaVarieties() { return cocoaVarieties; }
    public void setCocoaVarieties(List<VarietyDto> cocoaVarieties) { this.cocoaVarieties = cocoaVarieties; }

    public List<CertificationTypeDto> getCertificationTypes() { return certificationTypes; }
    public void setCertificationTypes(List<CertificationTypeDto> certificationTypes) { this.certificationTypes = certificationTypes; }
}
