package com.abelium.inatrace.components.codebook.shrimp_quality_grade;

import com.abelium.inatrace.components.codebook.shrimp_quality_grade.api.ApiShrimpQualityGrade;
import com.abelium.inatrace.components.codebook.shrimp_quality_grade.api.ApiShrimpQualityGradeTranslation;
import com.abelium.inatrace.db.entities.codebook.ShrimpQualityGrade;
import com.abelium.inatrace.db.entities.codebook.ShrimpQualityGradeTranslation;
import com.abelium.inatrace.types.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Mapper for ShrimpQualityGrade entity to API DTO.
 */
public final class ShrimpQualityGradeMapper {

    private ShrimpQualityGradeMapper() {}

    public static ApiShrimpQualityGrade toApiShrimpQualityGrade(ShrimpQualityGrade entity) {
        return toApiShrimpQualityGrade(entity, null);
    }

    public static ApiShrimpQualityGrade toApiShrimpQualityGrade(ShrimpQualityGrade entity, Language language) {
        if (entity == null) return null;

        ApiShrimpQualityGrade api = new ApiShrimpQualityGrade();
        api.setId(entity.getId());
        api.setCode(entity.getCode());
        api.setDisplayOrder(entity.getDisplayOrder());
        api.setStatus(entity.getStatus());

        // If language specified, get translated values
        if (language != null && entity.getTranslations() != null) {
            Optional<ShrimpQualityGradeTranslation> translation = entity.getTranslations().stream()
                    .filter(t -> t.getLanguage() == language)
                    .findFirst();

            if (translation.isPresent()) {
                api.setLabel(translation.get().getLabel());
                api.setDescription(translation.get().getDescription());
            } else {
                // Fallback to English
                Optional<ShrimpQualityGradeTranslation> englishTranslation = entity.getTranslations().stream()
                        .filter(t -> t.getLanguage() == Language.EN)
                        .findFirst();
                if (englishTranslation.isPresent()) {
                    api.setLabel(englishTranslation.get().getLabel());
                    api.setDescription(englishTranslation.get().getDescription());
                } else {
                    api.setLabel(entity.getLabel());
                    api.setDescription(entity.getDescription());
                }
            }
        } else {
            api.setLabel(entity.getLabel());
            api.setDescription(entity.getDescription());
        }

        // Map all translations
        if (entity.getTranslations() != null) {
            List<ApiShrimpQualityGradeTranslation> apiTranslations = new ArrayList<>();
            for (ShrimpQualityGradeTranslation t : entity.getTranslations()) {
                ApiShrimpQualityGradeTranslation apiT = new ApiShrimpQualityGradeTranslation();
                apiT.setLanguage(t.getLanguage());
                apiT.setLabel(t.getLabel());
                apiT.setDescription(t.getDescription());
                apiTranslations.add(apiT);
            }
            api.setTranslations(apiTranslations);
        }

        return api;
    }
}
