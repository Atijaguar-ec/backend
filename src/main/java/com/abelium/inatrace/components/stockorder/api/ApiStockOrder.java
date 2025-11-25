package com.abelium.inatrace.components.stockorder.api;

import com.abelium.inatrace.api.ApiBaseEntity;
import com.abelium.inatrace.components.codebook.measure_unit_type.api.ApiMeasureUnitType;
import com.abelium.inatrace.components.codebook.semiproduct.api.ApiSemiProduct;
import com.abelium.inatrace.components.common.api.ApiActivityProof;
import com.abelium.inatrace.components.common.api.ApiDocument;
import com.abelium.inatrace.components.company.api.ApiCompany;
import com.abelium.inatrace.components.company.api.ApiCompanyCustomer;
import com.abelium.inatrace.components.company.api.ApiUserCustomer;
import com.abelium.inatrace.components.facility.api.ApiFacility;
import com.abelium.inatrace.components.payment.api.ApiPayment;
import com.abelium.inatrace.components.processingorder.api.ApiProcessingOrder;
import com.abelium.inatrace.components.product.api.ApiFinalProduct;
import com.abelium.inatrace.components.productorder.api.ApiProductOrder;
import com.abelium.inatrace.components.user.api.ApiUser;
import com.abelium.inatrace.db.entities.stockorder.enums.OrderType;
import com.abelium.inatrace.db.entities.stockorder.enums.PreferredWayOfPayment;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Validated
public class ApiStockOrder extends ApiBaseEntity {

    @Schema(description = "Stock order identifier")
    private String identifier;

    @Schema(description = "Timestamp indicates when stock order have been created")
    private Instant creationTimestamp;

    @Schema(description = "Timestamp indicates when stock order have been updated")
    private Instant updateTimestamp;

    @Schema(description = "User that has created StockOrder")
    private ApiUser createdBy;

    @Schema(description = "User that has last updated StockOrder")
    private ApiUser updatedBy;

    @Schema(description = "ID of the user who has created the stock order")
    private Long creatorId;

    // Relevant only for order type: PURCHASE_ORDER
    @Schema(description = "Representative of producer user customer. E.g. collector.")
    private ApiUserCustomer representativeOfProducerUserCustomer;

    // Relevant only for order type: PURCHASE_ORDER
    @Schema(description = "Id of the person who has produced the entry.")
    private ApiUserCustomer producerUserCustomer;

    // Relevant only for order type: PURCHASE_ORDER
    @Schema(description = "Production location")
    private ApiStockOrderLocation productionLocation;

    @Schema(description = "Activity proofs")
    private List<ApiActivityProof> activityProofs;

    @Schema(description = "Processing evidence fields stored values for this stock order")
    private List<ApiStockOrderEvidenceFieldValue> requiredEvidenceFieldValues;

    @Schema(description = "Processing evidence types stored values for this stock order")
    private List<ApiStockOrderEvidenceTypeValue> requiredEvidenceTypeValues;

    @Schema(description = "Other processing evidence documents - evidence types that can be provided but are not mandatory")
    private List<ApiStockOrderEvidenceTypeValue> otherEvidenceDocuments;

    @Schema(description = "Semi product")
    private ApiSemiProduct semiProduct;

    @Schema(description = "Option to determine price later after taking delivery")
    private Boolean priceDeterminedLater;

    @Schema(description = "Final product")
    private ApiFinalProduct finalProduct;

    @Schema(description = "Facility")
    private ApiFacility facility;

    @Schema(description = "Company")
    private ApiCompany company;

    @Schema(description = "Measurement unit")
    private ApiMeasureUnitType measureUnitType;

    @Schema(description = "Total quantity")
    private BigDecimal totalQuantity;

    @Schema(description = "Total gross quantity")
    private BigDecimal totalGrossQuantity;

    @Schema(description = "Fulfilled quantity")
    private BigDecimal fulfilledQuantity;

    @Schema(description = "Available quantity")
    private BigDecimal availableQuantity;

    @Schema(description = "Is stock available")
    public Boolean isAvailable;

    @Schema(description = "Total quantity of this stock order is not within the expected range")
    public Boolean outQuantityNotInRange;

