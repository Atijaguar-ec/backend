package com.abelium.inatrace.components.integration.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class ApiShipmentIntegration {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("lot_codes")
    private LotCodesDto lotCodes;

    @JsonProperty("shipment_date")
    private LocalDate shipmentDate;

    @JsonProperty("from_company")
    private IdNameDto fromCompany;

    @JsonProperty("from_facility")
    private IdNameDto fromFacility;

    @JsonProperty("buyer")
    private IdNameDto buyer;

    @JsonProperty("semi_product")
    private IdNameDto semiProduct;

    @JsonProperty("quantity")
    private BigDecimal quantity;

    @JsonProperty("unit")
    private UnitDto unit;

    @JsonProperty("organic")
    private Boolean organic;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    public static class LotCodesDto {
        @JsonProperty("internal_lot_number")
        private String internalLotNumber;

        @JsonProperty("order_id")
        private String orderId;

        @JsonProperty("qr_code_tag")
        private String qrCodeTag;

        @JsonProperty("sac_numbers")
        private List<Integer> sacNumbers;

        public LotCodesDto() {}

        public String getInternalLotNumber() { return internalLotNumber; }
        public void setInternalLotNumber(String internalLotNumber) { this.internalLotNumber = internalLotNumber; }
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public String getQrCodeTag() { return qrCodeTag; }
        public void setQrCodeTag(String qrCodeTag) { this.qrCodeTag = qrCodeTag; }
        public List<Integer> getSacNumbers() { return sacNumbers; }
        public void setSacNumbers(List<Integer> sacNumbers) { this.sacNumbers = sacNumbers; }
    }

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

    public ApiShipmentIntegration() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LotCodesDto getLotCodes() { return lotCodes; }
    public void setLotCodes(LotCodesDto lotCodes) { this.lotCodes = lotCodes; }
    public LocalDate getShipmentDate() { return shipmentDate; }
    public void setShipmentDate(LocalDate shipmentDate) { this.shipmentDate = shipmentDate; }
    public IdNameDto getFromCompany() { return fromCompany; }
    public void setFromCompany(IdNameDto fromCompany) { this.fromCompany = fromCompany; }
    public IdNameDto getFromFacility() { return fromFacility; }
    public void setFromFacility(IdNameDto fromFacility) { this.fromFacility = fromFacility; }
    public IdNameDto getBuyer() { return buyer; }
    public void setBuyer(IdNameDto buyer) { this.buyer = buyer; }
    public IdNameDto getSemiProduct() { return semiProduct; }
    public void setSemiProduct(IdNameDto semiProduct) { this.semiProduct = semiProduct; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public UnitDto getUnit() { return unit; }
    public void setUnit(UnitDto unit) { this.unit = unit; }
    public Boolean getOrganic() { return organic; }
    public void setOrganic(Boolean organic) { this.organic = organic; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
