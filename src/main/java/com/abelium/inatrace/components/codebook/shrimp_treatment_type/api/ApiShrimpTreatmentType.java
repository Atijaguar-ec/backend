package com.abelium.inatrace.components.codebook.shrimp_treatment_type.api;

import com.abelium.inatrace.api.ApiCodebookBaseEntity;
import com.abelium.inatrace.db.enums.CodebookStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * API DTO for ShrimpTreatmentType catalog.
 */
@Validated
public class ApiShrimpTreatmentType extends ApiCodebookBaseEntity {

    @Schema(description = "Unique code (BISULFITO, METABISULFITO, etc.)")
    private String code;

    @Schema(description = "Default label")
    private String label;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Display order")
    private Integer displayOrder;

    @Schema(description = "Status")
    private CodebookStatus status;

    @Schema(description = "Translations")
    private List<ApiShrimpTreatmentTypeTranslation> translations;

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

    public List<ApiShrimpTreatmentTypeTranslation> getTranslations() {
        return translations;
    }

    public void setTranslations(List<ApiShrimpTreatmentTypeTranslation> translations) {
        this.translations = translations;
    }
}
