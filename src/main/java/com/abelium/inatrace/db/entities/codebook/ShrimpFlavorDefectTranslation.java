package com.abelium.inatrace.db.entities.codebook;

import com.abelium.inatrace.db.base.TimestampEntity;
import com.abelium.inatrace.types.Language;
import jakarta.persistence.*;

/**
 * Translation entity for ShrimpFlavorDefect.
 *
 * @author INATrace Team
 */
@Entity
@Table(name = "ShrimpFlavorDefectTranslation",
       uniqueConstraints = @UniqueConstraint(columnNames = {"shrimp_flavor_defect_id", "language"}))
public class ShrimpFlavorDefectTranslation extends TimestampEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shrimp_flavor_defect_id", nullable = false)
    private ShrimpFlavorDefect shrimpFlavorDefect;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private Language language;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 255)
    private String description;

    public ShrimpFlavorDefect getShrimpFlavorDefect() {
        return shrimpFlavorDefect;
    }

    public void setShrimpFlavorDefect(ShrimpFlavorDefect shrimpFlavorDefect) {
        this.shrimpFlavorDefect = shrimpFlavorDefect;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
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
}
