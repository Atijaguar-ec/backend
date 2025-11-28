package com.abelium.inatrace.db.entities.codebook;

import com.abelium.inatrace.api.types.Lengths;
import com.abelium.inatrace.db.base.TimestampEntity;
import com.abelium.inatrace.db.enums.CodebookStatus;
import com.abelium.inatrace.db.enums.ShrimpSizeType;
import jakarta.persistence.*;

/**
 * Codebook entity for shrimp size grades (counts per pound/kg).
 * Supports both whole shrimp (Head-On) and tail shrimp (Shell-On/Value Added) sizes.
 *
 * @author INATrace Team
 */
@Entity
@Table(name = "ShrimpSizeGrade")
@NamedQueries({
    @NamedQuery(name = "ShrimpSizeGrade.listAll",
                query = "SELECT ssg FROM ShrimpSizeGrade ssg ORDER BY ssg.sizeType, ssg.displayOrder"),
    @NamedQuery(name = "ShrimpSizeGrade.listActive",
                query = "SELECT ssg FROM ShrimpSizeGrade ssg WHERE ssg.status = 'ACTIVE' ORDER BY ssg.sizeType, ssg.displayOrder"),
    @NamedQuery(name = "ShrimpSizeGrade.listByType",
                query = "SELECT ssg FROM ShrimpSizeGrade ssg WHERE ssg.sizeType = :sizeType AND ssg.status = 'ACTIVE' ORDER BY ssg.displayOrder")
})
public class ShrimpSizeGrade extends TimestampEntity {

    /**
     * Unique code identifier (e.g., "WHOLE_30_40", "TAIL_21_25").
     */
    @Column(nullable = false, unique = true, length = Lengths.UID)
    private String code;

    /**
     * Display label (e.g., "30-40", "21-25", "U-10").
     */
    @Column(nullable = false, length = Lengths.DEFAULT)
    private String label;

    /**
     * Size type: WHOLE (Head-On) or TAIL (Shell-On/Value Added).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = Lengths.ENUM)
    private ShrimpSizeType sizeType;

    /**
     * Display order within size type.
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

    public ShrimpSizeType getSizeType() {
        return sizeType;
    }

    public void setSizeType(ShrimpSizeType sizeType) {
        this.sizeType = sizeType;
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
