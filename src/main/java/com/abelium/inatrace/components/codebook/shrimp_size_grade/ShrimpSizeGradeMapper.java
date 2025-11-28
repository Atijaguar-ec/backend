package com.abelium.inatrace.components.codebook.shrimp_size_grade;

import com.abelium.inatrace.components.codebook.shrimp_size_grade.api.ApiShrimpSizeGrade;
import com.abelium.inatrace.db.entities.codebook.ShrimpSizeGrade;

/**
 * Mapper for ShrimpSizeGrade entity.
 *
 * @author INATrace Team
 */
public final class ShrimpSizeGradeMapper {

    private ShrimpSizeGradeMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static ApiShrimpSizeGrade toApiShrimpSizeGrade(ShrimpSizeGrade entity) {
        if (entity == null) {
            return null;
        }

        ApiShrimpSizeGrade api = new ApiShrimpSizeGrade();
        api.setId(entity.getId());
        api.setCode(entity.getCode());
        api.setLabel(entity.getLabel());
        api.setSizeType(entity.getSizeType());
        api.setDisplayOrder(entity.getDisplayOrder());
        api.setStatus(entity.getStatus());

        return api;
    }
}
