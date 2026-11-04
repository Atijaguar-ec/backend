package com.abelium.inatrace.components.integration.v1.mappers;

import com.abelium.inatrace.components.integration.v1.dto.ApiShipmentIntegration;
import com.abelium.inatrace.components.integration.v1.dto.ApiShipmentIntegration.*;
import com.abelium.inatrace.db.entities.stockorder.StockOrder;

import java.math.BigDecimal;
import java.util.Collections;

public class ShipmentIntegrationMapper {

    private static final BigDecimal KG_FACTOR = BigDecimal.ONE;

    public static ApiShipmentIntegration toDto(StockOrder order) {
        if (order == null) {
            return null;
        }

        ApiShipmentIntegration dto = new ApiShipmentIntegration();
        dto.setId(order.getId());

        // Lot codes
        LotCodesDto lotCodes = new LotCodesDto();
        lotCodes.setInternalLotNumber(order.getInternalLotNumber());
        lotCodes.setOrderId(order.getOrderId());
        lotCodes.setQrCodeTag(order.getQrCodeTag());
        if (order.getSacNumber() != null) {
            lotCodes.setSacNumbers(Collections.singletonList(order.getSacNumber()));
        } else {
            lotCodes.setSacNumbers(Collections.emptyList());
        }
        dto.setLotCodes(lotCodes);

        dto.setShipmentDate(order.getDeliveryTime() != null ? order.getDeliveryTime() : order.getProductionDate());

        if (order.getCompany() != null) {
            dto.setFromCompany(new IdNameDto(order.getCompany().getId(), order.getCompany().getName()));
        }

        if (order.getFacility() != null) {
            dto.setFromFacility(new IdNameDto(order.getFacility().getId(), FacilityIntegrationMapper.resolveFacilityName(order.getFacility())));
        }

        if (order.getConsumerCompanyCustomer() != null) {
            dto.setBuyer(new IdNameDto(order.getConsumerCompanyCustomer().getId(), order.getConsumerCompanyCustomer().getName()));
        } else if (order.getQuoteCompany() != null) {
            dto.setBuyer(new IdNameDto(order.getQuoteCompany().getId(), order.getQuoteCompany().getName()));
        }

        if (order.getSemiProduct() != null) {
            dto.setSemiProduct(new IdNameDto(order.getSemiProduct().getId(), FacilityIntegrationMapper.resolveSemiProductName(order.getSemiProduct())));
        } else if (order.getFinalProduct() != null) {
            dto.setSemiProduct(new IdNameDto(order.getFinalProduct().getId(), order.getFinalProduct().getName()));
        }

        dto.setQuantity(order.getTotalQuantity());

        var normUnit = DeliveryIntegrationMapper.resolveNormalizedUnit(order.getMeasurementUnitType(), "KG", KG_FACTOR);
        dto.setUnit(new UnitDto(normUnit.getCode(), normUnit.getKgFactor()));

        dto.setOrganic(Boolean.TRUE.equals(order.getOrganic()));
        dto.setUpdatedAt(order.getUpdateTimestamp());

        return dto;
    }
}
