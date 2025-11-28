package com.abelium.inatrace.components.codebook.shrimp_flavor_defect.api;

import com.abelium.inatrace.api.ApiCodebookBaseEntity;
import com.abelium.inatrace.db.enums.CodebookStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * Shrimp flavor defect API model.
 *
 * @author INATrace Team
 */
@Validated
public class ApiShrimpFlavorDefect extends ApiCodebookBaseEntity {

    @Schema(description = "Description of the defect")
    private String description;

    @Schema(description = "Display order in lists")
    private Integer displayOrder;

    @Schema(description = "Status of the defect (ACTIVE or INACTIVE)")
    private CodebookStatus status;

    @Schema(description = "Translations for the defect")
    private List<ApiShrimpFlavorDefectTranslation> translations;

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

    public List<ApiShrimpFlavorDefectTranslation> getTranslations() {
        if (translations == null) {
            translations = new ArrayList<>();
        }
        return translations;
    }

    public void setTranslations(List<ApiShrimpFlavorDefectTranslation> translations) {
        this.translations = translations;
    }
}
