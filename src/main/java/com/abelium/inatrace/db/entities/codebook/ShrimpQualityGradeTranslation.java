package com.abelium.inatrace.db.entities.codebook;

import com.abelium.inatrace.db.base.TimestampEntity;
import com.abelium.inatrace.types.Language;
import jakarta.persistence.*;

/**
 * Translation entity for ShrimpQualityGrade.
 *
 * @author INATrace Team
 */
@Entity
@Table(name = "ShrimpQualityGradeTranslation")
public class ShrimpQualityGradeTranslation extends TimestampEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shrimp_quality_grade_id", nullable = false)
    private ShrimpQualityGrade shrimpQualityGrade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private Language language;

    @Column(length = 100)
    private String label;

    @Column(length = 500)
    private String description;

    // Getters and Setters

    public ShrimpQualityGrade getShrimpQualityGrade() {
        return shrimpQualityGrade;
    }

    public void setShrimpQualityGrade(ShrimpQualityGrade shrimpQualityGrade) {
        this.shrimpQualityGrade = shrimpQualityGrade;
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
