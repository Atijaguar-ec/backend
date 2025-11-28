package com.abelium.inatrace.components.codebook.shrimp_presentation_type;

import com.abelium.inatrace.components.codebook.shrimp_presentation_type.api.ApiShrimpPresentationType;
import com.abelium.inatrace.components.codebook.shrimp_presentation_type.api.ApiShrimpPresentationTypeTranslation;
import com.abelium.inatrace.db.entities.codebook.ShrimpPresentationType;
import com.abelium.inatrace.db.entities.codebook.ShrimpPresentationTypeTranslation;
import com.abelium.inatrace.types.Language;

import java.util.stream.Collectors;

/**
 * Mapper for ShrimpPresentationType entity.
 *
 * @author INATrace Team
 */
public final class ShrimpPresentationTypeMapper {

    private ShrimpPresentationTypeMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static ApiShrimpPresentationType toApiShrimpPresentationTypeBase(ShrimpPresentationType entity, Language language) {
        if (entity == null) {
            return null;
        }

        ShrimpPresentationTypeTranslation translation = entity.getTranslations().stream()
                .filter(t -> t.getLanguage().equals(language))
                .findFirst()
                .orElse(null);

        // Fallback to English if translation not found
        if (translation == null || translation.getLabel() == null || translation.getLabel().isEmpty()) {
            translation = entity.getTranslations().stream()
                    .filter(t -> t.getLanguage().equals(Language.EN))
                    .findFirst()
                    .orElse(null);
        }

        ApiShrimpPresentationType api = new ApiShrimpPresentationType();
        api.setId(entity.getId());
        api.setCode(entity.getCode());
        api.setLabel(translation != null && translation.getLabel() != null ? translation.getLabel() : entity.getLabel());
        api.setCategory(entity.getCategory());
        api.setDescription(translation != null && translation.getDescription() != null ? translation.getDescription() : entity.getDescription());
        api.setDisplayOrder(entity.getDisplayOrder());
        api.setStatus(entity.getStatus());

        return api;
    }

    public static ApiShrimpPresentationType toApiShrimpPresentationType(ShrimpPresentationType entity, Language language) {
        ApiShrimpPresentationType api = toApiShrimpPresentationTypeBase(entity, language);
        if (api == null) {
            return null;
        }

        api.setTranslations(
                entity.getTranslations().stream()
                        .map(ShrimpPresentationTypeMapper::toApiTranslation)
                        .collect(Collectors.toList())
        );

        return api;
    }

    public static ApiShrimpPresentationTypeTranslation toApiTranslation(ShrimpPresentationTypeTranslation translation) {
        ApiShrimpPresentationTypeTranslation api = new ApiShrimpPresentationTypeTranslation();
        api.setLanguage(translation.getLanguage());
        api.setLabel(translation.getLabel());
        api.setDescription(translation.getDescription());
        return api;
    }
}
