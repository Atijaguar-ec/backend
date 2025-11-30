package com.abelium.inatrace.components.codebook.shrimp_brand;

import com.abelium.inatrace.components.codebook.shrimp_brand.api.ApiShrimpBrand;
import com.abelium.inatrace.components.codebook.shrimp_brand.api.ApiShrimpBrandTranslation;
import com.abelium.inatrace.db.entities.codebook.ShrimpBrand;
import com.abelium.inatrace.db.entities.codebook.ShrimpBrandTranslation;
import com.abelium.inatrace.types.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Mapper for ShrimpBrand entity to API DTO.
 */
public final class ShrimpBrandMapper {

    private ShrimpBrandMapper() {}

    public static ApiShrimpBrand toApiShrimpBrand(ShrimpBrand entity) {
        return toApiShrimpBrand(entity, null);
    }

    public static ApiShrimpBrand toApiShrimpBrand(ShrimpBrand entity, Language language) {
        if (entity == null) return null;

        ApiShrimpBrand api = new ApiShrimpBrand();
        api.setId(entity.getId());
        api.setCode(entity.getCode());
        api.setWeightPerBox(entity.getWeightPerBox());
        api.setMeasureUnit(entity.getMeasureUnit());
        api.setDisplayOrder(entity.getDisplayOrder());
        api.setStatus(entity.getStatus());

        // If language specified, get translated values
        if (language != null && entity.getTranslations() != null) {
            String langCode = language.name().toLowerCase();
            Optional<ShrimpBrandTranslation> translation = entity.getTranslations().stream()
                    .filter(t -> t.getLanguage() != null && t.getLanguage().equalsIgnoreCase(langCode))
                    .findFirst();

            if (translation.isPresent()) {
                api.setLabel(translation.get().getLabel());
                api.setDescription(translation.get().getDescription());
            } else {
                // Fallback to English
                Optional<ShrimpBrandTranslation> englishTranslation = entity.getTranslations().stream()
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
            List<ApiShrimpBrandTranslation> apiTranslations = new ArrayList<>();
            for (ShrimpBrandTranslation t : entity.getTranslations()) {
                ApiShrimpBrandTranslation apiT = new ApiShrimpBrandTranslation();
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
