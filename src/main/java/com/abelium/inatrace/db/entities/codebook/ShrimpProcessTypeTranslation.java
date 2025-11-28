package com.abelium.inatrace.db.entities.codebook;

import com.abelium.inatrace.db.base.TimestampEntity;
import com.abelium.inatrace.types.Language;
import jakarta.persistence.*;

/**
 * Translation entity for ShrimpProcessType.
 *
 * @author INATrace Team
 */
@Entity
@Table(name = "ShrimpProcessTypeTranslation",
       uniqueConstraints = @UniqueConstraint(columnNames = {"shrimp_process_type_id", "language"}))
public class ShrimpProcessTypeTranslation extends TimestampEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shrimp_process_type_id", nullable = false)
    private ShrimpProcessType shrimpProcessType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private Language language;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 255)
    private String description;

    public ShrimpProcessType getShrimpProcessType() {
        return shrimpProcessType;
    }

    public void setShrimpProcessType(ShrimpProcessType shrimpProcessType) {
        this.shrimpProcessType = shrimpProcessType;
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
