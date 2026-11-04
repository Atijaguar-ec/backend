package com.abelium.inatrace.components.integration.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public class ApiReferenceDataIntegration {

    @JsonProperty("units")
    private List<UnitDto> units;

    @JsonProperty("semi_products")
    private List<SemiProductDto> semiProducts;

    @JsonProperty("cocoa_varieties")
    private List<VarietyDto> cocoaVarieties;

    @JsonProperty("certification_types")
    private List<CertificationTypeDto> certificationTypes;

    public static class UnitDto {
        @JsonProperty("code")
        private String code;

        @JsonProperty("label")
        private String label;

        @JsonProperty("kg_factor")
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

    public static class SemiProductDto {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("code")
        private String code;

        @JsonProperty("name")
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

    public static class VarietyDto {
        @JsonProperty("code")
        private String code;

        @JsonProperty("label")
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

    public static class CertificationTypeDto {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("code")
        private String code;

        @JsonProperty("label")
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
