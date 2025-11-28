package com.abelium.inatrace.components.codebook.shrimp_presentation_type.api;

import com.abelium.inatrace.api.ApiBaseEntity;
import com.abelium.inatrace.db.enums.CodebookStatus;
import com.abelium.inatrace.db.enums.ShrimpPresentationCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * API model for shrimp presentation type.
 *
 * @author INATrace Team
 */
@Validated
public class ApiShrimpPresentationType extends ApiBaseEntity {

    @Schema(description = "Unique code identifier", example = "SHELL_ON_A")
    private String code;

    @Schema(description = "Display label", example = "Shell-On A")
    private String label;

    @Schema(description = "Category: SHELL_ON, BROKEN, or OTHER")
    private ShrimpPresentationCategory category;

    @Schema(description = "Description of the presentation type")
    private String description;

    @Schema(description = "Display order within category")
    private Integer displayOrder;

    @Schema(description = "Status: ACTIVE or INACTIVE")
    private CodebookStatus status;

    @Schema(description = "Translations for multi-language support")
    private List<ApiShrimpPresentationTypeTranslation> translations;

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

    public ShrimpPresentationCategory getCategory() {
        return category;
    }

    public void setCategory(ShrimpPresentationCategory category) {
        this.category = category;
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

    public List<ApiShrimpPresentationTypeTranslation> getTranslations() {
        if (translations == null) {
            translations = new ArrayList<>();
        }
        return translations;
    }

    public void setTranslations(List<ApiShrimpPresentationTypeTranslation> translations) {
        this.translations = translations;
    }
}
