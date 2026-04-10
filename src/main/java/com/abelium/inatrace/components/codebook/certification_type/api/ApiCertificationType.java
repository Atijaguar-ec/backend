package com.abelium.inatrace.components.codebook.certification_type.api;

import com.abelium.inatrace.api.ApiBaseEntity;
import com.abelium.inatrace.db.enums.CertificationCategory;
import com.abelium.inatrace.db.enums.CertificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class ApiCertificationType extends ApiBaseEntity {

    @Schema(description = "Code")
    private String code;

    @Schema(description = "Name")
    private String name;

    @Schema(description = "Category")
    private CertificationCategory category;

    @Schema(description = "Status")
    private CertificationStatus status;

    @Schema(description = "Translations")
    private List<ApiCertificationTypeTranslation> translations;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CertificationCategory getCategory() {
        return category;
    }

    public void setCategory(CertificationCategory category) {
        this.category = category;
    }

    public CertificationStatus getStatus() {
        return status;
    }

    public void setStatus(CertificationStatus status) {
        this.status = status;
    }

    public List<ApiCertificationTypeTranslation> getTranslations() {
        return translations;
    }

    public void setTranslations(List<ApiCertificationTypeTranslation> translations) {
        this.translations = translations;
    }
}
