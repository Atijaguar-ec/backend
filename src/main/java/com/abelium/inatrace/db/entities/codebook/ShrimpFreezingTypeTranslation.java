package com.abelium.inatrace.db.entities.codebook;

import com.abelium.inatrace.db.base.TimestampEntity;
import jakarta.persistence.*;

/**
 * Translation entity for ShrimpFreezingType.
 * Supports multi-language labels for freezing types.
 *
 * @author INATrace Team
 */
@Entity
@Table(name = "ShrimpFreezingTypeTranslation")
public class ShrimpFreezingTypeTranslation extends TimestampEntity {

    /**
     * Reference to the parent freezing type
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shrimp_freezing_type_id", nullable = false)
    private ShrimpFreezingType shrimpFreezingType;

    /**
     * Language code (e.g., "en", "es")
     */
    @Column(nullable = false, length = 5)
    private String language;

    /**
     * Translated label
     */
    @Column(length = 100)
    private String label;

    /**
     * Translated description
     */
    @Column(length = 500)
    private String description;

    // Getters and Setters

    public ShrimpFreezingType getShrimpFreezingType() {
        return shrimpFreezingType;
    }

    public void setShrimpFreezingType(ShrimpFreezingType shrimpFreezingType) {
        this.shrimpFreezingType = shrimpFreezingType;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
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
