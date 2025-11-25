package com.abelium.inatrace.components.codebook.shrimp_color_grade.api;

import com.abelium.inatrace.api.ApiCodebookBaseEntity;
import com.abelium.inatrace.db.enums.CodebookStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

/**
 * Shrimp color grade API model.
 *
 * @author INATrace Team
 */
@Validated
public class ApiShrimpColorGrade extends ApiCodebookBaseEntity {

    @Schema(description = "Description of the color grade")
    private String description;

    @Schema(description = "Display order in lists")
    private Integer displayOrder;

    @Schema(description = "Status of the color grade (ACTIVE or INACTIVE)")
    private CodebookStatus status;

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
}
