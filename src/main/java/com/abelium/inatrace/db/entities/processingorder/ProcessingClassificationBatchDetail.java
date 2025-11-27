package com.abelium.inatrace.db.entities.processingorder;

import com.abelium.inatrace.db.base.TimestampEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Entity for Shrimp Classification Batch Detail.
 * Stores detailed classification information by size and brand.
 * 
 * @author INATrace Team
 */
@Entity
@Table(name = "ProcessingClassificationBatchDetail")
public class ProcessingClassificationBatchDetail extends TimestampEntity {

    @Version
    private Long entityVersion;

    /**
     * Reference to the classification batch header.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batchId", nullable = false)
    private ProcessingClassificationBatch batch;

    /**
     * Brand specific to this detail line.
     */
    @Column(name = "brandDetail", length = 100)
    private String brandDetail;

    /**
     * Size classification (e.g., "16/20", "21/25").
     */
    @Column(name = "size", length = 20)
    private String size;

    /**
     * Number of boxes.
     */
    @Column(name = "boxes")
    private Integer boxes;

    /**
     * Classification U value.
     */
    @Column(name = "classificationU", precision = 10, scale = 2)
    private BigDecimal classificationU;

    /**
     * Classification # value.
     */
    @Column(name = "classificationNumber", precision = 10, scale = 2)
    private BigDecimal classificationNumber;

    /**
     * Weight per box.
     */
    @Column(name = "weightPerBox", precision = 10, scale = 2)
    private BigDecimal weightPerBox;

    /**
     * Weight format: "LB" or "KG".
     */
    @Column(name = "weightFormat", length = 10)
    private String weightFormat;

    // =====================================================
    // 🦐 Liquidación de Compra - Campos adicionales
    // =====================================================

    /**
     * Quality grade: A, B, or C.
     */
    @Column(name = "qualityGrade", length = 10)
    private String qualityGrade;

    /**
     * Presentation type: SHELL_ON_A, SHELL_ON_B, BROKEN_VS, TITI, etc.
     */
    @Column(name = "presentationType", length = 50)
    private String presentationType;

    /**
     * Price per pound (USD).
     */
    @Column(name = "pricePerPound", precision = 10, scale = 4)
    private BigDecimal pricePerPound;

    /**
     * Line total (calculated: pounds × pricePerPound).
     */
    @Column(name = "lineTotal", precision = 12, scale = 2)
    private BigDecimal lineTotal;

    // =====================================================
    // Calculated Fields (not persisted, calculated on-the-fly)
    // =====================================================

    /**
     * Calculate total weight: boxes × weightPerBox.
     * @return total weight
     */
    @Transient
    public BigDecimal getTotalWeight() {
        if (boxes == null || weightPerBox == null) {
            return BigDecimal.ZERO;
        }
        return weightPerBox.multiply(BigDecimal.valueOf(boxes));
    }

    /**
     * Calculate pounds per size.
     * If format is KG, convert to LB (multiply by 2.20462).
     * If format is LB, return total weight as is.
     * @return pounds per size
     */
    @Transient
    public BigDecimal getPoundsPerSize() {
        BigDecimal totalWeight = getTotalWeight();
        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        if ("KG".equalsIgnoreCase(weightFormat)) {
            // Convert kg to lb
            return totalWeight.multiply(new BigDecimal("2.20462"));
        } else {
            // Already in lb
            return totalWeight;
        }
    }

    // =====================================================
    // Getters and Setters
    // =====================================================

    public ProcessingClassificationBatch getBatch() {
        return batch;
    }

    public void setBatch(ProcessingClassificationBatch batch) {
        this.batch = batch;
    }

    public String getBrandDetail() {
        return brandDetail;
    }

    public void setBrandDetail(String brandDetail) {
        this.brandDetail = brandDetail;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Integer getBoxes() {
        return boxes;
    }

    public void setBoxes(Integer boxes) {
        this.boxes = boxes;
    }

    public BigDecimal getClassificationU() {
        return classificationU;
    }

    public void setClassificationU(BigDecimal classificationU) {
        this.classificationU = classificationU;
    }

    public BigDecimal getClassificationNumber() {
        return classificationNumber;
    }

    public void setClassificationNumber(BigDecimal classificationNumber) {
        this.classificationNumber = classificationNumber;
    }

    public BigDecimal getWeightPerBox() {
        return weightPerBox;
    }

    public void setWeightPerBox(BigDecimal weightPerBox) {
        this.weightPerBox = weightPerBox;
    }

    public String getWeightFormat() {
        return weightFormat;
    }

    public void setWeightFormat(String weightFormat) {
        this.weightFormat = weightFormat;
    }

    public String getQualityGrade() {
        return qualityGrade;
    }

    public void setQualityGrade(String qualityGrade) {
        this.qualityGrade = qualityGrade;
    }

    public String getPresentationType() {
        return presentationType;
    }

    public void setPresentationType(String presentationType) {
        this.presentationType = presentationType;
    }

    public BigDecimal getPricePerPound() {
        return pricePerPound;
    }

    public void setPricePerPound(BigDecimal pricePerPound) {
        this.pricePerPound = pricePerPound;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    /**
     * Calculate line total: pounds × pricePerPound.
     * @return calculated line total
     */
    @Transient
    public BigDecimal getCalculatedLineTotal() {
        BigDecimal pounds = getPoundsPerSize();
        if (pounds.compareTo(BigDecimal.ZERO) == 0 || pricePerPound == null) {
            return BigDecimal.ZERO;
        }
        return pounds.multiply(pricePerPound);
    }
}
