package com.abelium.inatrace.components.codebook.shrimp_color_grade;

import com.abelium.inatrace.components.codebook.shrimp_color_grade.api.ApiShrimpColorGrade;
import com.abelium.inatrace.db.entities.codebook.ShrimpColorGrade;

/**
 * Mapper for ShrimpColorGrade entity.
 *
 * @author INATrace Team
 */
public final class ShrimpColorGradeMapper {

    private ShrimpColorGradeMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static ApiShrimpColorGrade toApiShrimpColorGrade(ShrimpColorGrade entity) {
        if (entity == null) {
            return null;
        }

        ApiShrimpColorGrade api = new ApiShrimpColorGrade();
        api.setId(entity.getId());
        api.setCode(entity.getCode());
        api.setLabel(entity.getLabel());
        api.setDescription(entity.getDescription());
        api.setDisplayOrder(entity.getDisplayOrder());
        api.setStatus(entity.getStatus());

        return api;
    }
}
