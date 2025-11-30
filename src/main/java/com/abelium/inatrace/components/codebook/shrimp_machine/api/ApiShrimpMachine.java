package com.abelium.inatrace.components.codebook.shrimp_machine.api;

import com.abelium.inatrace.api.ApiCodebookBaseEntity;
import com.abelium.inatrace.db.enums.CodebookStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * API DTO for ShrimpMachine catalog.
 */
@Validated
public class ApiShrimpMachine extends ApiCodebookBaseEntity {

    @Schema(description = "Unique code (MACH_01, GLAZER_01, etc.)")
    private String code;

    @Schema(description = "Machine name/label")
    private String label;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Machine type (CLASSIFIER, FREEZER, GLAZER, PACKER)")
    private String machineType;

    @Schema(description = "Display order")
    private Integer displayOrder;

    @Schema(description = "Status")
    private CodebookStatus status;

    @Schema(description = "Translations")
    private List<ApiShrimpMachineTranslation> translations;

    // Getters and Setters

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getMachineType() {
        return machineType;
    }

    public void setMachineType(String machineType) {
        this.machineType = machineType;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public CodebookStatus getStatus() {
        return status;
    }

    public void setStatus(CodebookStatus status) {
        this.status = status;
    }

    public List<ApiShrimpMachineTranslation> getTranslations() {
        return translations;
    }

    public void setTranslations(List<ApiShrimpMachineTranslation> translations) {
        this.translations = translations;
    }
}
