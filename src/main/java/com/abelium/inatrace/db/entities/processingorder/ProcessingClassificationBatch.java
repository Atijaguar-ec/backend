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
     * Provider/Supplier name (Formato DUFER: "Cam. Dietab").
     */
    @Column(name = "providerName", length = 200)
    private String providerName;

    // =====================================================
    // 🦐 Liquidación de Compra - Campos adicionales
    // =====================================================

    /**
     * Settlement number for purchase (e.g., "71694").
     */
    @Column(name = "settlementNumber", length = 50)
    private String settlementNumber;

    /**
     * Process type: HEAD_ON (Con Cabeza), SHELL_ON (En Cola), VALUE_ADDED.
     */
    @Column(name = "processType", length = 30)
    private String processType;

    /**
     * Pounds received from supplier.
     */
    @Column(name = "poundsReceived", precision = 12, scale = 2)
    private java.math.BigDecimal poundsReceived;

    /**
     * Pounds waste/trash.
     */
    @Column(name = "poundsWaste", precision = 12, scale = 2)
    private java.math.BigDecimal poundsWaste;

    /**
     * Net pounds received (poundsReceived - poundsWaste).
     */
    @Column(name = "poundsNetReceived", precision = 12, scale = 2)
    private java.math.BigDecimal poundsNetReceived;

    /**
     * Pounds processed (output).
     */
    @Column(name = "poundsProcessed", precision = 12, scale = 2)
    private java.math.BigDecimal poundsProcessed;

    /**
     * Yield percentage (poundsProcessed / poundsNetReceived * 100).
     */
    @Column(name = "yieldPercentage", precision = 5, scale = 2)
    private java.math.BigDecimal yieldPercentage;

    /**
     * Total amount to pay (sum of line totals).
     */
    @Column(name = "totalAmount", precision = 14, scale = 2)
    private java.math.BigDecimal totalAmount;

    /**
     * Average price per pound.
     */
    @Column(name = "averagePrice", precision = 10, scale = 4)
    private java.math.BigDecimal averagePrice;

    /**
     * Settlement status: DRAFT, APPROVED, PAID.
     */
    @Column(name = "settlementStatus", length = 20)
    private String settlementStatus;

    // =====================================================
    // 🦐 Rejected Output Support - Multi-output classification
    // =====================================================

    /**
     * Output type: PROCESSED (primary output to freezing) or REJECTED (secondary output to deheading).
     * Default is PROCESSED for backwards compatibility.
     */
    @Column(name = "output_type", length = 20)
    private String outputType = "PROCESSED";

    /**
     * Pounds rejected (sent to deheading facility).
     * Only relevant for PROCESSED type batches.
     */
    @Column(name = "pounds_rejected", precision = 12, scale = 2)
    private java.math.BigDecimal poundsRejected;

    /**
     * Reference to the secondary output stock order for rejected product.
     * This links the PROCESSED batch to its corresponding REJECTED output.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rejected_stock_order_id")
    private StockOrder rejectedStockOrder;

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

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    // 🦐 Liquidación de Compra - Getters and Setters

    public String getSettlementNumber() {
        return settlementNumber;
    }

    public void setSettlementNumber(String settlementNumber) {
        this.settlementNumber = settlementNumber;
    }

    public String getProcessType() {
        return processType;
    }

    public void setProcessType(String processType) {
        this.processType = processType;
    }

    public java.math.BigDecimal getPoundsReceived() {
        return poundsReceived;
    }

    public void setPoundsReceived(java.math.BigDecimal poundsReceived) {
        this.poundsReceived = poundsReceived;
    }

    public java.math.BigDecimal getPoundsWaste() {
        return poundsWaste;
    }

    public void setPoundsWaste(java.math.BigDecimal poundsWaste) {
        this.poundsWaste = poundsWaste;
    }

    public java.math.BigDecimal getPoundsNetReceived() {
        return poundsNetReceived;
    }

    public void setPoundsNetReceived(java.math.BigDecimal poundsNetReceived) {
        this.poundsNetReceived = poundsNetReceived;
    }

    public java.math.BigDecimal getPoundsProcessed() {
        return poundsProcessed;
    }

    public void setPoundsProcessed(java.math.BigDecimal poundsProcessed) {
        this.poundsProcessed = poundsProcessed;
    }

    public java.math.BigDecimal getYieldPercentage() {
        return yieldPercentage;
    }

    public void setYieldPercentage(java.math.BigDecimal yieldPercentage) {
        this.yieldPercentage = yieldPercentage;
    }

    public java.math.BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(java.math.BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public java.math.BigDecimal getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(java.math.BigDecimal averagePrice) {
        this.averagePrice = averagePrice;
    }

    public String getSettlementStatus() {
        return settlementStatus;
    }

    public void setSettlementStatus(String settlementStatus) {
        this.settlementStatus = settlementStatus;
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

    // 🦐 Rejected Output Support - Getters and Setters

    public String getOutputType() {
        return outputType;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    public java.math.BigDecimal getPoundsRejected() {
        return poundsRejected;
    }

    public void setPoundsRejected(java.math.BigDecimal poundsRejected) {
        this.poundsRejected = poundsRejected;
    }

    public StockOrder getRejectedStockOrder() {
        return rejectedStockOrder;
    }

    public void setRejectedStockOrder(StockOrder rejectedStockOrder) {
        this.rejectedStockOrder = rejectedStockOrder;
    }
}