    @Schema(description = "Production date")
    private LocalDate productionDate;

    @Schema(description = "Delivery time")
    private LocalDate deliveryTime;

    @Schema(description = "The produrct order that triggered creation of this stock order")
    private ApiProductOrder productOrder;

    @Schema(description = "User entered Order ID when placing Quote order")
    private String orderId;

    @Schema(description = "The processing order that created this stock order")
    private ApiProcessingOrder processingOrder;

    @Schema(description = "Price per unit")
    private BigDecimal pricePerUnit;

    @Schema(description = "Currency")
    private String currency;

    @Schema(description = "Is order of type PURCHASE_ORDER")
    private Boolean isPurchaseOrder;

    @Schema(description = "Order type")
    private OrderType orderType;

    @Schema(description = "The prefix for the LOT name - retrieved from the Processing action")
    private String lotPrefix;

    @Schema(description = "Internal LOT number")
    private String internalLotNumber;

    @Schema(description = "Comments")
    private String comments;

    @Schema(description = "Is women share")
    private Boolean womenShare;

    @Schema(description = "Cost")
    private BigDecimal cost;

    @Schema(description = "Paid")
    private BigDecimal paid;

    @Schema(description = "Balance")
    private BigDecimal balance;

    @Schema(description = "Preferred way of payment")
    private PreferredWayOfPayment preferredWayOfPayment;

    @Schema(description = "SAC number")
    private Integer sacNumber;

    @Schema(description = "Is order open")
    private Boolean isOpenOrder;

    @Schema(description = "Quote facility")
    private ApiFacility quoteFacility;

    @Schema(description = "Quote company")
    private ApiCompany quoteCompany;

    @Schema(description = "The company customer for whom the stock order is created")
    private ApiCompanyCustomer consumerCompanyCustomer;

    @Schema(description = "Price per unit for end customer")
    private BigDecimal pricePerUnitForEndCustomer;

    @Schema(description = "Currency for price per unit for end customer")
    private String currencyForEndCustomer;

    @Schema(description = "Organic")
    private Boolean organic;

    @Schema(description = "Week number for cacao deliveries (1-53)")
    private Integer weekNumber;

    @Schema(description = "Parcel lot for cacao deliveries")
    private String parcelLot;

    @Schema(description = "Variety for cacao deliveries")
    private String variety;

    @Schema(description = "Organic certification details")
    private String organicCertification;

    @Schema(description = "Tare")
    private BigDecimal tare;

    @Schema(description = "Damaged price deduction")
    private BigDecimal damagedPriceDeduction;

    @Schema(description = "Final price discount per unit")
    private BigDecimal finalPriceDiscount;

    @Schema(description = "Damaged weight deduction")
    private BigDecimal damagedWeightDeduction;

    @Schema(description = "Moisture percentage applied (0-100)")
    private BigDecimal moisturePercentage;

    @Schema(description = "Calculated weight deduction due to moisture")
    private BigDecimal moistureWeightDeduction;

    @Schema(description = "Net quantity after all deductions (tare, damaged weight, moisture)")
    private BigDecimal netQuantity;

    // 🦐 Shrimp-specific fields (only for non-laboratory deliveries)
    @Schema(description = "Number of gavetas (shrimp-specific)")
    private Integer numberOfGavetas;

    @Schema(description = "N° de Bines (específico para camarón)")
    private String numberOfBines;

    @Schema(description = "Number of pools/piscinas (shrimp-specific)")
    private String numberOfPiscinas;

    @Schema(description = "Remission guide number (shrimp-specific)")
    private String guiaRemisionNumber;

    // 🦐 Shrimp processing-specific fields: cutting
    @Schema(description = "Cutting type (shrimp processing)")
    private String cuttingType;

    @Schema(description = "Cutting entry date (shrimp processing)")
    private LocalDate cuttingEntryDate;

    @Schema(description = "Cutting exit date (shrimp processing)")
    private LocalDate cuttingExitDate;

    @Schema(description = "Temperature control during cutting (shrimp processing)")
    private String cuttingTemperatureControl;

    // 🦐 Shrimp processing-specific fields: treatment
    @Schema(description = "Treatment type (shrimp processing)")
    private String treatmentType;

