package com.abelium.inatrace.components.codebook.shrimp_freezing_type;

import com.abelium.inatrace.components.codebook.shrimp_freezing_type.api.ApiShrimpFreezingType;
import com.abelium.inatrace.components.codebook.shrimp_freezing_type.api.ApiShrimpFreezingTypeTranslation;
import com.abelium.inatrace.db.entities.codebook.ShrimpFreezingType;
import com.abelium.inatrace.db.entities.codebook.ShrimpFreezingTypeTranslation;
import com.abelium.inatrace.types.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Mapper for ShrimpFreezingType entity to API DTO.
 */
public final class ShrimpFreezingTypeMapper {

    private ShrimpFreezingTypeMapper() {}

    public static ApiShrimpFreezingType toApiShrimpFreezingType(ShrimpFreezingType entity) {
        return toApiShrimpFreezingType(entity, null);
    }

    public static ApiShrimpFreezingType toApiShrimpFreezingType(ShrimpFreezingType entity, Language language) {
        if (entity == null) return null;

        ApiShrimpFreezingType api = new ApiShrimpFreezingType();
        api.setId(entity.getId());
        api.setCode(entity.getCode());
        api.setDisplayOrder(entity.getDisplayOrder());
        api.setStatus(entity.getStatus());

        // If language specified, get translated values
        if (language != null && entity.getTranslations() != null) {
            String langCode = language.name().toLowerCase();
            Optional<ShrimpFreezingTypeTranslation> translation = entity.getTranslations().stream()
                    .filter(t -> t.getLanguage() != null && t.getLanguage().equalsIgnoreCase(langCode))
                    .findFirst();

            if (translation.isPresent()) {
                api.setLabel(translation.get().getLabel());
                api.setDescription(translation.get().getDescription());
            } else {
                // Fallback to English
                Optional<ShrimpFreezingTypeTranslation> englishTranslation = entity.getTranslations().stream()
                        .filter(t -> "en".equalsIgnoreCase(t.getLanguage()))
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
            List<ApiShrimpFreezingTypeTranslation> apiTranslations = new ArrayList<>();
            for (ShrimpFreezingTypeTranslation t : entity.getTranslations()) {
                ApiShrimpFreezingTypeTranslation apiT = new ApiShrimpFreezingTypeTranslation();
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
