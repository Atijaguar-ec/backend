package com.abelium.inatrace.db.entities.fieldinspection;

import com.abelium.inatrace.db.base.TimestampEntity;
import com.abelium.inatrace.db.entities.common.User;
import com.abelium.inatrace.db.entities.common.UserCustomer;
import com.abelium.inatrace.db.entities.company.Company;
import com.abelium.inatrace.db.entities.stockorder.StockOrder;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Field Inspection entity for storing sensorial inspection results
 * performed at shrimp farms (field inspection facilities).
 * 
 * These inspections are performed before the product arrives at the packing plant
 * and can be linked to delivery stock orders when the product is received.
 * 
 * @author INATrace Team
 */
@Entity
@Table(name = "FieldInspection")
public class FieldInspection extends TimestampEntity {

    // =========================================================================
    // AUDIT FIELDS
    // =========================================================================

    /**
     * User who created this inspection record
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    /**
     * User who last updated this inspection record
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_id")
    private User updatedBy;

    // =========================================================================
    // STOCK ORDER REFERENCES
    // =========================================================================

    /**
     * Source stock order where the inspection was performed.
     * This is the order created at the field inspection facility.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_stock_order_id", nullable = false)
    private StockOrder sourceStockOrder;

    /**
     * Destination stock order where this inspection is used.
     * When set, the inspection is no longer available for linking.
     * This is the order created at the packing plant.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_stock_order_id")
    private StockOrder destinationStockOrder;

    // =========================================================================
    // COMPANY REFERENCE
    // =========================================================================

    /**
     * Company that owns this inspection record
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // =========================================================================
    // INSPECTION DATA
    // =========================================================================

    /**
     * Date when the inspection was performed
     */
    @Column(name = "inspection_date", nullable = false)
    private Instant inspectionDate;

    /**
     * Time of inspection in HH:mm format
     */
    @Column(name = "inspection_time", length = 10)
    private String inspectionTime;

    // =========================================================================
    // PRODUCER INFORMATION (denormalized for display)
    // =========================================================================

    /**
     * Producer/supplier who delivered the product
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producer_user_customer_id")
    private UserCustomer producerUserCustomer;

    /**
     * Cached producer name for quick display without joins
     */
    @Column(name = "producer_name")
    private String producerName;

    // =========================================================================
    // SENSORIAL INSPECTION RESULTS
    // =========================================================================

    /**
     * Result of the flavor test: NORMAL or DEFECT
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "flavor_test_result", nullable = false, length = 20)
    private FlavorTestResult flavorTestResult;

    /**
     * ID of the flavor defect type (if result is DEFECT)
     */
    @Column(name = "flavor_defect_type_id")
    private Long flavorDefectTypeId;

    /**
     * Cached defect type code for display
     */
    @Column(name = "flavor_defect_type_code", length = 50)
    private String flavorDefectTypeCode;

    /**
     * Cached defect type label for display
     */
    @Column(name = "flavor_defect_type_label")
    private String flavorDefectTypeLabel;

    /**
     * Whether purchase is recommended based on inspection
     */
    @Column(name = "purchase_recommended", nullable = false)
    private Boolean purchaseRecommended = true;

    /**
     * Additional notes from the inspection
     */
    @Column(name = "inspection_notes", columnDefinition = "LONGTEXT")
    private String inspectionNotes;

    // =========================================================================
    // RECEPTION DATA (from source order)
    // =========================================================================

    /**
     * Number of gavetas (crates)
     */
    @Column(name = "number_of_gavetas")
    private Integer numberOfGavetas;

    /**
     * Number of bines (bins)
     */
    @Column(name = "number_of_bines")
    private Integer numberOfBines;

    /**
     * Number of piscinas (ponds) the product came from
     */
    @Column(name = "number_of_piscinas")
    private Integer numberOfPiscinas;

    /**
     * Guía de remisión number
     */
    @Column(name = "guia_remision_number", length = 100)
    private String guiaRemisionNumber;

