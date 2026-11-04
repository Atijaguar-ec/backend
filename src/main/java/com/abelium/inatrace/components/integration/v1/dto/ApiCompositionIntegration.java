package com.abelium.inatrace.components.integration.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ApiCompositionIntegration {

    @JsonProperty("shipment")
    private CompositionShipmentDto shipment;

    @JsonProperty("source_deliveries")
    private List<SourceDeliveryDto> sourceDeliveries;

    public static class CompositionShipmentDto {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("lot_codes")
        private LotCodesDto lotCodes;

        @JsonProperty("quantity")
        private BigDecimal quantity;

        @JsonProperty("unit")
        private UnitDto unit;

        public CompositionShipmentDto() {}

        public CompositionShipmentDto(Long id, LotCodesDto lotCodes, BigDecimal quantity, UnitDto unit) {
            this.id = id;
            this.lotCodes = lotCodes;
            this.quantity = quantity;
            this.unit = unit;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public LotCodesDto getLotCodes() { return lotCodes; }
        public void setLotCodes(LotCodesDto lotCodes) { this.lotCodes = lotCodes; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public UnitDto getUnit() { return unit; }
        public void setUnit(UnitDto unit) { this.unit = unit; }
    }

    public static class LotCodesDto {
        @JsonProperty("internal_lot_number")
        private String internalLotNumber;

        @JsonProperty("qr_code_tag")
        private String qrCodeTag;

        public LotCodesDto() {}
        public LotCodesDto(String internalLotNumber, String qrCodeTag) {
            this.internalLotNumber = internalLotNumber;
            this.qrCodeTag = qrCodeTag;
        }

        public String getInternalLotNumber() { return internalLotNumber; }
        public void setInternalLotNumber(String internalLotNumber) { this.internalLotNumber = internalLotNumber; }
        public String getQrCodeTag() { return qrCodeTag; }
        public void setQrCodeTag(String qrCodeTag) { this.qrCodeTag = qrCodeTag; }
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

    public static class SourceDeliveryDto {
        @JsonProperty("delivery_id")
        private Long deliveryId;

        @JsonProperty("farmer_id")
        private Long farmerId;

        @JsonProperty("farmer_internal_id")
        private String farmerInternalId;

        @JsonProperty("delivery_receipt")
        private String deliveryReceipt;

        @JsonProperty("delivery_date")
        private LocalDate deliveryDate;

        @JsonProperty("net_quantity")
        private BigDecimal netQuantity;

        @JsonProperty("unit")
        private UnitDto unit;

        public SourceDeliveryDto() {}

        public SourceDeliveryDto(Long deliveryId, Long farmerId, String farmerInternalId,
                                 String deliveryReceipt, LocalDate deliveryDate,
                                 BigDecimal netQuantity, UnitDto unit) {
            this.deliveryId = deliveryId;
            this.farmerId = farmerId;
            this.farmerInternalId = farmerInternalId;
            this.deliveryReceipt = deliveryReceipt;
            this.deliveryDate = deliveryDate;
            this.netQuantity = netQuantity;
            this.unit = unit;
        }

        public Long getDeliveryId() { return deliveryId; }
        public void setDeliveryId(Long deliveryId) { this.deliveryId = deliveryId; }
        public Long getFarmerId() { return farmerId; }
        public void setFarmerId(Long farmerId) { this.farmerId = farmerId; }
        public String getFarmerInternalId() { return farmerInternalId; }
        public void setFarmerInternalId(String farmerInternalId) { this.farmerInternalId = farmerInternalId; }
        public String getDeliveryReceipt() { return deliveryReceipt; }
        public void setDeliveryReceipt(String deliveryReceipt) { this.deliveryReceipt = deliveryReceipt; }
        public LocalDate getDeliveryDate() { return deliveryDate; }
        public void setDeliveryDate(LocalDate deliveryDate) { this.deliveryDate = deliveryDate; }
        public BigDecimal getNetQuantity() { return netQuantity; }
        public void setNetQuantity(BigDecimal netQuantity) { this.netQuantity = netQuantity; }
        public UnitDto getUnit() { return unit; }
        public void setUnit(UnitDto unit) { this.unit = unit; }
    }

    public ApiCompositionIntegration() {}

    public CompositionShipmentDto getShipment() { return shipment; }
    public void setShipment(CompositionShipmentDto shipment) { this.shipment = shipment; }

    public List<SourceDeliveryDto> getSourceDeliveries() { return sourceDeliveries; }
    public void setSourceDeliveries(List<SourceDeliveryDto> sourceDeliveries) { this.sourceDeliveries = sourceDeliveries; }
}
