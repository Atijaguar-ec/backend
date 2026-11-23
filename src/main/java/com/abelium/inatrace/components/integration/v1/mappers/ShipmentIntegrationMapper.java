package com.abelium.inatrace.components.integration.v1.mappers;

import com.abelium.inatrace.components.integration.v1.dto.ApiShipmentIntegration;
import com.abelium.inatrace.components.integration.v1.dto.ApiShipmentIntegration.*;
import com.abelium.inatrace.components.integration.v1.services.LotCompositionService;
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

        // Lot codes (trim trailing spaces from legacy records such as "SEMANA 7 NACIONAL ")
        ShipmentLotCodesDto lotCodes = new ShipmentLotCodesDto();
        lotCodes.setInternalLotNumber(LotCompositionService.trimOrNull(order.getInternalLotNumber()));
        lotCodes.setOrderId(LotCompositionService.trimOrNull(order.getOrderId()));
        lotCodes.setQrCodeTag(LotCompositionService.trimOrNull(order.getQrCodeTag()));
        if (order.getSacNumber() != null) {
            lotCodes.setSacNumbers(Collections.singletonList(order.getSacNumber()));
        } else {
            lotCodes.setSacNumbers(Collections.emptyList());
        }
        dto.setLotCodes(lotCodes);

        dto.setShipmentDate(order.getDeliveryTime() != null ? order.getDeliveryTime() : order.getProductionDate());

        if (order.getCompany() != null) {
            dto.setFromCompany(new IdNameDto(order.getCompany().getId(), LotCompositionService.trimOrNull(order.getCompany().getName())));
        }

        if (order.getFacility() != null) {
            dto.setFromFacility(new IdNameDto(order.getFacility().getId(), FacilityIntegrationMapper.resolveFacilityName(order.getFacility())));
        }

        if (order.getConsumerCompanyCustomer() != null) {
            dto.setBuyer(new IdNameDto(order.getConsumerCompanyCustomer().getId(), LotCompositionService.trimOrNull(order.getConsumerCompanyCustomer().getName())));
        } else if (order.getQuoteCompany() != null) {
            dto.setBuyer(new IdNameDto(order.getQuoteCompany().getId(), LotCompositionService.trimOrNull(order.getQuoteCompany().getName())));
        }

        if (order.getSemiProduct() != null) {
            dto.setSemiProduct(new IdNameDto(order.getSemiProduct().getId(), FacilityIntegrationMapper.resolveSemiProductName(order.getSemiProduct())));
        } else if (order.getFinalProduct() != null) {
            dto.setSemiProduct(new IdNameDto(order.getFinalProduct().getId(), LotCompositionService.trimOrNull(order.getFinalProduct().getName())));
        }

        dto.setQuantity(order.getTotalQuantity());

        var normUnit = DeliveryIntegrationMapper.resolveNormalizedUnit(order.getMeasurementUnitType(), "KG", KG_FACTOR);
        dto.setUnit(new UnitSummaryDto(normUnit.getCode(), normUnit.getKgFactor()));

        dto.setOrganic(Boolean.TRUE.equals(order.getOrganic()));
        dto.setUpdatedAt(order.getUpdateTimestamp());

        return dto;
    }
}
