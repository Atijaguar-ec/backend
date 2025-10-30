package com.abelium.inatrace.components.codebook.certification_type.api;

import com.abelium.inatrace.api.ApiCodebookBaseEntity;
import com.abelium.inatrace.db.enums.CertificationCategory;
import com.abelium.inatrace.db.enums.CertificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * Certification type API model.
 *
 * @author INATrace Development Team
 */
@Validated
public class ApiCertificationType extends ApiCodebookBaseEntity {

    @Schema(description = "Category of certification (CERTIFICATE or SEAL)")
    private CertificationCategory category;

    @Schema(description = "Status of certification (ACTIVE or INACTIVE)")
    private CertificationStatus status;

    @Schema(description = "Translations for certification type")
    private List<ApiCertificationTypeTranslation> translations;

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
        if (translations == null) {
            translations = new ArrayList<>();
        }
        return translations;
    }

    public void setTranslations(List<ApiCertificationTypeTranslation> translations) {
        this.translations = translations;
    }
}
