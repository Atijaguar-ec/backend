package com.abelium.inatrace.components.codebook.shrimp_freezing_type.api;

import com.abelium.inatrace.api.ApiCodebookBaseEntity;
import com.abelium.inatrace.db.enums.CodebookStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * API DTO for ShrimpFreezingType catalog.
 */
@Validated
public class ApiShrimpFreezingType extends ApiCodebookBaseEntity {

    @Schema(description = "Unique code (IQF, BLOCK, SEMI_IQF)")
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
    private List<ApiShrimpFreezingTypeTranslation> translations;

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

    public List<ApiShrimpFreezingTypeTranslation> getTranslations() {
        return translations;
    }

    public void setTranslations(List<ApiShrimpFreezingTypeTranslation> translations) {
        this.translations = translations;
    }
}
