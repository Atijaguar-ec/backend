package com.abelium.inatrace.components.codebook.shrimp_treatment_type.api;

import com.abelium.inatrace.types.Language;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * API DTO for ShrimpTreatmentType translations.
 */
public class ApiShrimpTreatmentTypeTranslation {

    @Schema(description = "Language code")
    private Language language;

    @Schema(description = "Translated label")
    private String label;

    @Schema(description = "Translated description")
    private String description;

    // Getters and Setters

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
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
