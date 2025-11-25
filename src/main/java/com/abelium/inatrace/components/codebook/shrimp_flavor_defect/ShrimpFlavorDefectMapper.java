package com.abelium.inatrace.components.codebook.shrimp_flavor_defect;

import com.abelium.inatrace.components.codebook.shrimp_flavor_defect.api.ApiShrimpFlavorDefect;
import com.abelium.inatrace.components.codebook.shrimp_flavor_defect.api.ApiShrimpFlavorDefectTranslation;
import com.abelium.inatrace.db.entities.codebook.ShrimpFlavorDefect;
import com.abelium.inatrace.db.entities.codebook.ShrimpFlavorDefectTranslation;
import com.abelium.inatrace.types.Language;

import java.util.stream.Collectors;

/**
 * Mapper for ShrimpFlavorDefect entity.
 *
 * @author INATrace Team
 */
public final class ShrimpFlavorDefectMapper {

    private ShrimpFlavorDefectMapper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Mapping the base entity attributes - no associations are included.
     */
    public static ApiShrimpFlavorDefect toApiShrimpFlavorDefectBase(ShrimpFlavorDefect entity, Language language) {
        if (entity == null) {
            return null;
        }

        ShrimpFlavorDefectTranslation translation = entity.getTranslations().stream()
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

        ApiShrimpFlavorDefect api = new ApiShrimpFlavorDefect();
        api.setId(entity.getId());
        api.setCode(entity.getCode());
        api.setLabel(translation != null && translation.getName() != null ? translation.getName() : entity.getName());
        api.setDescription(translation != null && translation.getDescription() != null ? translation.getDescription() : entity.getDescription());
        api.setDisplayOrder(entity.getDisplayOrder());
        api.setStatus(entity.getStatus());

        return api;
    }

    /**
     * Mapping of the base attributes and all the associations.
     */
    public static ApiShrimpFlavorDefect toApiShrimpFlavorDefect(ShrimpFlavorDefect entity, Language language) {
        ApiShrimpFlavorDefect api = toApiShrimpFlavorDefectBase(entity, language);
        if (api == null) {
            return null;
        }

        api.setTranslations(
                entity.getTranslations().stream()
                        .map(ShrimpFlavorDefectMapper::toApiTranslation)
                        .collect(Collectors.toList())
        );

        return api;
    }

    public static ApiShrimpFlavorDefectTranslation toApiTranslation(ShrimpFlavorDefectTranslation translation) {
        ApiShrimpFlavorDefectTranslation api = new ApiShrimpFlavorDefectTranslation();
        api.setLanguage(translation.getLanguage());
        api.setName(translation.getName());
        api.setDescription(translation.getDescription());
        return api;
    }
}
