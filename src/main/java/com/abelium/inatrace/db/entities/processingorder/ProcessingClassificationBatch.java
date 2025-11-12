package com.abelium.inatrace.db.entities.processingorder;

import com.abelium.inatrace.db.base.TimestampEntity;
import com.abelium.inatrace.db.entities.stockorder.StockOrder;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity for Shrimp Classification Batch header.
 * Stores general information about a classification batch linked to a target stock order.
 * 
 * @author INATrace Team
 */
@Entity
@Table(name = "ProcessingClassificationBatch")
public class ProcessingClassificationBatch extends TimestampEntity {

    @Version
    private Long entityVersion;

    /**
     * Reference to the target stock order (output) from the processing order.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "targetStockOrderId", nullable = false)
    private StockOrder targetStockOrder;

    /**
     * Start time (HH:MM format).
     */
    @Column(name = "startTime", length = 10)
    private String startTime;

    /**
     * End time (HH:MM format).
     */
    @Column(name = "endTime", length = 10)
    private String endTime;

    /**
     * Production order number.
     */
    @Column(name = "productionOrder", length = 100)
    private String productionOrder;

    /**
     * Freezing type (e.g., IQF, Block).
     */
    @Column(name = "freezingType", length = 50)
    private String freezingType;

    /**
     * Machine used for classification.
     */
    @Column(name = "machine", length = 50)
    private String machine;

    /**
     * Brand (header level).
     */
    @Column(name = "brandHeader", length = 100)
    private String brandHeader;

    /**
     * Details of the classification batch (by size).
     */
    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<ProcessingClassificationBatchDetail> details;

    // =====================================================
    // Getters and Setters
    // =====================================================

    public StockOrder getTargetStockOrder() {
        return targetStockOrder;
    }

    public void setTargetStockOrder(StockOrder targetStockOrder) {
        this.targetStockOrder = targetStockOrder;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getProductionOrder() {
        return productionOrder;
    }

    public void setProductionOrder(String productionOrder) {
        this.productionOrder = productionOrder;
    }

    public String getFreezingType() {
        return freezingType;
    }

    public void setFreezingType(String freezingType) {
        this.freezingType = freezingType;
    }

    public String getMachine() {
        return machine;
    }

    public void setMachine(String machine) {
        this.machine = machine;
    }

    public String getBrandHeader() {
        return brandHeader;
    }

    public void setBrandHeader(String brandHeader) {
        this.brandHeader = brandHeader;
    }

    public Set<ProcessingClassificationBatchDetail> getDetails() {
        if (details == null) {
            details = new HashSet<>();
        }
        return details;
    }

    public void setDetails(Set<ProcessingClassificationBatchDetail> details) {
        this.details = details;
    }
}
