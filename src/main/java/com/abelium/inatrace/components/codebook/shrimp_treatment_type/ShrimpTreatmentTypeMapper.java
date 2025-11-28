package com.abelium.inatrace.components.codebook.shrimp_treatment_type;

import com.abelium.inatrace.components.codebook.shrimp_treatment_type.api.ApiShrimpTreatmentType;
import com.abelium.inatrace.components.codebook.shrimp_treatment_type.api.ApiShrimpTreatmentTypeTranslation;
import com.abelium.inatrace.db.entities.codebook.ShrimpTreatmentType;
import com.abelium.inatrace.db.entities.codebook.ShrimpTreatmentTypeTranslation;
import com.abelium.inatrace.types.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Mapper for ShrimpTreatmentType entity to API DTO.
 */
public final class ShrimpTreatmentTypeMapper {

    private ShrimpTreatmentTypeMapper() {}

    public static ApiShrimpTreatmentType toApiShrimpTreatmentType(ShrimpTreatmentType entity, Language language) {
        if (entity == null) return null;

        ApiShrimpTreatmentType api = new ApiShrimpTreatmentType();
        api.setId(entity.getId());
        api.setCode(entity.getCode());
        api.setDisplayOrder(entity.getDisplayOrder());
        api.setStatus(entity.getStatus());

        if (language != null && entity.getTranslations() != null) {
            Optional<ShrimpTreatmentTypeTranslation> translation = entity.getTranslations().stream()
                    .filter(t -> t.getLanguage() == language)
                    .findFirst();

            if (translation.isPresent()) {
                api.setLabel(translation.get().getLabel());
                api.setDescription(translation.get().getDescription());
            } else {
                Optional<ShrimpTreatmentTypeTranslation> englishTranslation = entity.getTranslations().stream()
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

        if (entity.getTranslations() != null) {
            List<ApiShrimpTreatmentTypeTranslation> apiTranslations = new ArrayList<>();
            for (ShrimpTreatmentTypeTranslation t : entity.getTranslations()) {
                ApiShrimpTreatmentTypeTranslation apiT = new ApiShrimpTreatmentTypeTranslation();
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
