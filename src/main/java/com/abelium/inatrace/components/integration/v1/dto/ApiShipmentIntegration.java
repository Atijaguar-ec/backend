package com.abelium.inatrace.components.integration.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Processed post-harvest cocoa lot or export shipment")
public class ApiShipmentIntegration {

    @JsonProperty("id")
    @Schema(description = "Unique shipment / lot ID in INATrace", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @JsonProperty("lot_codes")
    @Schema(description = "Identifiers printed on shipping documents, bags, and QR labels", requiredMode = Schema.RequiredMode.REQUIRED)
    private LotCodesDto lotCodes;

    @JsonProperty("shipment_date")
    @Schema(description = "Production or dispatch date (YYYY-MM-DD)")
    private LocalDate shipmentDate;

    @JsonProperty("from_company")
    @Schema(description = "Originating organization", requiredMode = Schema.RequiredMode.REQUIRED)
    private IdNameDto fromCompany;

    @JsonProperty("from_facility")
    @Schema(description = "Originating facility")
    private IdNameDto fromFacility;

    @JsonProperty("buyer")
    @Schema(description = "Destination buyer (present on sales/export lots)")
    private IdNameDto buyer;

    @JsonProperty("semi_product")
    @Schema(description = "Lot semi-product or final product")
    private IdNameDto semiProduct;

    @JsonProperty("quantity")
    @Schema(description = "Total lot weight", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal quantity;

    @JsonProperty("unit")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private UnitDto unit;

    @JsonProperty("organic")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean organic;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    @Schema(name = "ShipmentLotCodesDto", description = "Identifiers printed on shipping documents, bags, and QR labels")
    public static class LotCodesDto {
        @JsonProperty("internal_lot_number")
        private String internalLotNumber;

        @JsonProperty("order_id")
        private String orderId;

        @JsonProperty("qr_code_tag")
        private String qrCodeTag;

        @JsonProperty("sac_numbers")
        @Schema(description = "Numbered sack identifiers (int32)")
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
