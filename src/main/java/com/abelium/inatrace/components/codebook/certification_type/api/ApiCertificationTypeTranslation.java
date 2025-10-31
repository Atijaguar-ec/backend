package com.abelium.inatrace.components.codebook.certification_type.api;

import com.abelium.inatrace.types.Language;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Certification type translation API model.
 *
 * @author Álvaro Sánchez
 */
public class ApiCertificationTypeTranslation {

    @Schema(description = "Language code")
    private Language language;

    @Schema(description = "Translated name")
    private String name;

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
