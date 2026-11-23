package com.abelium.inatrace.components.integration.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "Primary wet cocoa delivery from a farmer without financial pricing")
public class ApiDeliveryIntegration {

    @JsonProperty("id")
    @Schema(description = "Unique delivery ID in INATrace", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @JsonProperty("delivery_receipt")
    @Schema(description = "Sequential delivery receipt number (e.g. 0001)")
    private String deliveryReceipt;

    @JsonProperty("company")
    @Schema(description = "Receiving organization", requiredMode = Schema.RequiredMode.REQUIRED)
    private IdNameDto company;

    @JsonProperty("facility")
    @Schema(description = "Receiving collection center")
    private IdNameDto facility;

    @JsonProperty("farmer_id")
    private Long farmerId;

    @JsonProperty("farmer_internal_id")
    private String farmerInternalId;

    @JsonProperty("delivery_date")
    @Schema(description = "Delivery calendar date (YYYY-MM-DD)", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate deliveryDate;

    @JsonProperty("semi_product")
    private IdNameDto semiProduct;

    @JsonProperty("variety")
    private CodeLabelDto variety;

    @JsonProperty("parcel_lot")
    @Schema(description = "Plot name or code declared at reception")
    private String parcelLot;

    @JsonProperty("plot_id")
    @Schema(description = "Resolved plot ID matching parcel_lot")
    private Long plotId;

    @JsonProperty("organic")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean organic;

    @JsonProperty("unit")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private UnitDto unit;

    @JsonProperty("gross_quantity")
    private BigDecimal grossQuantity;

    @JsonProperty("tare")
    private BigDecimal tare;

    @JsonProperty("damaged_weight_deduction")
    private BigDecimal damagedWeightDeduction;

    @JsonProperty("moisture_percentage")
    @Schema(description = "Retained dry-equivalent yield percentage recorded at scale qualification (consume net_quantity and moisture_weight_deduction directly)")
    private BigDecimal moisturePercentage;

    @JsonProperty("moisture_weight_deduction")
    @Schema(description = "Weight deducted for moisture/conversion at reception")
    private BigDecimal moistureWeightDeduction;

    @JsonProperty("net_quantity")
    @Schema(description = "Final net credited weight in the unit specified by unit", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal netQuantity;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    @Schema(name = "IdNameDto", description = "Entity ID and display name")
    public static class IdNameDto {
        @JsonProperty("id")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private Long id;

        @JsonProperty("name")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private String name;

        public IdNameDto() {}
        public IdNameDto(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    @Schema(name = "CodeLabelDto", description = "Catalog code and human-readable label")
    public static class CodeLabelDto {
        @JsonProperty("code")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private String code;

        @JsonProperty("label")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private String label;

        public CodeLabelDto() {}
        public CodeLabelDto(String code, String label) {
            this.code = code;
            this.label = label;
        }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }

    @Schema(name = "UnitSummaryDto", description = "Normalized measurement unit and exact kilogram conversion factor")
    public static class UnitDto {
        @JsonProperty("code")
        @Schema(allowableValues = {"LB", "KG", "QQ"}, requiredMode = Schema.RequiredMode.REQUIRED)
        private String code;

        @JsonProperty("kg_factor")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private BigDecimal kgFactor;

        public UnitDto() {}
        public UnitDto(String code, BigDecimal kgFactor) {
            this.code = code;
            this.kgFactor = kgFactor;
        }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public BigDecimal getKgFactor() { return kgFactor; }
        public void setKgFactor(BigDecimal kgFactor) { this.kgFactor = kgFactor; }
    }

    public ApiDeliveryIntegration() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDeliveryReceipt() { return deliveryReceipt; }
    public void setDeliveryReceipt(String deliveryReceipt) { this.deliveryReceipt = deliveryReceipt; }
    public IdNameDto getCompany() { return company; }
    public void setCompany(IdNameDto company) { this.company = company; }
    public IdNameDto getFacility() { return facility; }
    public void setFacility(IdNameDto facility) { this.facility = facility; }
    public Long getFarmerId() { return farmerId; }
    public void setFarmerId(Long farmerId) { this.farmerId = farmerId; }
    public String getFarmerInternalId() { return farmerInternalId; }
    public void setFarmerInternalId(String farmerInternalId) { this.farmerInternalId = farmerInternalId; }
    public LocalDate getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDate deliveryDate) { this.deliveryDate = deliveryDate; }
    public IdNameDto getSemiProduct() { return semiProduct; }
    public void setSemiProduct(IdNameDto semiProduct) { this.semiProduct = semiProduct; }
    public CodeLabelDto getVariety() { return variety; }
    public void setVariety(CodeLabelDto variety) { this.variety = variety; }
    public String getParcelLot() { return parcelLot; }
    public void setParcelLot(String parcelLot) { this.parcelLot = parcelLot; }
    public Long getPlotId() { return plotId; }
    public void setPlotId(Long plotId) { this.plotId = plotId; }
    public Boolean getOrganic() { return organic; }
    public void setOrganic(Boolean organic) { this.organic = organic; }
    public UnitDto getUnit() { return unit; }
    public void setUnit(UnitDto unit) { this.unit = unit; }
    public BigDecimal getGrossQuantity() { return grossQuantity; }
    public void setGrossQuantity(BigDecimal grossQuantity) { this.grossQuantity = grossQuantity; }
    public BigDecimal getTare() { return tare; }
    public void setTare(BigDecimal tare) { this.tare = tare; }
    public BigDecimal getDamagedWeightDeduction() { return damagedWeightDeduction; }
    public void setDamagedWeightDeduction(BigDecimal damagedWeightDeduction) { this.damagedWeightDeduction = damagedWeightDeduction; }
    public BigDecimal getMoisturePercentage() { return moisturePercentage; }
    public void setMoisturePercentage(BigDecimal moisturePercentage) { this.moisturePercentage = moisturePercentage; }
    public BigDecimal getMoistureWeightDeduction() { return moistureWeightDeduction; }
    public void setMoistureWeightDeduction(BigDecimal moistureWeightDeduction) { this.moistureWeightDeduction = moistureWeightDeduction; }
    public BigDecimal getNetQuantity() { return netQuantity; }
    public void setNetQuantity(BigDecimal netQuantity) { this.netQuantity = netQuantity; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
