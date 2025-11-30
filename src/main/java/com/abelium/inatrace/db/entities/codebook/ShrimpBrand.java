package com.abelium.inatrace.db.entities.codebook;

import com.abelium.inatrace.db.base.TimestampEntity;
import com.abelium.inatrace.db.enums.CodebookStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity for shrimp brand catalog.
 * Represents brands used in shrimp classification and packing.
 * Each brand includes the default weight per box and measure unit.
 *
 * @author INATrace Team
 */
@Entity
@Table(name = "ShrimpBrand")
public class ShrimpBrand extends TimestampEntity {

    /**
     * Unique code (e.g., "DUFER_5LB", "OCEAN_GOLD_2KG")
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * Brand name/label
     */
    @Column(nullable = false, length = 100)
    private String label;

    /**
     * Description
     */
    @Column(length = 500)
    private String description;

    /**
     * Default weight per box for this brand
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal weightPerBox;

    /**
     * Measure unit for the weight (KG, LB)
     */
    @Column(length = 10)
    private String measureUnit;

    /**
     * Display order
     */
    @Column
    private Integer displayOrder;

    /**
     * Status (ACTIVE/INACTIVE)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CodebookStatus status = CodebookStatus.ACTIVE;

    /**
     * Translations
     */
    @OneToMany(mappedBy = "shrimpBrand", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShrimpBrandTranslation> translations = new ArrayList<>();

    // Getters and Setters

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public BigDecimal getWeightPerBox() {
        return weightPerBox;
    }

    public void setWeightPerBox(BigDecimal weightPerBox) {
        this.weightPerBox = weightPerBox;
    }

    public String getMeasureUnit() {
        return measureUnit;
    }

    public void setMeasureUnit(String measureUnit) {
        this.measureUnit = measureUnit;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public CodebookStatus getStatus() {
        return status;
    }

    public void setStatus(CodebookStatus status) {
        this.status = status;
    }

    public List<ShrimpBrandTranslation> getTranslations() {
        return translations;
    }

    public void setTranslations(List<ShrimpBrandTranslation> translations) {
        this.translations = translations;
    }
}
