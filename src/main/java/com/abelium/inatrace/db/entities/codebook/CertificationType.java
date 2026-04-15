package com.abelium.inatrace.db.entities.codebook;

import com.abelium.inatrace.db.base.TimestampEntity;
import com.abelium.inatrace.db.enums.CertificationCategory;
import com.abelium.inatrace.db.enums.CertificationStatus;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity for certification types (e.g. Organic, UTZ, Fairtrade).
 */
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"code"}, name = "uk_certification_type_code")
})
@NamedQueries({
        @NamedQuery(name = "CertificationType.findByCode",
                query = "SELECT c FROM CertificationType c WHERE c.code = :code"),
        @NamedQuery(name = "CertificationType.findAllActive",
                query = "SELECT c FROM CertificationType c WHERE c.status = 'ACTIVE' ORDER BY c.code ASC")
})
public class CertificationType extends TimestampEntity {

    @Column(nullable = false, length = 100)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CertificationCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CertificationStatus status;

    @OneToMany(mappedBy = "certificationType", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CertificationTypeTranslation> translations = new HashSet<>();

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
        return translations;
    }

    public void setTranslations(Set<CertificationTypeTranslation> translations) {
        this.translations = translations;
    }
}
