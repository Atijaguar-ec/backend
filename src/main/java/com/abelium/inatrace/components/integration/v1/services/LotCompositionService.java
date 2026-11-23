package com.abelium.inatrace.components.integration.v1.services;

import com.abelium.inatrace.components.integration.v1.dto.ApiCompositionIntegration;
import com.abelium.inatrace.components.integration.v1.dto.ApiCompositionIntegration.*;
import com.abelium.inatrace.components.integration.v1.mappers.DeliveryIntegrationMapper;
import com.abelium.inatrace.db.entities.codebook.SemiProduct;
import com.abelium.inatrace.db.entities.processingaction.ProcessingAction;
import com.abelium.inatrace.db.entities.processingorder.ProcessingOrder;
import com.abelium.inatrace.db.entities.stockorder.StockOrder;
import com.abelium.inatrace.db.entities.stockorder.StockOrderStatus;
import com.abelium.inatrace.db.entities.stockorder.Transaction;
import com.abelium.inatrace.db.entities.stockorder.enums.OrderType;
import com.abelium.inatrace.db.entities.stockorder.enums.TransactionStatus;
import com.abelium.inatrace.types.ProcessingActionType;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class LotCompositionService {

    private static final Logger logger = LoggerFactory.getLogger(LotCompositionService.class);
    private static final int MAX_DEPTH = 25;
    private static final BigDecimal LB_KG_FACTOR = new BigDecimal("0.45359237");
    private static final BigDecimal KG_FACTOR = BigDecimal.ONE;

    private final EntityManager entityManager;

    @Autowired
    public LotCompositionService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public static String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Normalizes an internal lot number to its batch stem by trimming whitespace, collapsing spaces,
     * normalizing CCN-51 spacing ("ccn 51" -> "ccn51"), and stripping trailing "/1", "/7/1" slice suffixes.
     */
    public static String normalizeLotStem(String lotNumber) {
        if (lotNumber == null) {
            return "";
        }
        String s = lotNumber.trim().toLowerCase(Locale.ROOT);
        if (s.isEmpty()) {
            return "";
        }
        // Strip trailing slash-number slice suffixes (including spaces before slash, e.g. "SEMANA 7 NACIONAL /1")
        s = s.replaceAll("(?:\\s*/\\s*\\d+)+$", "").trim();
        s = s.replaceAll("\\s+", " ");
        s = s.replace("ccn 51", "ccn51").replace("ccn-51", "ccn51");
        return s;
    }

    @Transactional(readOnly = true)
    public ApiCompositionIntegration getComposition(StockOrder shipment) {
        if (shipment == null) {
            return null;
        }

        CompositionLotCodesDto lotCodes = new CompositionLotCodesDto(
                trimOrNull(shipment.getInternalLotNumber()),
                trimOrNull(shipment.getQrCodeTag())
        );
        var normShipmentUnit = DeliveryIntegrationMapper.resolveNormalizedUnit(shipment.getMeasurementUnitType(), "KG", KG_FACTOR);
        UnitSummaryDto shipmentUnit = new UnitSummaryDto(normShipmentUnit.getCode(), normShipmentUnit.getKgFactor());
        CompositionShipmentDto shipmentDto = new CompositionShipmentDto(
                shipment.getId(),
                lotCodes,
                shipment.getTotalQuantity(),
                shipmentUnit
        );

        Map<Long, StockOrder> sourceDeliveriesMap = new TreeMap<>();
        Set<Long> visitedProcessingOrderIds = new HashSet<>();
        Set<Long> visitedStockOrderIds = new HashSet<>();
        Set<Long> claimedFallbackDeliveryIds = new HashSet<>();

        traceStockOrder(
                shipment,
                sourceDeliveriesMap,
                visitedProcessingOrderIds,
                visitedStockOrderIds,
                claimedFallbackDeliveryIds,
                0
        );

        List<SourceDeliveryDto> deliveryDtos = new ArrayList<>();
        for (StockOrder so : sourceDeliveriesMap.values()) {
            SourceDeliveryDto dDto = new SourceDeliveryDto();
            dDto.setDeliveryId(so.getId());
            if (so.getProducerUserCustomer() != null) {
                dDto.setFarmerId(so.getProducerUserCustomer().getId());
                dDto.setFarmerInternalId(trimOrNull(so.getProducerUserCustomer().getFarmerCompanyInternalId()));
            }
            dDto.setDeliveryReceipt(trimOrNull(so.getDeliveryReceipt()));
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
            UnitSummaryDto unit = new UnitSummaryDto(normUnit.getCode(), normUnit.getKgFactor());
            dDto.setUnit(unit);
            deliveryDtos.add(dDto);
        }

        ApiCompositionIntegration result = new ApiCompositionIntegration();
        result.setShipment(shipmentDto);
        result.setSourceDeliveries(deliveryDtos);
        return result;
    }

    private boolean isPrimaryPurchaseOrder(StockOrder so) {
        return so.getOrderType() == OrderType.PURCHASE_ORDER || Boolean.TRUE.equals(so.getPurchaseOrder());
    }

    private void traceStockOrder(StockOrder stockOrder,
                                 Map<Long, StockOrder> sourceDeliveries,
                                 Set<Long> visitedPoIds,
                                 Set<Long> visitedSoIds,
                                 Set<Long> claimedFallbackDeliveryIds,
                                 int currentDepth) {
        if (stockOrder == null || currentDepth > MAX_DEPTH || stockOrder.getStatus() == StockOrderStatus.CANCELED) {
            return;
        }

        if (stockOrder.getId() != null && !visitedSoIds.add(stockOrder.getId())) {
            return;
        }

        if (currentDepth > 0 && isPrimaryPurchaseOrder(stockOrder)) {
            if (stockOrder.getId() != null) {
                sourceDeliveries.put(stockOrder.getId(), stockOrder);
                claimedFallbackDeliveryIds.add(stockOrder.getId());
            } else {
                sourceDeliveries.put((long) (-sourceDeliveries.size() - 1), stockOrder);
            }
            return;
        }

        // 1. Traverse repackedOriginStockOrderId if present (bagging / multi-sack repacking)
        if (stockOrder.getRepackedOriginStockOrderId() != null && !stockOrder.getRepackedOriginStockOrderId().isBlank()) {
            StockOrder repackedOrigin = findOriginByRepackedId(stockOrder.getRepackedOriginStockOrderId().trim());
            if (repackedOrigin != null) {
                traceStockOrder(repackedOrigin, sourceDeliveries, visitedPoIds, visitedSoIds, claimedFallbackDeliveryIds, currentDepth + 1);
            }
        }

        // 2. Collect ALL ProcessingOrders that produced this StockOrder
        List<ProcessingOrder> originPos = collectOriginProcessingOrders(stockOrder);

        for (ProcessingOrder po : originPos) {
            if (po == null) {
                continue;
            }
            boolean isTransfer = po.getProcessingAction() != null
                    && po.getProcessingAction().getType() == ProcessingActionType.TRANSFER;

            // Note: for TRANSFER orders with multiple targetStockOrders (1-to-1 per slice), we evaluate per StockOrder
            // when transactions are incomplete; for consolidated PROCESSING orders we visit each PO once.
            if (!isTransfer && po.getId() != null && !visitedPoIds.add(po.getId())) {
                continue;
            }

            List<Transaction> activeTxs = loadActiveInputTransactions(po);
            int targetCount = (po.getTargetStockOrders() != null) ? po.getTargetStockOrders().size() : 1;

            if (!activeTxs.isEmpty()) {
                if (isTransfer && targetCount > 1 && activeTxs.size() == targetCount) {
                    // Pair 1-to-1 when sorted by ID if possible, otherwise traverse all
                    StockOrder pairedSource = findPairedTransferSource(stockOrder, po, activeTxs);
                    if (pairedSource != null) {
                        traceStockOrder(pairedSource, sourceDeliveries, visitedPoIds, visitedSoIds, claimedFallbackDeliveryIds, currentDepth + 1);
                        continue;
                    }
                }
                if (!isTransfer || activeTxs.size() >= targetCount) {
                    for (Transaction tx : activeTxs) {
                        if (tx.getSourceStockOrder() != null) {
                            traceStockOrder(tx.getSourceStockOrder(), sourceDeliveries, visitedPoIds, visitedSoIds, claimedFallbackDeliveryIds, currentDepth + 1);
                        }
                    }
                    continue;
                }
                // Incomplete TRANSFER PO (e.g. PO 1 in migrated staging where only 1 of 7 transactions survived):
                // If this specific target slice matches an existing transaction's quantity, use that transaction;
                // otherwise recover this slice's upstream source below.
                Transaction exactSliceTx = findMatchingTransferTransactionForSlice(stockOrder, po, activeTxs);
                if (exactSliceTx != null && exactSliceTx.getSourceStockOrder() != null) {
                    traceStockOrder(exactSliceTx.getSourceStockOrder(), sourceDeliveries, visitedPoIds, visitedSoIds, claimedFallbackDeliveryIds, currentDepth + 1);
                    continue;
                }
            }

            // 3. Historical orphan-PO recovery for migrated batches whose Transaction rows were omitted
            List<StockOrder> recoveredSources = resolveHistoricalUnlinkedInputs(stockOrder, po, activeTxs, claimedFallbackDeliveryIds);
            for (StockOrder src : recoveredSources) {
                traceStockOrder(src, sourceDeliveries, visitedPoIds, visitedSoIds, claimedFallbackDeliveryIds, currentDepth + 1);
            }
        }
    }

    private StockOrder findOriginByRepackedId(String repackedId) {
        if (entityManager == null) {
            return null;
        }
        try {
            if (repackedId.matches("\\d+")) {
                StockOrder byId = entityManager.find(StockOrder.class, Long.parseLong(repackedId));
                if (byId != null) {
                    return byId;
                }
            }
            List<StockOrder> byIdentifier = entityManager.createQuery(
                    "SELECT so FROM StockOrder so WHERE so.identifier = :ident ORDER BY so.id ASC",
                    StockOrder.class
            ).setParameter("ident", repackedId).setMaxResults(1).getResultList();
            return (byIdentifier != null && !byIdentifier.isEmpty()) ? byIdentifier.get(0) : null;
        } catch (Exception e) {
            logger.debug("Could not resolve repackedOriginStockOrderId '{}': {}", repackedId, e.getMessage());
            return null;
        }
    }

    private List<ProcessingOrder> collectOriginProcessingOrders(StockOrder stockOrder) {
        Map<Long, ProcessingOrder> ordered = new LinkedHashMap<>();
        List<ProcessingOrder> transientPos = new ArrayList<>();

        if (stockOrder.getProcessingOrder() != null) {
            ProcessingOrder direct = stockOrder.getProcessingOrder();
            if (direct.getId() != null) {
                ordered.put(direct.getId(), direct);
            } else {
                transientPos.add(direct);
            }
        }

        if (entityManager != null && stockOrder.getId() != null) {
            try {
                List<ProcessingOrder> queried = entityManager.createQuery(
                        "SELECT po FROM ProcessingOrder po JOIN po.targetStockOrders tso WHERE tso.id = :soId ORDER BY po.id ASC",
                        ProcessingOrder.class
                ).setParameter("soId", stockOrder.getId()).getResultList();
                if (queried != null) {
                    for (ProcessingOrder po : queried) {
                        if (po != null && po.getId() != null) {
                            ordered.putIfAbsent(po.getId(), po);
                        }
                    }
                }
            } catch (Exception e) {
                logger.debug("Could not query origin ProcessingOrders for StockOrder {}: {}", stockOrder.getId(), e.getMessage());
            }
        }

        List<ProcessingOrder> result = new ArrayList<>(ordered.values());
        result.addAll(transientPos);
        return result;
    }

    private List<Transaction> loadActiveInputTransactions(ProcessingOrder po) {
        List<Transaction> result = new ArrayList<>();
        if (entityManager != null && po.getId() != null) {
            try {
                List<Transaction> queried = entityManager.createQuery(
                        "SELECT t FROM Transaction t LEFT JOIN FETCH t.sourceStockOrder " +
                        "WHERE t.targetProcessingOrder.id = :poId " +
                        "AND (t.status IS NULL OR t.status <> com.abelium.inatrace.db.entities.stockorder.enums.TransactionStatus.CANCELED) " +
                        "ORDER BY t.id ASC",
                        Transaction.class
                ).setParameter("poId", po.getId()).getResultList();
                if (queried != null && !queried.isEmpty()) {
                    return queried;
                }
            } catch (Exception e) {
                logger.debug("Falling back to in-memory inputTransactions for PO {}: {}", po.getId(), e.getMessage());
            }
        }
        if (po.getInputTransactions() != null) {
            for (Transaction tx : po.getInputTransactions()) {
                if (tx != null && tx.getStatus() != TransactionStatus.CANCELED) {
                    result.add(tx);
                }
            }
            result.sort(Comparator.comparing(Transaction::getId, Comparator.nullsLast(Comparator.naturalOrder())));
        }
        return result;
    }

    private StockOrder findPairedTransferSource(StockOrder targetSlice, ProcessingOrder po, List<Transaction> activeTxs) {
        if (targetSlice.getId() == null || po.getTargetStockOrders() == null) {
            return null;
        }
        List<StockOrder> sortedTargets = new ArrayList<>(po.getTargetStockOrders());
        sortedTargets.sort(Comparator.comparing(StockOrder::getId, Comparator.nullsLast(Comparator.naturalOrder())));
        for (int i = 0; i < sortedTargets.size() && i < activeTxs.size(); i++) {
            if (targetSlice.getId().equals(sortedTargets.get(i).getId())) {
                return activeTxs.get(i).getSourceStockOrder();
            }
        }
        return null;
    }

    private Transaction findMatchingTransferTransactionForSlice(StockOrder targetSlice, ProcessingOrder po, List<Transaction> activeTxs) {
        if (targetSlice.getTotalQuantity() == null || po.getTargetStockOrders() == null) {
            return null;
        }
        List<StockOrder> sortedTargets = new ArrayList<>(po.getTargetStockOrders());
        sortedTargets.sort(Comparator.comparing(StockOrder::getId, Comparator.nullsLast(Comparator.naturalOrder())));
        // Only match the first target slice with that quantity against the surviving transaction
        for (Transaction tx : activeTxs) {
            BigDecimal txQty = tx.getInputQuantity();
            if (txQty != null && txQty.compareTo(targetSlice.getTotalQuantity()) == 0) {
                for (StockOrder tso : sortedTargets) {
                    if (tso.getTotalQuantity() != null && tso.getTotalQuantity().compareTo(txQty) == 0) {
                        if (Objects.equals(tso.getId(), targetSlice.getId())) {
                            return tx;
                        }
                        break;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Deterministic lineage recovery for migrated historical ProcessingOrders whose Transaction links
     * were dropped during legacy database migration. Never invoked when normal Transaction edges exist.
     */
    private List<StockOrder> resolveHistoricalUnlinkedInputs(StockOrder stockOrder,
                                                             ProcessingOrder po,
                                                             List<Transaction> existingTxs,
                                                             Set<Long> claimedFallbackDeliveryIds) {
        if (entityManager == null || stockOrder.getId() == null || stockOrder.getCompany() == null || stockOrder.getCompany().getId() == null) {
            return Collections.emptyList();
        }

        Long companyId = stockOrder.getCompany().getId();
        ProcessingAction action = po.getProcessingAction();
        SemiProduct inputSemi = (action != null) ? action.getInputSemiProduct() : null;
        Long inputSemiId = (inputSemi != null) ? inputSemi.getId() : null;
        String lotStem = normalizeLotStem(stockOrder.getInternalLotNumber());

        // Step A: Look for upstream intermediate (non-purchase) StockOrders matching company, inputSemiProduct, and lot stem
        if (inputSemiId != null && !lotStem.isEmpty()) {
            try {
                List<StockOrder> candidates = entityManager.createQuery(
                        "SELECT so FROM StockOrder so " +
                        "LEFT JOIN FETCH so.processingOrder " +
                        "LEFT JOIN FETCH so.company " +
                        "LEFT JOIN FETCH so.semiProduct " +
                        "LEFT JOIN FETCH so.measurementUnitType " +
                        "WHERE so.company.id = :compId " +
                        "AND so.id < :curId " +
                        "AND (so.orderType IS NULL OR so.orderType <> com.abelium.inatrace.db.entities.stockorder.enums.OrderType.PURCHASE_ORDER) " +
                        "AND (so.status IS NULL OR so.status <> com.abelium.inatrace.db.entities.stockorder.StockOrderStatus.CANCELED) " +
                        "AND so.semiProduct.id = :inSemiId " +
                        "ORDER BY so.id DESC",
                        StockOrder.class
                ).setParameter("compId", companyId)
                 .setParameter("curId", stockOrder.getId())
                 .setParameter("inSemiId", inputSemiId)
                 .getResultList();

                List<StockOrder> stemMatched = new ArrayList<>();
                if (candidates != null) {
                    Long nearestPoId = null;
                    for (StockOrder cand : candidates) {
                        if (Objects.equals(cand.getProcessingOrder() != null ? cand.getProcessingOrder().getId() : null, po.getId())) {
                            continue; // Skip sibling outputs of the same ProcessingOrder
                        }
                        if (lotStem.equals(normalizeLotStem(cand.getInternalLotNumber()))) {
                            Long candPoId = cand.getProcessingOrder() != null ? cand.getProcessingOrder().getId() : null;
                            if (nearestPoId == null && candPoId != null) {
                                nearestPoId = candPoId;
                            }
                            // Take all slices from the immediately preceding stage ProcessingOrder
                            if (nearestPoId == null || Objects.equals(candPoId, nearestPoId)) {
                                stemMatched.add(cand);
                            }
                        }
                    }
                }
                if (!stemMatched.isEmpty()) {
                    stemMatched.sort(Comparator.comparing(StockOrder::getId));
                    return stemMatched;
                }
            } catch (Exception e) {
                logger.debug("Historical stage lookup failed for StockOrder {}: {}", stockOrder.getId(), e.getMessage());
            }
        }

        // Step B: First-hop wet cocoa reception TRANSFER -> match against consumed primary PURCHASE_ORDER deliveries
        if (action != null && action.getType() == ProcessingActionType.TRANSFER && stockOrder.getTotalQuantity() != null) {
            try {
                Set<Long> alreadyLinkedIds = new HashSet<>(claimedFallbackDeliveryIds);
                for (Transaction tx : existingTxs) {
                    if (tx.getSourceStockOrder() != null && tx.getSourceStockOrder().getId() != null) {
                        alreadyLinkedIds.add(tx.getSourceStockOrder().getId());
                    }
                }

                List<StockOrder> purchases = entityManager.createQuery(
                        "SELECT del FROM StockOrder del " +
                        "LEFT JOIN FETCH del.producerUserCustomer " +
                        "LEFT JOIN FETCH del.measurementUnitType " +
                        "WHERE del.company.id = :compId " +
                        "AND del.id < :curId " +
                        "AND del.orderType = com.abelium.inatrace.db.entities.stockorder.enums.OrderType.PURCHASE_ORDER " +
                        "AND (del.status IS NULL OR del.status <> com.abelium.inatrace.db.entities.stockorder.StockOrderStatus.CANCELED) " +
                        "AND (del.availableQuantity IS NULL OR del.totalQuantity IS NULL OR del.availableQuantity < del.totalQuantity OR del.availableQuantity = 0) " +
                        "ORDER BY del.id DESC",
                        StockOrder.class
                ).setParameter("compId", companyId)
                 .setParameter("curId", stockOrder.getId())
                 .getResultList();

                if (purchases != null) {
                    BigDecimal targetQty = stockOrder.getTotalQuantity();
                    LocalDate targetDate = stockOrder.getDeliveryTime() != null ? stockOrder.getDeliveryTime() : stockOrder.getProductionDate();

                    // Pass 1: Exact weight + matching reception date
                    for (StockOrder del : purchases) {
                        if (alreadyLinkedIds.contains(del.getId())) {
                            continue;
                        }
                        LocalDate delDate = del.getDeliveryTime() != null ? del.getDeliveryTime() : del.getProductionDate();
                        boolean weightMatches = (del.getTotalGrossQuantity() != null && del.getTotalGrossQuantity().compareTo(targetQty) == 0)
                                || (del.getTotalQuantity() != null && del.getTotalQuantity().compareTo(targetQty) == 0);
                        if (weightMatches && (targetDate == null || Objects.equals(targetDate, delDate))) {
                            claimedFallbackDeliveryIds.add(del.getId());
                            return Collections.singletonList(del);
                        }
                    }
                    // Pass 2: Exact weight in the immediately preceding batch regardless of 1-day reception lag
                    for (StockOrder del : purchases) {
                        if (alreadyLinkedIds.contains(del.getId())) {
                            continue;
                        }
                        boolean weightMatches = (del.getTotalGrossQuantity() != null && del.getTotalGrossQuantity().compareTo(targetQty) == 0)
                                || (del.getTotalQuantity() != null && del.getTotalQuantity().compareTo(targetQty) == 0);
                        if (weightMatches) {
                            claimedFallbackDeliveryIds.add(del.getId());
                            return Collections.singletonList(del);
                        }
                    }
                }
            } catch (Exception e) {
                logger.debug("Historical purchase lookup failed for StockOrder {}: {}", stockOrder.getId(), e.getMessage());
            }
        }

        return Collections.emptyList();
    }
}
