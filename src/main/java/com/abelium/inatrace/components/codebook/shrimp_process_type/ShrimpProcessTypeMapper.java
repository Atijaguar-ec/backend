package com.abelium.inatrace.components.codebook.shrimp_process_type;

import com.abelium.inatrace.components.codebook.shrimp_process_type.api.ApiShrimpProcessType;
import com.abelium.inatrace.components.codebook.shrimp_process_type.api.ApiShrimpProcessTypeTranslation;
import com.abelium.inatrace.db.entities.codebook.ShrimpProcessType;
import com.abelium.inatrace.db.entities.codebook.ShrimpProcessTypeTranslation;
import com.abelium.inatrace.types.Language;

import java.util.stream.Collectors;

/**
 * Mapper for ShrimpProcessType entity.
 *
 * @author INATrace Team
 */
public final class ShrimpProcessTypeMapper {

    private ShrimpProcessTypeMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static ApiShrimpProcessType toApiShrimpProcessTypeBase(ShrimpProcessType entity, Language language) {
        if (entity == null) {
            return null;
        }

        ShrimpProcessTypeTranslation translation = entity.getTranslations().stream()
                .filter(t -> t.getLanguage().equals(language))
                .findFirst()
                .orElse(null);

        // Fallback to English if translation not found
        if (translation == null || translation.getName() == null || translation.getName().isEmpty()) {
            translation = entity.getTranslations().stream()
                    .filter(t -> t.getLanguage().equals(Language.EN))
                    .findFirst()
                    .orElse(null);
        }

        ApiShrimpProcessType api = new ApiShrimpProcessType();
        api.setId(entity.getId());
        api.setCode(entity.getCode());
        api.setLabel(translation != null && translation.getName() != null ? translation.getName() : entity.getName());
        api.setDescription(translation != null && translation.getDescription() != null ? translation.getDescription() : entity.getDescription());
        api.setDisplayOrder(entity.getDisplayOrder());
        api.setStatus(entity.getStatus());

        return api;
    }

    public static ApiShrimpProcessType toApiShrimpProcessType(ShrimpProcessType entity, Language language) {
        ApiShrimpProcessType api = toApiShrimpProcessTypeBase(entity, language);
        if (api == null) {
            return null;
        }

        api.setTranslations(
                entity.getTranslations().stream()
                        .map(ShrimpProcessTypeMapper::toApiTranslation)
                        .collect(Collectors.toList())
        );

        return api;
    }

    public static ApiShrimpProcessTypeTranslation toApiTranslation(ShrimpProcessTypeTranslation translation) {
        ApiShrimpProcessTypeTranslation api = new ApiShrimpProcessTypeTranslation();
        api.setLanguage(translation.getLanguage());
        api.setName(translation.getName());
        api.setDescription(translation.getDescription());
        return api;
    }
}
