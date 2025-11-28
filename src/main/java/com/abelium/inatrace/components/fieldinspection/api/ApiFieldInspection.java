package com.abelium.inatrace.components.fieldinspection.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Field Inspection API model for sensorial inspections at shrimp farms.
 * 
 * @author INATrace Team
 */
@Schema(description = "Field inspection record for shrimp sensorial tests at farms")
public class ApiFieldInspection {

    // =========================================================================
    // BASIC FIELDS
    // =========================================================================

    @Schema(description = "Entity ID")
    private Long id;

    @Schema(description = "Creation timestamp")
    private Instant creationTimestamp;

    @Schema(description = "Update timestamp")
    private Instant updateTimestamp;

    @Schema(description = "User ID who created this inspection")
    private Long createdById;

    @Schema(description = "User ID who last updated this inspection")
    private Long updatedById;

    // =========================================================================
    // STOCK ORDER REFERENCES
    // =========================================================================

    @Schema(description = "Source stock order ID (order at field inspection facility)")
    private Long sourceStockOrderId;

    @Schema(description = "Source stock order identifier")
    private String sourceStockOrderIdentifier;

    @Schema(description = "Destination stock order ID (order at packing plant, null if available)")
    private Long destinationStockOrderId;

    @Schema(description = "Company ID that owns this inspection")
    private Long companyId;

    // =========================================================================
    // INSPECTION DATA
    // =========================================================================

    @Schema(description = "Date when the inspection was performed")
    private Instant inspectionDate;

    @Schema(description = "Time of inspection in HH:mm format")
    private String inspectionTime;

    // =========================================================================
    // PRODUCER INFORMATION
    // =========================================================================

    @Schema(description = "Producer/supplier ID")
    private Long producerUserCustomerId;

    @Schema(description = "Producer name for display")
    private String producerName;

    // =========================================================================
    // SENSORIAL INSPECTION RESULTS
    // =========================================================================

    @Schema(description = "Flavor test result: NORMAL or DEFECT")
    private String flavorTestResult;

    @Schema(description = "Flavor defect type ID (if result is DEFECT)")
    private Long flavorDefectTypeId;

    @Schema(description = "Flavor defect type code")
    private String flavorDefectTypeCode;

    @Schema(description = "Flavor defect type label")
    private String flavorDefectTypeLabel;

    @Schema(description = "Whether purchase is recommended")
    private Boolean purchaseRecommended;

    @Schema(description = "Additional inspection notes")
    private String inspectionNotes;

    // =========================================================================
    // RECEPTION DATA
    // =========================================================================

    @Schema(description = "Number of gavetas (crates)")
    private Integer numberOfGavetas;

    @Schema(description = "Number of bines (bins)")
    private Integer numberOfBines;

    @Schema(description = "Number of piscinas (ponds)")
    private Integer numberOfPiscinas;

    @Schema(description = "Guía de remisión number")
    private String guiaRemisionNumber;

    @Schema(description = "Total quantity in units")
    private BigDecimal totalQuantity;

    // =========================================================================
    // COMPUTED FIELDS
    // =========================================================================

    @Schema(description = "Whether this inspection is available for linking")
    private Boolean available;

    // =========================================================================
    // GETTERS AND SETTERS
    // =========================================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(Instant creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public Instant getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(Instant updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public Long getUpdatedById() {
        return updatedById;
    }

    public void setUpdatedById(Long updatedById) {
        this.updatedById = updatedById;
    }

    public Long getSourceStockOrderId() {
        return sourceStockOrderId;
    }

    public void setSourceStockOrderId(Long sourceStockOrderId) {
        this.sourceStockOrderId = sourceStockOrderId;
    }

    public String getSourceStockOrderIdentifier() {
        return sourceStockOrderIdentifier;
    }

    public void setSourceStockOrderIdentifier(String sourceStockOrderIdentifier) {
        this.sourceStockOrderIdentifier = sourceStockOrderIdentifier;
    }

    public Long getDestinationStockOrderId() {
        return destinationStockOrderId;
    }

    public void setDestinationStockOrderId(Long destinationStockOrderId) {
        this.destinationStockOrderId = destinationStockOrderId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
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

    public Long getProducerUserCustomerId() {
        return producerUserCustomerId;
    }

    public void setProducerUserCustomerId(Long producerUserCustomerId) {
        this.producerUserCustomerId = producerUserCustomerId;
    }

    public String getProducerName() {
        return producerName;
    }

    public void setProducerName(String producerName) {
        this.producerName = producerName;
    }

    public String getFlavorTestResult() {
        return flavorTestResult;
    }

    public void setFlavorTestResult(String flavorTestResult) {
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

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }
}
