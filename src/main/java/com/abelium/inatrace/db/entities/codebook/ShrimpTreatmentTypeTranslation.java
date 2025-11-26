package com.abelium.inatrace.db.entities.codebook;

import com.abelium.inatrace.db.base.TimestampEntity;
import com.abelium.inatrace.types.Language;
import jakarta.persistence.*;

/**
 * Translation entity for ShrimpTreatmentType.
 *
 * @author INATrace Team
 */
@Entity
@Table(name = "ShrimpTreatmentTypeTranslation")
public class ShrimpTreatmentTypeTranslation extends TimestampEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shrimp_treatment_type_id", nullable = false)
    private ShrimpTreatmentType shrimpTreatmentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private Language language;

    @Column(length = 100)
    private String label;

    @Column(length = 500)
    private String description;

    // Getters and Setters

    public ShrimpTreatmentType getShrimpTreatmentType() {
        return shrimpTreatmentType;
    }

    public void setShrimpTreatmentType(ShrimpTreatmentType shrimpTreatmentType) {
        this.shrimpTreatmentType = shrimpTreatmentType;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
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
}
