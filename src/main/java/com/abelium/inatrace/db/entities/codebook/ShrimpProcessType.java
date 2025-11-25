package com.abelium.inatrace.db.entities.codebook;

import com.abelium.inatrace.api.types.Lengths;
import com.abelium.inatrace.db.base.TimestampEntity;
import com.abelium.inatrace.db.enums.CodebookStatus;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Codebook entity for shrimp process types.
 * Types: HEAD_ON (Con Cabeza), SHELL_ON (Cola con Cáscara), VALUE_ADDED (Valor Agregado)
 *
 * @author INATrace Team
 */
@Entity
@Table(name = "ShrimpProcessType")
@NamedQueries({
    @NamedQuery(name = "ShrimpProcessType.listAll",
                query = "SELECT spt FROM ShrimpProcessType spt ORDER BY spt.displayOrder"),
    @NamedQuery(name = "ShrimpProcessType.listActive",
                query = "SELECT spt FROM ShrimpProcessType spt WHERE spt.status = 'ACTIVE' ORDER BY spt.displayOrder")
})
public class ShrimpProcessType extends TimestampEntity {

    /**
     * Unique code identifier (e.g., "HEAD_ON", "SHELL_ON", "VALUE_ADDED").
     */
    @Column(nullable = false, unique = true, length = Lengths.UID)
    private String code;

    /**
     * Default name in English.
     */
    @Column(nullable = false, length = Lengths.DEFAULT)
    private String name;

    /**
     * Description of the process type.
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
    @OneToMany(mappedBy = "shrimpProcessType", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<ShrimpProcessTypeTranslation> translations;

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

    public Set<ShrimpProcessTypeTranslation> getTranslations() {
        if (translations == null) {
            translations = new HashSet<>();
        }
        return translations;
    }

    public void setTranslations(Set<ShrimpProcessTypeTranslation> translations) {
        this.translations = translations;
    }
}
