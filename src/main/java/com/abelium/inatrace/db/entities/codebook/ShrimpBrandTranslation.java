package com.abelium.inatrace.db.entities.codebook;

import com.abelium.inatrace.db.base.TimestampEntity;
import jakarta.persistence.*;

/**
 * Translation entity for ShrimpBrand.
 * Supports multi-language labels for brands.
 *
 * @author INATrace Team
 */
@Entity
@Table(name = "ShrimpBrandTranslation")
public class ShrimpBrandTranslation extends TimestampEntity {

    /**
     * Reference to the parent brand
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shrimp_brand_id", nullable = false)
    private ShrimpBrand shrimpBrand;

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

    public ShrimpBrand getShrimpBrand() {
        return shrimpBrand;
    }

    public void setShrimpBrand(ShrimpBrand shrimpBrand) {
        this.shrimpBrand = shrimpBrand;
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
