package com.abelium.inatrace.components.codebook.shrimp_process_type.api;

import com.abelium.inatrace.api.ApiCodebookBaseEntity;
import com.abelium.inatrace.db.enums.CodebookStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * Shrimp process type API model.
 *
 * @author INATrace Team
 */
@Validated
public class ApiShrimpProcessType extends ApiCodebookBaseEntity {

    @Schema(description = "Description of the process type")
    private String description;

    @Schema(description = "Display order in lists")
    private Integer displayOrder;

    @Schema(description = "Status of the process type (ACTIVE or INACTIVE)")
    private CodebookStatus status;

    @Schema(description = "Translations for the process type")
    private List<ApiShrimpProcessTypeTranslation> translations;

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

    public List<ApiShrimpProcessTypeTranslation> getTranslations() {
        if (translations == null) {
            translations = new ArrayList<>();
        }
        return translations;
    }

    public void setTranslations(List<ApiShrimpProcessTypeTranslation> translations) {
        this.translations = translations;
    }
}
