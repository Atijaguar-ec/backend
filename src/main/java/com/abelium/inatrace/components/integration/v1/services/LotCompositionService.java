package com.abelium.inatrace.components.integration.v1.services;

import com.abelium.inatrace.components.integration.v1.dto.ApiCompositionIntegration;
import com.abelium.inatrace.components.integration.v1.dto.ApiCompositionIntegration.*;
import com.abelium.inatrace.components.integration.v1.mappers.DeliveryIntegrationMapper;
import com.abelium.inatrace.db.entities.processingorder.ProcessingOrder;
import com.abelium.inatrace.db.entities.stockorder.StockOrder;
import com.abelium.inatrace.db.entities.stockorder.StockOrderStatus;
import com.abelium.inatrace.db.entities.stockorder.Transaction;
import com.abelium.inatrace.db.entities.stockorder.enums.OrderType;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class LotCompositionService {

    private static final Logger logger = LoggerFactory.getLogger(LotCompositionService.class);
    private static final int MAX_DEPTH = 10;
    private static final BigDecimal LB_KG_FACTOR = new BigDecimal("0.45359237");
    private static final BigDecimal KG_FACTOR = BigDecimal.ONE;

    private final EntityManager entityManager;

    @Autowired
    public LotCompositionService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public ApiCompositionIntegration getComposition(StockOrder shipment) {
        if (shipment == null) {
            return null;
        }

        // Build shipment dto
        LotCodesDto lotCodes = new LotCodesDto(shipment.getInternalLotNumber(), shipment.getQrCodeTag());
        var normShipmentUnit = DeliveryIntegrationMapper.resolveNormalizedUnit(shipment.getMeasurementUnitType(), "KG", KG_FACTOR);
        UnitDto shipmentUnit = new UnitDto(normShipmentUnit.getCode(), normShipmentUnit.getKgFactor());
        CompositionShipmentDto shipmentDto = new CompositionShipmentDto(
                shipment.getId(),
                lotCodes,
                shipment.getTotalQuantity(),
                shipmentUnit
        );

        // Backward trace of source deliveries
        Map<Long, StockOrder> sourceDeliveriesMap = new LinkedHashMap<>();
        Set<Long> visitedProcessingOrderIds = new HashSet<>();
        Set<Long> visitedStockOrderIds = new HashSet<>();

        traceStockOrder(shipment, sourceDeliveriesMap, visitedProcessingOrderIds, visitedStockOrderIds, 0);

        List<SourceDeliveryDto> deliveryDtos = new ArrayList<>();
        for (StockOrder so : sourceDeliveriesMap.values()) {
            SourceDeliveryDto dDto = new SourceDeliveryDto();
            dDto.setDeliveryId(so.getId());
            if (so.getProducerUserCustomer() != null) {
                dDto.setFarmerId(so.getProducerUserCustomer().getId());
                dDto.setFarmerInternalId(so.getProducerUserCustomer().getFarmerCompanyInternalId());
            }
            dDto.setDeliveryReceipt(so.getDeliveryReceipt());
            dDto.setDeliveryDate(so.getDeliveryTime() != null ? so.getDeliveryTime() : so.getProductionDate());
            BigDecimal net = so.getNetQuantity() != null ? so.getNetQuantity() : so.getTotalQuantity();
            if (net == null && so.getTotalGrossQuantity() != null) {
                BigDecimal gross = so.getTotalGrossQuantity();
                BigDecimal tare = so.getTare() != null ? so.getTare() : BigDecimal.ZERO;
                BigDecimal damaged = so.getDamagedWeightDeduction() != null ? so.getDamagedWeightDeduction() : BigDecimal.ZERO;
                BigDecimal moist = so.getMoistureWeightDeduction() != null ? so.getMoistureWeightDeduction() : BigDecimal.ZERO;
                net = gross.subtract(tare).subtract(damaged).subtract(moist);
            }
            dDto.setNetQuantity(net);

            var normUnit = DeliveryIntegrationMapper.resolveNormalizedUnit(so.getMeasurementUnitType(), "LB", LB_KG_FACTOR);
            UnitDto unit = new UnitDto(normUnit.getCode(), normUnit.getKgFactor());
            dDto.setUnit(unit);
            deliveryDtos.add(dDto);
        }

        ApiCompositionIntegration result = new ApiCompositionIntegration();
        result.setShipment(shipmentDto);
        result.setSourceDeliveries(deliveryDtos);
        return result;
    }

    private void traceStockOrder(StockOrder stockOrder,
                                 Map<Long, StockOrder> sourceDeliveries,
                                 Set<Long> visitedPoIds,
                                 Set<Long> visitedSoIds,
                                 int currentDepth) {
        if (stockOrder == null || currentDepth > MAX_DEPTH || stockOrder.getStatus() == StockOrderStatus.CANCELED) {
            return;
        }

        if (stockOrder.getId() != null && !visitedSoIds.add(stockOrder.getId())) {
            // Already visited this StockOrder
            return;
        }

        // Check if this stock order is a leaf purchase order (only when traversed from root shipment)
        if (currentDepth > 0 && (stockOrder.getOrderType() == OrderType.PURCHASE_ORDER || Boolean.TRUE.equals(stockOrder.getPurchaseOrder()))) {
            sourceDeliveries.put(stockOrder.getId(), stockOrder);
            return;
        }

        // Otherwise find its origin ProcessingOrder
        ProcessingOrder po = stockOrder.getProcessingOrder();
        if (po == null && stockOrder.getId() != null) {
            List<ProcessingOrder> pos = entityManager.createQuery(
                    "SELECT po FROM ProcessingOrder po JOIN po.targetStockOrders tso WHERE tso.id = :soId",
                    ProcessingOrder.class
            ).setParameter("soId", stockOrder.getId()).getResultList();
            if (!pos.isEmpty()) {
                po = pos.get(0);
            }
        }

        if (po != null && po.getId() != null && visitedPoIds.add(po.getId())) {
            Set<Transaction> inputTransactions = po.getInputTransactions();
            if (inputTransactions == null || inputTransactions.isEmpty()) {
                inputTransactions = new HashSet<>(entityManager.createQuery(
                        "SELECT t FROM Transaction t WHERE t.targetProcessingOrder.id = :poId",
                        Transaction.class
                ).setParameter("poId", po.getId()).getResultList());
            }

            if (inputTransactions != null) {
                for (Transaction tx : inputTransactions) {
                    if (tx != null && tx.getSourceStockOrder() != null) {
                        traceStockOrder(tx.getSourceStockOrder(), sourceDeliveries, visitedPoIds, visitedSoIds, currentDepth + 1);
                    }
                }
            }
        }
    }
}