    @Schema(description = "Treatment entry date (shrimp processing)")
    private LocalDate treatmentEntryDate;

    @Schema(description = "Treatment exit date (shrimp processing)")
    private LocalDate treatmentExitDate;

    @Schema(description = "Temperature control during treatment (shrimp processing)")
    private String treatmentTemperatureControl;

    @Schema(description = "Chemical used in treatment (shrimp processing)")
    private String treatmentChemicalUsed;

    // 🦐 Shrimp processing-specific fields: tunnel freezing
    @Schema(description = "Tunnel production date (shrimp processing)")
    private LocalDate tunnelProductionDate;

    @Schema(description = "Tunnel expiration date (shrimp processing)")
    private LocalDate tunnelExpirationDate;

    @Schema(description = "Tunnel net weight (shrimp processing)")
    private BigDecimal tunnelNetWeight;

    @Schema(description = "Tunnel supplier (shrimp processing)")
    private String tunnelSupplier;

    @Schema(description = "Tunnel freezing type (shrimp processing)")
    private String tunnelFreezingType;

    @Schema(description = "Tunnel entry date (shrimp processing)")
    private LocalDate tunnelEntryDate;

    @Schema(description = "Tunnel exit date (shrimp processing)")
    private LocalDate tunnelExitDate;

    // 🦐 Shrimp processing-specific fields: washing area
    @Schema(description = "Washing water temperature (shrimp processing)")
    private String washingWaterTemperature;

    @Schema(description = "Shrimp temperature control in washing area (shrimp processing)")
    private String washingShrimpTemperatureControl;

    // 🔬 Laboratory-specific fields
    @Schema(description = "Sample number (laboratory-specific)")
    private String sampleNumber;

    @Schema(description = "Reception time (laboratory-specific)")
    private LocalTime receptionTime;

    @Schema(description = "Quality document (PDF) for laboratory analysis")
    private ApiDocument qualityDocument;

    // 🔍 Field inspection (sensory testing) specific fields - for isFieldInspection facilities
    @Schema(description = "Flavor test result: NORMAL or DEFECT (field inspection)")
    private String flavorTestResult;

    @Schema(description = "Flavor defect type ID (field inspection)")
    private Long flavorDefectTypeId;

    @Schema(description = "Flavor defect type code (field inspection)")
    private String flavorDefectTypeCode;

    @Schema(description = "Flavor defect type label (field inspection)")
    private String flavorDefectTypeLabel;

    @Schema(description = "Purchase recommended by inspector (field inspection)")
    private Boolean purchaseRecommended;

    @Schema(description = "Inspection notes (field inspection)")
    private String inspectionNotes;

    // 🧪 Laboratory analysis fields (transient - stored in LaboratoryAnalysis table)
    // These fields are received from frontend but not persisted in StockOrder
    @Schema(hidden = true, description = "Sensorial analysis - Raw odor (stored in LaboratoryAnalysis)")
    private String sensorialRawOdor;

    @Schema(hidden = true, description = "Sensorial analysis - Raw taste (stored in LaboratoryAnalysis)")
    private String sensorialRawTaste;

    @Schema(hidden = true, description = "Sensorial analysis - Raw color (stored in LaboratoryAnalysis)")
    private String sensorialRawColor;

    @Schema(hidden = true, description = "Sensorial analysis - Cooked odor (stored in LaboratoryAnalysis)")
    private String sensorialCookedOdor;

    @Schema(hidden = true, description = "Sensorial analysis - Cooked taste (stored in LaboratoryAnalysis)")
    private String sensorialCookedTaste;

    @Schema(hidden = true, description = "Sensorial analysis - Cooked color (stored in LaboratoryAnalysis)")
    private String sensorialCookedColor;

    @Schema(hidden = true, description = "Quality notes (stored in LaboratoryAnalysis)")
    private String qualityNotes;

    @Schema(hidden = true, description = "Whether metabisulfite level is acceptable (stored in LaboratoryAnalysis)")
    private Boolean metabisulfiteLevelAcceptable;

    @Schema(hidden = true, description = "Whether approved for purchase (stored in LaboratoryAnalysis)")
    private Boolean approvedForPurchase;

