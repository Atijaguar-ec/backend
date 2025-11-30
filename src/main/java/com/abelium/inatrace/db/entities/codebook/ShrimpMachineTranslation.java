package com.abelium.inatrace.db.entities.codebook;

import com.abelium.inatrace.db.base.TimestampEntity;
import jakarta.persistence.*;

/**
 * Translation entity for ShrimpMachine.
 * Supports multi-language labels for machines.
 *
 * @author INATrace Team
 */
@Entity
@Table(name = "ShrimpMachineTranslation")
public class ShrimpMachineTranslation extends TimestampEntity {

    /**
     * Reference to the parent machine
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shrimp_machine_id", nullable = false)
    private ShrimpMachine shrimpMachine;

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

    public ShrimpMachine getShrimpMachine() {
        return shrimpMachine;
    }

    public void setShrimpMachine(ShrimpMachine shrimpMachine) {
        this.shrimpMachine = shrimpMachine;
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
