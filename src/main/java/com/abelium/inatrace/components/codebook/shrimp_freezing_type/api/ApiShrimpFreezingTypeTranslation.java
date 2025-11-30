package com.abelium.inatrace.components.codebook.shrimp_freezing_type.api;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * API DTO for ShrimpFreezingType translations.
 */
public class ApiShrimpFreezingTypeTranslation {

    @Schema(description = "Language code (EN, ES)")
    private String language;

    @Schema(description = "Translated label")
    private String label;

    @Schema(description = "Translated description")
    private String description;

    // Getters and Setters

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
