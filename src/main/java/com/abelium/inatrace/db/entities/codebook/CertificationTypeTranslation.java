package com.abelium.inatrace.db.entities.codebook;

import com.abelium.inatrace.db.base.BaseEntity;
import com.abelium.inatrace.types.Language;
import jakarta.persistence.*;

/**
 * i18n entity for certification types.
 */
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"certification_type_id", "language"}, name = "uk_cert_type_trans_lang")
})
public class CertificationTypeTranslation extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Language language;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "certification_type_id", nullable = false)
    private CertificationType certificationType;

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

    public CertificationType getCertificationType() {
        return certificationType;
    }

    public void setCertificationType(CertificationType certificationType) {
        this.certificationType = certificationType;
    }
}
