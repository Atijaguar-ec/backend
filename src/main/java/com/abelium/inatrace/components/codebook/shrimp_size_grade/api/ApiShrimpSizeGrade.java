package com.abelium.inatrace.components.codebook.shrimp_size_grade.api;

import com.abelium.inatrace.api.ApiCodebookBaseEntity;
import com.abelium.inatrace.db.enums.CodebookStatus;
import com.abelium.inatrace.db.enums.ShrimpSizeType;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

/**
 * Shrimp size grade API model.
 *
 * @author INATrace Team
 */
@Validated
public class ApiShrimpSizeGrade extends ApiCodebookBaseEntity {

    @Schema(description = "Size type: WHOLE (Head-On) or TAIL (Shell-On/Value Added)")
    private ShrimpSizeType sizeType;

    @Schema(description = "Display order in lists")
    private Integer displayOrder;

    @Schema(description = "Status of the size grade (ACTIVE or INACTIVE)")
    private CodebookStatus status;

    public ShrimpSizeType getSizeType() {
        return sizeType;
    }

    public void setSizeType(ShrimpSizeType sizeType) {
        this.sizeType = sizeType;
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
