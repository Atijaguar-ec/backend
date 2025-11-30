package com.abelium.inatrace.components.codebook.shrimp_brand.api;

import com.abelium.inatrace.api.ApiCodebookBaseEntity;
import com.abelium.inatrace.db.enums.CodebookStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;

/**
 * API DTO for ShrimpBrand catalog.
 * Includes weight per box and measure unit.
 */
@Validated
public class ApiShrimpBrand extends ApiCodebookBaseEntity {

    @Schema(description = "Unique code (DUFER_2KG, DUFER_5LB, etc.)")
    private String code;

    @Schema(description = "Brand name/label")
    private String label;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Default weight per box for this brand")
    private BigDecimal weightPerBox;

    @Schema(description = "Measure unit for the weight (KG, LB)")
    private String measureUnit;

    @Schema(description = "Display order")
    private Integer displayOrder;

    @Schema(description = "Status")
    private CodebookStatus status;

    @Schema(description = "Translations")
    private List<ApiShrimpBrandTranslation> translations;

    // Getters and Setters

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public BigDecimal getWeightPerBox() {
        return weightPerBox;
    }

    public void setWeightPerBox(BigDecimal weightPerBox) {
        this.weightPerBox = weightPerBox;
    }

    public String getMeasureUnit() {
        return measureUnit;
    }

    public void setMeasureUnit(String measureUnit) {
        this.measureUnit = measureUnit;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public CodebookStatus getStatus() {
        return status;
    }

    public void setStatus(CodebookStatus status) {
        this.status = status;
    }

    public List<ApiShrimpBrandTranslation> getTranslations() {
        return translations;
    }

    public void setTranslations(List<ApiShrimpBrandTranslation> translations) {
        this.translations = translations;
    }
}
