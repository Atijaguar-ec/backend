package com.abelium.inatrace.components.codebook.shrimp_process_type.api;

import com.abelium.inatrace.types.Language;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

/**
 * Shrimp process type translation API model.
 *
 * @author INATrace Team
 */
@Validated
public class ApiShrimpProcessTypeTranslation {

    @Schema(description = "Language of the translation")
    private Language language;

    @Schema(description = "Translated name")
    private String name;

    @Schema(description = "Translated description")
    private String description;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
