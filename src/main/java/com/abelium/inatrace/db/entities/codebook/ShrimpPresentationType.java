package com.abelium.inatrace.db.entities.codebook;

import com.abelium.inatrace.api.types.Lengths;
import com.abelium.inatrace.db.base.TimestampEntity;
import com.abelium.inatrace.db.enums.CodebookStatus;
import com.abelium.inatrace.db.enums.ShrimpPresentationCategory;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Codebook entity for shrimp presentation types.
 * Used in technical liquidation sheets for final product classification.
 * Categories: SHELL_ON (A/B), BROKEN (Large/Medium/Small), OTHER (TITI/ROJO/BVS)
 *
 * @author INATrace Team
 */
@Entity
@Table(name = "ShrimpPresentationType")
@NamedQueries({
    @NamedQuery(name = "ShrimpPresentationType.listAll",
                query = "SELECT spt FROM ShrimpPresentationType spt ORDER BY spt.category, spt.displayOrder"),
    @NamedQuery(name = "ShrimpPresentationType.listActive",
                query = "SELECT spt FROM ShrimpPresentationType spt WHERE spt.status = 'ACTIVE' ORDER BY spt.category, spt.displayOrder"),
    @NamedQuery(name = "ShrimpPresentationType.listByCategory",
                query = "SELECT spt FROM ShrimpPresentationType spt WHERE spt.category = :category AND spt.status = 'ACTIVE' ORDER BY spt.displayOrder")
})
public class ShrimpPresentationType extends TimestampEntity {

    /**
     * Unique code identifier (e.g., "SHELL_ON_A", "BROKEN_LARGE", "OTHER_TITI").
     */
    @Column(nullable = false, unique = true, length = Lengths.UID)
    private String code;

    /**
     * Display label (e.g., "Shell-On A", "Broken Large", "TITI").
     */
    @Column(nullable = false, length = Lengths.DEFAULT)
    private String label;

    /**
     * Category grouping: SHELL_ON, BROKEN, OTHER.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = Lengths.ENUM)
    private ShrimpPresentationCategory category;

    /**
     * Description of the presentation type.
     */
    @Column(length = Lengths.DEFAULT)
    private String description;

    /**
     * Display order within category.
     */
    @Column
    private Integer displayOrder;

    /**
     * Status: ACTIVE or INACTIVE.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = Lengths.ENUM)
    private CodebookStatus status;

    /**
     * Translations for multi-language support.
     */
    @OneToMany(mappedBy = "shrimpPresentationType", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<ShrimpPresentationTypeTranslation> translations;

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

    public ShrimpPresentationCategory getCategory() {
        return category;
    }

    public void setCategory(ShrimpPresentationCategory category) {
        this.category = category;
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

    public Set<ShrimpPresentationTypeTranslation> getTranslations() {
        if (translations == null) {
            translations = new HashSet<>();
        }
        return translations;
    }

    public void setTranslations(Set<ShrimpPresentationTypeTranslation> translations) {
        this.translations = translations;
    }
}