    // 🦐 Classification-specific fields (stored in ProcessingClassificationBatch)
    @Schema(hidden = true, description = "Classification start time (stored in ProcessingClassificationBatch)")
    private String classificationStartTime;

    @Schema(hidden = true, description = "Classification end time (stored in ProcessingClassificationBatch)")
    private String classificationEndTime;

    @Schema(hidden = true, description = "Production order number (stored in ProcessingClassificationBatch)")
    private String productionOrder;

    @Schema(hidden = true, description = "Freezing type (stored in ProcessingClassificationBatch)")
    private String freezingType;

    @Schema(hidden = true, description = "Machine used for classification (stored in ProcessingClassificationBatch)")
    private String machine;

    @Schema(hidden = true, description = "Brand header (stored in ProcessingClassificationBatch)")
    private String brandHeader;

    @Schema(hidden = true, description = "Classification details by size (stored in ProcessingClassificationBatchDetail)")
    private List<ApiClassificationDetail> classificationDetails;

    @Schema(description = "Generated UUID tag for this stock order QR code")
    private String qrCodeTag;

    @Schema(description = "The final product for which the QR code tag is generated")
    private ApiFinalProduct qrCodeTagFinalProduct;

    @Schema(description = "Payments for stock order")
    private List<ApiPayment> payments;

    @Schema(description = "The ID from which this repacked stock order was created; This ID is generated and provided by the client; Only applicable for repacked stock orders")
    private String repackedOriginStockOrderId;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
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

    public ApiUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(ApiUser createdBy) {
        this.createdBy = createdBy;
    }

