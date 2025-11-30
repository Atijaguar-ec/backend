package com.abelium.inatrace.components.codebook.shrimp_machine;

import com.abelium.inatrace.components.codebook.shrimp_machine.api.ApiShrimpMachine;
import com.abelium.inatrace.components.codebook.shrimp_machine.api.ApiShrimpMachineTranslation;
import com.abelium.inatrace.db.entities.codebook.ShrimpMachine;
import com.abelium.inatrace.db.entities.codebook.ShrimpMachineTranslation;
import com.abelium.inatrace.types.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Mapper for ShrimpMachine entity to API DTO.
 */
public final class ShrimpMachineMapper {

    private ShrimpMachineMapper() {}

    public static ApiShrimpMachine toApiShrimpMachine(ShrimpMachine entity) {
        return toApiShrimpMachine(entity, null);
    }

    public static ApiShrimpMachine toApiShrimpMachine(ShrimpMachine entity, Language language) {
        if (entity == null) return null;

        ApiShrimpMachine api = new ApiShrimpMachine();
        api.setId(entity.getId());
        api.setCode(entity.getCode());
        api.setMachineType(entity.getMachineType());
        api.setDisplayOrder(entity.getDisplayOrder());
        api.setStatus(entity.getStatus());

        // If language specified, get translated values
        if (language != null && entity.getTranslations() != null) {
            String langCode = language.name().toLowerCase();
            Optional<ShrimpMachineTranslation> translation = entity.getTranslations().stream()
                    .filter(t -> t.getLanguage() != null && t.getLanguage().equalsIgnoreCase(langCode))
                    .findFirst();

            if (translation.isPresent()) {
                api.setLabel(translation.get().getLabel());
                api.setDescription(translation.get().getDescription());
            } else {
                // Fallback to English
                Optional<ShrimpMachineTranslation> englishTranslation = entity.getTranslations().stream()
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
            List<ApiShrimpMachineTranslation> apiTranslations = new ArrayList<>();
            for (ShrimpMachineTranslation t : entity.getTranslations()) {
                ApiShrimpMachineTranslation apiT = new ApiShrimpMachineTranslation();
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
