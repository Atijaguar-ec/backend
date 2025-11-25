package com.abelium.inatrace.db.entities.codebook;

import com.abelium.inatrace.db.base.TimestampEntity;
import com.abelium.inatrace.types.Language;
import jakarta.persistence.*;

/**
 * Translation entity for ShrimpPresentationType.
 *
 * @author INATrace Team
 */
@Entity
@Table(name = "ShrimpPresentationTypeTranslation",
       uniqueConstraints = @UniqueConstraint(columnNames = {"shrimp_presentation_type_id", "language"}))
public class ShrimpPresentationTypeTranslation extends TimestampEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shrimp_presentation_type_id", nullable = false)
    private ShrimpPresentationType shrimpPresentationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private Language language;

    @Column(nullable = false, length = 255)
    private String label;

    @Column(length = 255)
    private String description;

    public ShrimpPresentationType getShrimpPresentationType() {
        return shrimpPresentationType;
    }

    public void setShrimpPresentationType(ShrimpPresentationType shrimpPresentationType) {
        this.shrimpPresentationType = shrimpPresentationType;
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
