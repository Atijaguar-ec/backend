package com.abelium.inatrace.components.codebook.certification_type.api;

import com.abelium.inatrace.types.Language;
import io.swagger.v3.oas.annotations.media.Schema;

public class ApiCertificationTypeTranslation {

    @Schema(description = "Translation name")
    private String name;

    @Schema(description = "Translation language")
    private Language language;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }
}
