package com.abelium.inatrace.db.entities.codebook;

import com.abelium.inatrace.api.types.Lengths;
import com.abelium.inatrace.db.base.TimestampEntity;
import com.abelium.inatrace.db.enums.CertificationCategory;
import com.abelium.inatrace.db.enums.CertificationStatus;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Codebook entity for certifications and environmental seals.
 *
 * @author INATrace Development Team
 */
@Entity
@Table(name = "certification_type")
@NamedQueries({
    @NamedQuery(name = "CertificationType.listAll",
                query = "SELECT ct FROM CertificationType ct ORDER BY ct.name"),
    @NamedQuery(name = "CertificationType.countAll",
                query = "SELECT COUNT(ct) FROM CertificationType ct"),
    @NamedQuery(name = "CertificationType.listActive",
                query = "SELECT ct FROM CertificationType ct WHERE ct.status = 'ACTIVE' ORDER BY ct.name"),
    @NamedQuery(name = "CertificationType.countActive",
                query = "SELECT COUNT(ct) FROM CertificationType ct WHERE ct.status = 'ACTIVE'")
})
public class CertificationType extends TimestampEntity {

    /**
     * Unique code identifier for the certification type.
     */
    @Column(nullable = false, unique = true)
    private String code;

    /**
     * Default name (English).
     */
    @Column(nullable = false, length = Lengths.DEFAULT)
    private String name;

    /**
     * Category: CERTIFICATE or SEAL.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = Lengths.ENUM)
    private CertificationCategory category;

    /**
     * Status: ACTIVE or INACTIVE.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = Lengths.ENUM)
    private CertificationStatus status;

    /**
     * Translations for multi-language support.
     */
    @OneToMany(mappedBy = "certificationType", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<CertificationTypeTranslation> translations;

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

    public Set<CertificationTypeTranslation> getTranslations() {
        if (translations == null) {
            translations = new HashSet<>();
        }
        return translations;
    }

    public void setTranslations(Set<CertificationTypeTranslation> translations) {
        this.translations = translations;
    }
}
