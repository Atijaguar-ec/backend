package com.abelium.inatrace.components.integration.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class ApiDeliveryIntegration {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("delivery_receipt")
    private String deliveryReceipt;

    @JsonProperty("company")
    private IdNameDto company;

    @JsonProperty("facility")
    private IdNameDto facility;

    @JsonProperty("farmer_id")
    private Long farmerId;

    @JsonProperty("farmer_internal_id")
    private String farmerInternalId;

    @JsonProperty("delivery_date")
    private LocalDate deliveryDate;

    @JsonProperty("semi_product")
    private IdNameDto semiProduct;

    @JsonProperty("variety")
    private CodeLabelDto variety;

    @JsonProperty("parcel_lot")
    private String parcelLot;

    @JsonProperty("plot_id")
    private Long plotId;

    @JsonProperty("organic")
    private Boolean organic;

    @JsonProperty("unit")
    private UnitDto unit;

    @JsonProperty("gross_quantity")
    private BigDecimal grossQuantity;

    @JsonProperty("tare")
    private BigDecimal tare;

    @JsonProperty("damaged_weight_deduction")
    private BigDecimal damagedWeightDeduction;

    @JsonProperty("moisture_percentage")
    private BigDecimal moisturePercentage;

    @JsonProperty("moisture_weight_deduction")
    private BigDecimal moistureWeightDeduction;

    @JsonProperty("net_quantity")
    private BigDecimal netQuantity;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    public static class IdNameDto {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("name")
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

    public static class CodeLabelDto {
        @JsonProperty("code")
        private String code;

        @JsonProperty("label")
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

    public static class UnitDto {
        @JsonProperty("code")
        private String code;

        @JsonProperty("kg_factor")
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
