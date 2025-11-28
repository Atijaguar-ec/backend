package com.abelium.inatrace.db.entities.codebook;

import com.abelium.inatrace.api.types.Lengths;
import com.abelium.inatrace.db.base.TimestampEntity;
import com.abelium.inatrace.db.enums.CodebookStatus;
import jakarta.persistence.*;

/**
 * Codebook entity for shrimp color grades.
 * Color grades: A-1, A-2, A-3, A-4
 *
 * @author INATrace Team
 */
@Entity
@Table(name = "ShrimpColorGrade")
@NamedQueries({
    @NamedQuery(name = "ShrimpColorGrade.listAll",
                query = "SELECT scg FROM ShrimpColorGrade scg ORDER BY scg.displayOrder"),
    @NamedQuery(name = "ShrimpColorGrade.listActive",
                query = "SELECT scg FROM ShrimpColorGrade scg WHERE scg.status = 'ACTIVE' ORDER BY scg.displayOrder")
})
public class ShrimpColorGrade extends TimestampEntity {

    /**
     * Unique code identifier (e.g., "A_1", "A_2").
     */
    @Column(nullable = false, unique = true, length = Lengths.UID)
    private String code;

    /**
     * Display label (e.g., "A-1", "A-2").
     */
    @Column(nullable = false, length = Lengths.DEFAULT)
    private String label;

    /**
     * Description of the color grade.
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
}
