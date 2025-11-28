package com.abelium.inatrace.db.entities.codebook;

import com.abelium.inatrace.api.types.Lengths;
import com.abelium.inatrace.db.base.TimestampEntity;
import com.abelium.inatrace.db.enums.CodebookStatus;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Codebook entity for shrimp flavor defects detected during field sensory inspection.
 * Used in the "Inspección Sensorial en Campo" process.
 *
 * @author INATrace Team
 */
@Entity
@Table(name = "ShrimpFlavorDefect")
@NamedQueries({
    @NamedQuery(name = "ShrimpFlavorDefect.listAll",
                query = "SELECT sfd FROM ShrimpFlavorDefect sfd ORDER BY sfd.displayOrder"),
    @NamedQuery(name = "ShrimpFlavorDefect.listActive",
                query = "SELECT sfd FROM ShrimpFlavorDefect sfd WHERE sfd.status = 'ACTIVE' ORDER BY sfd.displayOrder")
})
public class ShrimpFlavorDefect extends TimestampEntity {

    /**
     * Unique code identifier (e.g., "ARENA", "PALO").
     */
    @Column(nullable = false, unique = true, length = Lengths.UID)
    private String code;

    /**
     * Default name in English.
     */
    @Column(nullable = false, length = Lengths.DEFAULT)
    private String name;

    /**
     * Description of the defect.
     */
    @Column(length = Lengths.DEFAULT)
    private String description;

    /**
     * Display order in lists/dropdowns.
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
    @OneToMany(mappedBy = "shrimpFlavorDefect", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<ShrimpFlavorDefectTranslation> translations;

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

    public Set<ShrimpFlavorDefectTranslation> getTranslations() {
        if (translations == null) {
            translations = new HashSet<>();
        }
        return translations;
    }

    public void setTranslations(Set<ShrimpFlavorDefectTranslation> translations) {
        this.translations = translations;
    }
}