    public ApiUser getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(ApiUser updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public ApiUserCustomer getRepresentativeOfProducerUserCustomer() {
        return representativeOfProducerUserCustomer;
    }

    public void setRepresentativeOfProducerUserCustomer(ApiUserCustomer representativeOfProducerUserCustomer) {
        this.representativeOfProducerUserCustomer = representativeOfProducerUserCustomer;
    }

    public ApiUserCustomer getProducerUserCustomer() {
        return producerUserCustomer;
    }

    public void setProducerUserCustomer(ApiUserCustomer producerUserCustomer) {
        this.producerUserCustomer = producerUserCustomer;
    }

    public ApiStockOrderLocation getProductionLocation() {
        return productionLocation;
    }

    public void setProductionLocation(ApiStockOrderLocation productionLocation) {
        this.productionLocation = productionLocation;
    }

    public List<ApiActivityProof> getActivityProofs() {
        if (activityProofs == null) {
            activityProofs = new ArrayList<>();
        }
        return activityProofs;
    }

    public void setActivityProofs(List<ApiActivityProof> activityProofs) {
        this.activityProofs = activityProofs;
    }

    public List<ApiStockOrderEvidenceFieldValue> getRequiredEvidenceFieldValues() {
        if (requiredEvidenceFieldValues == null) {
            requiredEvidenceFieldValues = new ArrayList<>();
        }
        return requiredEvidenceFieldValues;
    }

    public void setRequiredEvidenceFieldValues(List<ApiStockOrderEvidenceFieldValue> requiredEvidenceFieldValues) {
        this.requiredEvidenceFieldValues = requiredEvidenceFieldValues;
    }

    public List<ApiStockOrderEvidenceTypeValue> getRequiredEvidenceTypeValues() {
        if (requiredEvidenceTypeValues == null) {
            requiredEvidenceTypeValues = new ArrayList<>();
        }
        return requiredEvidenceTypeValues;
    }

    public void setRequiredEvidenceTypeValues(List<ApiStockOrderEvidenceTypeValue> requiredEvidenceTypeValues) {
        this.requiredEvidenceTypeValues = requiredEvidenceTypeValues;
    }

    public List<ApiStockOrderEvidenceTypeValue> getOtherEvidenceDocuments() {
        if (otherEvidenceDocuments == null) {
            otherEvidenceDocuments = new ArrayList<>();
        }
        return otherEvidenceDocuments;
    }

    public void setOtherEvidenceDocuments(List<ApiStockOrderEvidenceTypeValue> otherEvidenceDocuments) {
        this.otherEvidenceDocuments = otherEvidenceDocuments;
    }

    public ApiSemiProduct getSemiProduct() {
        return semiProduct;
    }

    public void setSemiProduct(ApiSemiProduct semiProduct) {
        this.semiProduct = semiProduct;
    }

    public Boolean getPriceDeterminedLater() {
        return priceDeterminedLater;
    }

    public void setPriceDeterminedLater(Boolean priceDeterminedLater) {
        this.priceDeterminedLater = priceDeterminedLater;
    }

    public ApiFinalProduct getFinalProduct() {
        return finalProduct;
    }

    public void setFinalProduct(ApiFinalProduct finalProduct) {
        this.finalProduct = finalProduct;
    }

    public ApiFacility getFacility() {
        return facility;
    }

    public void setFacility(ApiFacility facility) {
        this.facility = facility;
    }

    public ApiCompany getCompany() {
        return company;
    }

    public void setCompany(ApiCompany company) {
        this.company = company;
    }

    public ApiMeasureUnitType getMeasureUnitType() {
        return measureUnitType;
    }

    public void setMeasureUnitType(ApiMeasureUnitType measureUnitType) {
        this.measureUnitType = measureUnitType;
    }

    public BigDecimal getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(BigDecimal totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public BigDecimal getTotalGrossQuantity() {
        return totalGrossQuantity;
    }

    public void setTotalGrossQuantity(BigDecimal totalGrossQuantity) {
        this.totalGrossQuantity = totalGrossQuantity;
    }

    public BigDecimal getFulfilledQuantity() {
        return fulfilledQuantity;
    }

    public void setFulfilledQuantity(BigDecimal fulfilledQuantity) {
        this.fulfilledQuantity = fulfilledQuantity;
    }

    public BigDecimal getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(BigDecimal availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public Boolean getAvailable() {
        return isAvailable;
    }

    public void setAvailable(Boolean available) {
        isAvailable = available;
    }

    public Boolean getOutQuantityNotInRange() {
        return outQuantityNotInRange;
    }

    public void setOutQuantityNotInRange(Boolean outQuantityNotInRange) {
        this.outQuantityNotInRange = outQuantityNotInRange;
    }

    public LocalDate getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(LocalDate productionDate) {
        this.productionDate = productionDate;
    }

    public LocalDate getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(LocalDate deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public ApiProductOrder getProductOrder() {
        return productOrder;
    }

    public void setProductOrder(ApiProductOrder productOrder) {
        this.productOrder = productOrder;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public ApiProcessingOrder getProcessingOrder() {
        return processingOrder;
    }

    public void setProcessingOrder(ApiProcessingOrder processingOrder) {
        this.processingOrder = processingOrder;
    }

    public BigDecimal getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(BigDecimal pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Boolean getPurchaseOrder() {
        return isPurchaseOrder;
    }

    public void setPurchaseOrder(Boolean purchaseOrder) {
        isPurchaseOrder = purchaseOrder;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public String getLotPrefix() {
        return lotPrefix;
    }

    public void setLotPrefix(String lotPrefix) {
        this.lotPrefix = lotPrefix;
    }

    public String getInternalLotNumber() {
        return internalLotNumber;
    }

    public void setInternalLotNumber(String internalLotNumber) {
        this.internalLotNumber = internalLotNumber;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Boolean getWomenShare() {
        return womenShare;
    }

    public void setWomenShare(Boolean womenShare) {
        this.womenShare = womenShare;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public BigDecimal getPaid() {
        return paid;
    }

    public void setPaid(BigDecimal paid) {
        this.paid = paid;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public PreferredWayOfPayment getPreferredWayOfPayment() {
        return preferredWayOfPayment;
    }

    public void setPreferredWayOfPayment(PreferredWayOfPayment preferredWayOfPayment) {
        this.preferredWayOfPayment = preferredWayOfPayment;
    }

    public Integer getSacNumber() {
        return sacNumber;
    }

    public void setSacNumber(Integer sacNumber) {
        this.sacNumber = sacNumber;
    }

    public Boolean getOpenOrder() {
        return isOpenOrder;
    }

    public void setOpenOrder(Boolean openOrder) {
        isOpenOrder = openOrder;
    }

    public ApiFacility getQuoteFacility() {
        return quoteFacility;
    }

    public void setQuoteFacility(ApiFacility quoteFacility) {
        this.quoteFacility = quoteFacility;
    }

    public ApiCompany getQuoteCompany() {
        return quoteCompany;
    }

    public void setQuoteCompany(ApiCompany quoteCompany) {
        this.quoteCompany = quoteCompany;
    }

    public ApiCompanyCustomer getConsumerCompanyCustomer() {
        return consumerCompanyCustomer;
    }

    public void setConsumerCompanyCustomer(ApiCompanyCustomer consumerCompanyCustomer) {
        this.consumerCompanyCustomer = consumerCompanyCustomer;
    }

    public BigDecimal getPricePerUnitForEndCustomer() {
        return pricePerUnitForEndCustomer;
    }

    public void setPricePerUnitForEndCustomer(BigDecimal pricePerUnitForEndCustomer) {
        this.pricePerUnitForEndCustomer = pricePerUnitForEndCustomer;
    }

    public String getCurrencyForEndCustomer() {
        return currencyForEndCustomer;
    }

    public void setCurrencyForEndCustomer(String currencyForEndCustomer) {
        this.currencyForEndCustomer = currencyForEndCustomer;
    }

    public Boolean getOrganic() {
        return organic;
    }

    public void setOrganic(Boolean organic) {
        this.organic = organic;
    }

    public Integer getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(Integer weekNumber) {
        this.weekNumber = weekNumber;
    }

    public String getParcelLot() {
        return parcelLot;
    }

    public void setParcelLot(String parcelLot) {
        this.parcelLot = parcelLot;
    }

    public String getVariety() {
        return variety;
    }

    public void setVariety(String variety) {
        this.variety = variety;
    }

    public String getOrganicCertification() {
        return organicCertification;
    }

    public void setOrganicCertification(String organicCertification) {
        this.organicCertification = organicCertification;
    }

    public BigDecimal getTare() {
        return tare;
    }

    public void setTare(BigDecimal tare) {
        this.tare = tare;
    }

    public BigDecimal getDamagedPriceDeduction() {
        return damagedPriceDeduction;
    }

    public void setDamagedPriceDeduction(BigDecimal damagedPriceDeduction) {
        this.damagedPriceDeduction = damagedPriceDeduction;
    }

    public BigDecimal getFinalPriceDiscount() {
        return finalPriceDiscount;
    }

    public void setFinalPriceDiscount(BigDecimal finalPriceDiscount) {
        this.finalPriceDiscount = finalPriceDiscount;
    }

    public BigDecimal getDamagedWeightDeduction() {
        return damagedWeightDeduction;
    }

    public void setDamagedWeightDeduction(BigDecimal damagedWeightDeduction) {
        this.damagedWeightDeduction = damagedWeightDeduction;
    }

    public BigDecimal getMoisturePercentage() {
        return moisturePercentage;
    }

    public void setMoisturePercentage(BigDecimal moisturePercentage) {
        this.moisturePercentage = moisturePercentage;
    }

    public BigDecimal getMoistureWeightDeduction() {
        return moistureWeightDeduction;
    }

    public void setMoistureWeightDeduction(BigDecimal moistureWeightDeduction) {
        this.moistureWeightDeduction = moistureWeightDeduction;
    }

    public BigDecimal getNetQuantity() {
        return netQuantity;
    }

    public void setNetQuantity(BigDecimal netQuantity) {
        this.netQuantity = netQuantity;
    }

    public Integer getNumberOfGavetas() {
        return numberOfGavetas;
    }

    public void setNumberOfGavetas(Integer numberOfGavetas) {
        this.numberOfGavetas = numberOfGavetas;
    }

    public String getNumberOfBines() {
        return numberOfBines;
    }

    public void setNumberOfBines(String numberOfBines) {
        this.numberOfBines = numberOfBines;
    }

    public String getNumberOfPiscinas() {
        return numberOfPiscinas;
    }

    public void setNumberOfPiscinas(String numberOfPiscinas) {
        this.numberOfPiscinas = numberOfPiscinas;
    }

    public String getGuiaRemisionNumber() {
        return guiaRemisionNumber;
    }

    public void setGuiaRemisionNumber(String guiaRemisionNumber) {
        this.guiaRemisionNumber = guiaRemisionNumber;
    }

    public String getCuttingType() {
        return cuttingType;
    }

    public void setCuttingType(String cuttingType) {
        this.cuttingType = cuttingType;
    }

    public LocalDate getCuttingEntryDate() {
        return cuttingEntryDate;
    }

    public void setCuttingEntryDate(LocalDate cuttingEntryDate) {
        this.cuttingEntryDate = cuttingEntryDate;
    }

    public LocalDate getCuttingExitDate() {
        return cuttingExitDate;
    }

    public void setCuttingExitDate(LocalDate cuttingExitDate) {
        this.cuttingExitDate = cuttingExitDate;
    }

    public String getCuttingTemperatureControl() {
        return cuttingTemperatureControl;
    }

    public void setCuttingTemperatureControl(String cuttingTemperatureControl) {
        this.cuttingTemperatureControl = cuttingTemperatureControl;
    }

    public String getTreatmentType() {
        return treatmentType;
    }

    public void setTreatmentType(String treatmentType) {
        this.treatmentType = treatmentType;
    }

    public LocalDate getTreatmentEntryDate() {
        return treatmentEntryDate;
    }

    public void setTreatmentEntryDate(LocalDate treatmentEntryDate) {
        this.treatmentEntryDate = treatmentEntryDate;
    }

    public LocalDate getTreatmentExitDate() {
        return treatmentExitDate;
    }

    public void setTreatmentExitDate(LocalDate treatmentExitDate) {
        this.treatmentExitDate = treatmentExitDate;
    }

    public String getTreatmentTemperatureControl() {
        return treatmentTemperatureControl;
    }

    public void setTreatmentTemperatureControl(String treatmentTemperatureControl) {
        this.treatmentTemperatureControl = treatmentTemperatureControl;
    }

    public String getTreatmentChemicalUsed() {
        return treatmentChemicalUsed;
    }

    public void setTreatmentChemicalUsed(String treatmentChemicalUsed) {
        this.treatmentChemicalUsed = treatmentChemicalUsed;
    }

    public LocalDate getTunnelProductionDate() {
        return tunnelProductionDate;
    }

    public void setTunnelProductionDate(LocalDate tunnelProductionDate) {
        this.tunnelProductionDate = tunnelProductionDate;
    }

    public LocalDate getTunnelExpirationDate() {
        return tunnelExpirationDate;
    }

    public void setTunnelExpirationDate(LocalDate tunnelExpirationDate) {
        this.tunnelExpirationDate = tunnelExpirationDate;
    }

    public BigDecimal getTunnelNetWeight() {
        return tunnelNetWeight;
    }

    public void setTunnelNetWeight(BigDecimal tunnelNetWeight) {
        this.tunnelNetWeight = tunnelNetWeight;
    }

    public String getTunnelSupplier() {
        return tunnelSupplier;
    }

    public void setTunnelSupplier(String tunnelSupplier) {
        this.tunnelSupplier = tunnelSupplier;
    }

    public String getTunnelFreezingType() {
        return tunnelFreezingType;
    }

    public void setTunnelFreezingType(String tunnelFreezingType) {
        this.tunnelFreezingType = tunnelFreezingType;
    }

    public LocalDate getTunnelEntryDate() {
        return tunnelEntryDate;
    }

    public void setTunnelEntryDate(LocalDate tunnelEntryDate) {
        this.tunnelEntryDate = tunnelEntryDate;
    }

    public LocalDate getTunnelExitDate() {
        return tunnelExitDate;
    }

    public void setTunnelExitDate(LocalDate tunnelExitDate) {
        this.tunnelExitDate = tunnelExitDate;
    }

    public String getWashingWaterTemperature() {
        return washingWaterTemperature;
    }

    public void setWashingWaterTemperature(String washingWaterTemperature) {
        this.washingWaterTemperature = washingWaterTemperature;
    }

    public String getWashingShrimpTemperatureControl() {
        return washingShrimpTemperatureControl;
    }

    public void setWashingShrimpTemperatureControl(String washingShrimpTemperatureControl) {
        this.washingShrimpTemperatureControl = washingShrimpTemperatureControl;
    }

    public String getSampleNumber() {
        return sampleNumber;
    }

    public void setSampleNumber(String sampleNumber) {
        this.sampleNumber = sampleNumber;
    }

    public LocalTime getReceptionTime() {
        return receptionTime;
    }

    public void setReceptionTime(LocalTime receptionTime) {
        this.receptionTime = receptionTime;
    }

    public ApiDocument getQualityDocument() {
        return qualityDocument;
    }

    public void setQualityDocument(ApiDocument qualityDocument) {
        this.qualityDocument = qualityDocument;
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

    public String getSensorialRawOdor() {
        return sensorialRawOdor;
    }

    public void setSensorialRawOdor(String sensorialRawOdor) {
        this.sensorialRawOdor = sensorialRawOdor;
    }

    public String getSensorialRawTaste() {
        return sensorialRawTaste;
    }

    public void setSensorialRawTaste(String sensorialRawTaste) {
        this.sensorialRawTaste = sensorialRawTaste;
    }

    public String getSensorialRawColor() {
        return sensorialRawColor;
    }

    public void setSensorialRawColor(String sensorialRawColor) {
        this.sensorialRawColor = sensorialRawColor;
    }

    public String getSensorialCookedOdor() {
        return sensorialCookedOdor;
    }

    public void setSensorialCookedOdor(String sensorialCookedOdor) {
        this.sensorialCookedOdor = sensorialCookedOdor;
    }

    public String getSensorialCookedTaste() {
        return sensorialCookedTaste;
    }

    public void setSensorialCookedTaste(String sensorialCookedTaste) {
        this.sensorialCookedTaste = sensorialCookedTaste;
    }

    public String getSensorialCookedColor() {
        return sensorialCookedColor;
    }

    public void setSensorialCookedColor(String sensorialCookedColor) {
        this.sensorialCookedColor = sensorialCookedColor;
    }

    public String getQualityNotes() {
        return qualityNotes;
    }

    public void setQualityNotes(String qualityNotes) {
        this.qualityNotes = qualityNotes;
    }

    public Boolean getMetabisulfiteLevelAcceptable() {
        return metabisulfiteLevelAcceptable;
    }

    public void setMetabisulfiteLevelAcceptable(Boolean metabisulfiteLevelAcceptable) {
        this.metabisulfiteLevelAcceptable = metabisulfiteLevelAcceptable;
    }

    public Boolean getApprovedForPurchase() {
        return approvedForPurchase;
    }

    public void setApprovedForPurchase(Boolean approvedForPurchase) {
        this.approvedForPurchase = approvedForPurchase;
    }

    public String getClassificationStartTime() {
        return classificationStartTime;
    }

    public void setClassificationStartTime(String classificationStartTime) {
        this.classificationStartTime = classificationStartTime;
    }

    public String getClassificationEndTime() {
        return classificationEndTime;
    }

    public void setClassificationEndTime(String classificationEndTime) {
        this.classificationEndTime = classificationEndTime;
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

    public List<ApiClassificationDetail> getClassificationDetails() {
        if (classificationDetails == null) {
            classificationDetails = new ArrayList<>();
        }
        return classificationDetails;
    }

    public void setClassificationDetails(List<ApiClassificationDetail> classificationDetails) {
        this.classificationDetails = classificationDetails;
    }

    public String getQrCodeTag() {
        return qrCodeTag;
    }

    public void setQrCodeTag(String qrCodeTag) {
        this.qrCodeTag = qrCodeTag;
    }

    public ApiFinalProduct getQrCodeTagFinalProduct() {
        return qrCodeTagFinalProduct;
    }

    public void setQrCodeTagFinalProduct(ApiFinalProduct qrCodeTagFinalProduct) {
        this.qrCodeTagFinalProduct = qrCodeTagFinalProduct;
    }

    public List<ApiPayment> getPayments() {
        return payments;
    }

    public void setPayments(List<ApiPayment> payments) {
        this.payments = payments;
    }

    public String getRepackedOriginStockOrderId() {
        return repackedOriginStockOrderId;
    }

    public void setRepackedOriginStockOrderId(String repackedOriginStockOrderId) {
        this.repackedOriginStockOrderId = repackedOriginStockOrderId;
    }
}
