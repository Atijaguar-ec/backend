package com.abelium.inatrace.db.entities.codebook;

import com.abelium.inatrace.db.base.TimestampEntity;
import com.abelium.inatrace.db.enums.CodebookStatus;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity for shrimp quality grade catalog.
 * Represents quality classifications: A (First), B (Second), C (Other).
 * Used in purchase settlements to group products by quality.
 *
 * @author INATrace Team
 */
@Entity
@Table(name = "ShrimpQualityGrade")
public class ShrimpQualityGrade extends TimestampEntity {

    /**
     * Unique code (A, B, C)
     */
    @Column(nullable = false, unique = true, length = 10)
    private String code;

    /**
     * Default label (English)
     */
    @Column(nullable = false, length = 100)
    private String label;

    /**
     * Description
     */
    @Column(length = 500)
    private String description;

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
    @OneToMany(mappedBy = "shrimpQualityGrade", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShrimpQualityGradeTranslation> translations = new ArrayList<>();

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

    public List<ShrimpQualityGradeTranslation> getTranslations() {
        return translations;
    }

    public void setTranslations(List<ShrimpQualityGradeTranslation> translations) {
        this.translations = translations;
    }
}
