package com.abelium.inatrace.db.entities.codebook;

import com.abelium.inatrace.db.base.TimestampEntity;
import com.abelium.inatrace.types.Language;
import jakarta.persistence.*;

/**
 * Translation entity for CertificationType.
 *
 * @author INATrace Development Team
 */
@Entity
@Table(name = "certification_type_translation",
       uniqueConstraints = @UniqueConstraint(columnNames = {"certification_type_id", "language"}))
public class CertificationTypeTranslation extends TimestampEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_type_id", nullable = false)
    private CertificationType certificationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Language language;

    @Column(nullable = false, length = 500)
    private String name;

    public CertificationType getCertificationType() {
        return certificationType;
    }

    public void setCertificationType(CertificationType certificationType) {
        this.certificationType = certificationType;
    }

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
