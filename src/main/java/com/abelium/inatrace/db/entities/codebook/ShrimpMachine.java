package com.abelium.inatrace.db.entities.codebook;

import com.abelium.inatrace.db.base.TimestampEntity;
import com.abelium.inatrace.db.enums.CodebookStatus;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity for shrimp processing machine catalog.
 * Represents machines used in shrimp processing and classification.
 *
 * @author INATrace Team
 */
@Entity
@Table(name = "ShrimpMachine")
public class ShrimpMachine extends TimestampEntity {

    /**
     * Unique code (e.g., "MACH_001", "GLAZER_01")
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * Machine name/label
     */
    @Column(nullable = false, length = 100)
    private String label;

    /**
     * Description of machine capabilities
     */
    @Column(length = 500)
    private String description;

    /**
     * Machine type (e.g., "CLASSIFIER", "FREEZER", "GLAZER", "PACKER")
     */
    @Column(length = 50)
    private String machineType;

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
    @OneToMany(mappedBy = "shrimpMachine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShrimpMachineTranslation> translations = new ArrayList<>();

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

    public String getMachineType() {
        return machineType;
    }

    public void setMachineType(String machineType) {
        this.machineType = machineType;
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

    public List<ShrimpMachineTranslation> getTranslations() {
        return translations;
    }

    public void setTranslations(List<ShrimpMachineTranslation> translations) {
        this.translations = translations;
    }
}
