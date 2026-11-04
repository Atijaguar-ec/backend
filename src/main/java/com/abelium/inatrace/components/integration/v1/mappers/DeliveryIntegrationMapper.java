package com.abelium.inatrace.components.integration.v1.mappers;

import com.abelium.inatrace.components.integration.v1.dto.ApiDeliveryIntegration;
import com.abelium.inatrace.components.integration.v1.dto.ApiDeliveryIntegration.*;
import com.abelium.inatrace.db.entities.codebook.MeasureUnitType;
import com.abelium.inatrace.db.entities.common.Plot;
import com.abelium.inatrace.db.entities.stockorder.StockOrder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class DeliveryIntegrationMapper {

    private static final BigDecimal LB_KG_FACTOR = new BigDecimal("0.45359237");
    private static final BigDecimal QQ_KG_FACTOR = new BigDecimal("45.359237");

    public static UnitDto resolveNormalizedUnit(MeasureUnitType mut, String defaultCode, BigDecimal defaultFactor) {
        if (mut == null) {
            return new UnitDto(defaultCode, defaultFactor);
        }
        String rawCode = mut.getCode() != null ? mut.getCode().trim().toUpperCase() : defaultCode;
        if ("PESOLIBRA".equals(rawCode) || "LB".equals(rawCode) || "LIBRA".equals(rawCode) || "LIBRAS".equals(rawCode)) {
            return new UnitDto("LB", LB_KG_FACTOR);
        }
        if ("PESOKG".equals(rawCode) || "PESOKILO".equals(rawCode) || "KG".equals(rawCode) || "KILO".equals(rawCode)) {
            return new UnitDto("KG", BigDecimal.ONE);
        }
        if ("QUINTALES".equals(rawCode) || "QUINTAL".equals(rawCode) || "QQ".equals(rawCode)) {
            return new UnitDto("QQ", QQ_KG_FACTOR);
        }
        BigDecimal factor = mut.getWeight() != null ? mut.getWeight() : defaultFactor;
        return new UnitDto(mut.getCode() != null ? mut.getCode() : defaultCode, factor);
    }

    public static ApiDeliveryIntegration toDto(StockOrder order) {
        return toDto(order, null);
    }

    public static ApiDeliveryIntegration toDto(StockOrder order, List<Plot> preloadedPlots) {
        if (order == null) {
            return null;
        }

        ApiDeliveryIntegration dto = new ApiDeliveryIntegration();
        dto.setId(order.getId());
        dto.setDeliveryReceipt(order.getDeliveryReceipt());

        if (order.getCompany() != null) {
            dto.setCompany(new IdNameDto(order.getCompany().getId(), order.getCompany().getName()));
        }

        if (order.getFacility() != null) {
            dto.setFacility(new IdNameDto(order.getFacility().getId(), FacilityIntegrationMapper.resolveFacilityName(order.getFacility())));
        }

        if (order.getProducerUserCustomer() != null) {
            dto.setFarmerId(order.getProducerUserCustomer().getId());
            dto.setFarmerInternalId(order.getProducerUserCustomer().getFarmerCompanyInternalId());
        }

        dto.setDeliveryDate(order.getDeliveryTime() != null ? order.getDeliveryTime() : order.getProductionDate());

        if (order.getSemiProduct() != null) {
            dto.setSemiProduct(new IdNameDto(order.getSemiProduct().getId(), FacilityIntegrationMapper.resolveSemiProductName(order.getSemiProduct())));
        } else {
            dto.setSemiProduct(new IdNameDto(1L, "Cacao en baba"));
        }

        // Variety
        String varietyStr = order.getVariety();
        if (varietyStr != null) {
            if ("CCN51".equalsIgnoreCase(varietyStr) || "2".equals(varietyStr)) {
                dto.setVariety(new CodeLabelDto("CCN51", "CCN-51"));
            } else if ("NACIONAL".equalsIgnoreCase(varietyStr) || "ORGANICO".equalsIgnoreCase(varietyStr) || "1".equals(varietyStr)) {
                dto.setVariety(new CodeLabelDto("NACIONAL", "Nacional / Fino de Aroma"));
            } else {
                dto.setVariety(new CodeLabelDto(varietyStr, varietyStr));
            }
        }

        dto.setParcelLot(order.getParcelLot());

        // Resolve plot_id from parcelLot
        List<Plot> plots = preloadedPlots != null ? preloadedPlots
                : (order.getProducerUserCustomer() != null && order.getProducerUserCustomer().getPlots() != null
                        ? new java.util.ArrayList<>(order.getProducerUserCustomer().getPlots()) : null);

        if (order.getParcelLot() != null && plots != null) {
            String target = order.getParcelLot().trim();
            Plot matchedPlot = null;
            for (Plot p : plots) {
                if (p.getPlotName() != null && p.getPlotName().trim().equalsIgnoreCase(target)) {
                    matchedPlot = p;
                    break;
                }
            }
            if (matchedPlot == null && target.matches("\\d+")) {
                int pos = Integer.parseInt(target);
                List<Plot> sortedPlots = plots.stream()
                        .sorted(java.util.Comparator.comparing(Plot::getId, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())))
                        .collect(java.util.stream.Collectors.toList());
                if (pos >= 1 && pos <= sortedPlots.size()) {
                    matchedPlot = sortedPlots.get(pos - 1);
                }
            }
            if (matchedPlot != null) {
                dto.setPlotId(matchedPlot.getId());
            }
        }

        dto.setOrganic(Boolean.TRUE.equals(order.getOrganic()));

        // Unit
        dto.setUnit(resolveNormalizedUnit(order.getMeasurementUnitType(), "LB", LB_KG_FACTOR));

        // Weights
        BigDecimal gross = order.getTotalGrossQuantity() != null ? order.getTotalGrossQuantity() : order.getTotalQuantity();
        BigDecimal tare = order.getTare() != null ? order.getTare() : BigDecimal.ZERO;
        BigDecimal damagedDeduction = order.getDamagedWeightDeduction() != null ? order.getDamagedWeightDeduction() : BigDecimal.ZERO;
        BigDecimal moisturePct = order.getMoisturePercentage();
        BigDecimal moistureDeduction = order.getMoistureWeightDeduction();

        if (moistureDeduction == null && moisturePct != null && gross != null) {
            BigDecimal baseQty = gross.subtract(tare);
            if (baseQty.compareTo(BigDecimal.ZERO) > 0) {
                moistureDeduction = baseQty.multiply(moisturePct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
        }

        dto.setGrossQuantity(gross);
        dto.setTare(tare);
        dto.setDamagedWeightDeduction(damagedDeduction);
        dto.setMoisturePercentage(moisturePct);
        dto.setMoistureWeightDeduction(moistureDeduction);

        BigDecimal net = order.getNetQuantity() != null ? order.getNetQuantity() : order.getTotalQuantity();
        if (net == null && gross != null) {
            BigDecimal baseQty = gross.subtract(tare);
            BigDecimal moist = moistureDeduction != null ? moistureDeduction : BigDecimal.ZERO;
            net = baseQty.subtract(damagedDeduction).subtract(moist);
        }
        dto.setNetQuantity(net);

        // Strict LOPDP: NO pricePerUnit, NO cost, NO paid, NO balance, NO totalAmount
        dto.setCreatedAt(order.getCreationTimestamp());
        dto.setUpdatedAt(order.getUpdateTimestamp());

        return dto;
    }
}
