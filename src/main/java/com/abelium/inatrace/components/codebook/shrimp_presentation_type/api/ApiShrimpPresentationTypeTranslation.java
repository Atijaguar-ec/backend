package com.abelium.inatrace.components.codebook.shrimp_presentation_type.api;

import com.abelium.inatrace.types.Language;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

/**
 * API model for shrimp presentation type translation.
 *
 * @author INATrace Team
 */
@Validated
public class ApiShrimpPresentationTypeTranslation {

    @Schema(description = "Language code", example = "ES")
    private Language language;

    @Schema(description = "Translated label", example = "Cola con Cáscara A")
    private String label;

    @Schema(description = "Translated description")
    private String description;

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