    /**
     * Total quantity in units
     */
    @Column(name = "total_quantity", precision = 19, scale = 2)
    private BigDecimal totalQuantity;

    // =========================================================================
    // ENUMS
    // =========================================================================

    /**
     * Flavor test result enum
     */
    public enum FlavorTestResult {
        NORMAL,
        DEFECT
    }

    // =========================================================================
    // GETTERS AND SETTERS
    // =========================================================================

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

    public StockOrder getSourceStockOrder() {
        return sourceStockOrder;
    }

    public void setSourceStockOrder(StockOrder sourceStockOrder) {
        this.sourceStockOrder = sourceStockOrder;
    }

    public StockOrder getDestinationStockOrder() {
        return destinationStockOrder;
    }

    public void setDestinationStockOrder(StockOrder destinationStockOrder) {
        this.destinationStockOrder = destinationStockOrder;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Instant getInspectionDate() {
        return inspectionDate;
    }

    public void setInspectionDate(Instant inspectionDate) {
        this.inspectionDate = inspectionDate;
    }

    public String getInspectionTime() {
        return inspectionTime;
    }

    public void setInspectionTime(String inspectionTime) {
        this.inspectionTime = inspectionTime;
    }

    public UserCustomer getProducerUserCustomer() {
        return producerUserCustomer;
    }

    public void setProducerUserCustomer(UserCustomer producerUserCustomer) {
        this.producerUserCustomer = producerUserCustomer;
    }

    public String getProducerName() {
        return producerName;
    }

    public void setProducerName(String producerName) {
        this.producerName = producerName;
    }

    public FlavorTestResult getFlavorTestResult() {
        return flavorTestResult;
    }

    public void setFlavorTestResult(FlavorTestResult flavorTestResult) {
        this.flavorTestResult = flavorTestResult;
    }

    public Long getFlavorDefectTypeId() {
        return flavorDefectTypeId;
    }

    public void setFlavorDefectTypeId(Long flavorDefectTypeId) {
        this.flavorDefectTypeId = flavorDefectTypeId;
    }

    public String getFlavorDefectTypeCode() {
        return flavorDefectTypeCode;
    }

    public void setFlavorDefectTypeCode(String flavorDefectTypeCode) {
        this.flavorDefectTypeCode = flavorDefectTypeCode;
    }

    public String getFlavorDefectTypeLabel() {
        return flavorDefectTypeLabel;
    }

    public void setFlavorDefectTypeLabel(String flavorDefectTypeLabel) {
        this.flavorDefectTypeLabel = flavorDefectTypeLabel;
    }

    public Boolean getPurchaseRecommended() {
        return purchaseRecommended;
    }

    public void setPurchaseRecommended(Boolean purchaseRecommended) {
        this.purchaseRecommended = purchaseRecommended;
    }

    public String getInspectionNotes() {
        return inspectionNotes;
    }

    public void setInspectionNotes(String inspectionNotes) {
        this.inspectionNotes = inspectionNotes;
    }

    public Integer getNumberOfGavetas() {
        return numberOfGavetas;
    }

    public void setNumberOfGavetas(Integer numberOfGavetas) {
        this.numberOfGavetas = numberOfGavetas;
    }

    public Integer getNumberOfBines() {
        return numberOfBines;
    }

    public void setNumberOfBines(Integer numberOfBines) {
        this.numberOfBines = numberOfBines;
    }

    public Integer getNumberOfPiscinas() {
        return numberOfPiscinas;
    }

    public void setNumberOfPiscinas(Integer numberOfPiscinas) {
        this.numberOfPiscinas = numberOfPiscinas;
    }

    public String getGuiaRemisionNumber() {
        return guiaRemisionNumber;
    }

    public void setGuiaRemisionNumber(String guiaRemisionNumber) {
        this.guiaRemisionNumber = guiaRemisionNumber;
    }

    public BigDecimal getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(BigDecimal totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    /**
     * Check if this inspection is available for linking to a delivery
     */
    public boolean isAvailable() {
        return destinationStockOrder == null;
    }
}
