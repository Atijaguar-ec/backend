package com.abelium.inatrace.components.processingorder.mappers;

import com.abelium.inatrace.components.processingaction.ProcessingActionMapper;
import com.abelium.inatrace.components.processingorder.api.ApiProcessingOrder;
import com.abelium.inatrace.components.stockorder.mappers.StockOrderMapper;
import com.abelium.inatrace.components.transaction.mappers.TransactionMapper;
import com.abelium.inatrace.db.entities.processingorder.ProcessingOrder;
import com.abelium.inatrace.db.entities.stockorder.StockOrder;
import com.abelium.inatrace.types.Language;

import java.util.Comparator;
import java.util.stream.Collectors;

public class ProcessingOrderMapper {

    static final Comparator<StockOrder> TARGET_STOCK_ORDER_ORDER = Comparator
            .comparing(StockOrder::getSacNumber, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(StockOrder::getId, Comparator.nullsLast(Comparator.naturalOrder()));

    public static ApiProcessingOrder toApiProcessingOrderBase(ProcessingOrder entity) {

        if (entity == null) {
            return null;
        }

        ApiProcessingOrder apiProcessingOrder = new ApiProcessingOrder();
        apiProcessingOrder.setId(entity.getId());
        apiProcessingOrder.setInitiatorUserId(entity.getInitiatorUserId());

        return apiProcessingOrder;
    }

    public static ApiProcessingOrder toApiProcessingOrder(ProcessingOrder entity, Language language) {

        ApiProcessingOrder apiProcessingOrder = toApiProcessingOrderBase(entity);

        if (apiProcessingOrder == null) {
            return null;
        }

        apiProcessingOrder.setCreationTimestamp(entity.getCreationTimestamp());
        apiProcessingOrder.setProcessingDate(entity.getProcessingDate());
        apiProcessingOrder.setProcessingAction(ProcessingActionMapper.toApiProcessingAction(entity.getProcessingAction(), language));
        apiProcessingOrder.setInputTransactions(entity.getInputTransactions().stream().map(transaction -> TransactionMapper.toApiTransactionBase(transaction, language)).collect(Collectors.toList()));
        // 'targetStockOrders' is a Set (no stable order): sort by sac number so repacked outputs are shown in order
        apiProcessingOrder.setTargetStockOrders(entity.getTargetStockOrders().stream()
                .sorted(TARGET_STOCK_ORDER_ORDER)
                .map(so -> StockOrderMapper.toApiStockOrder(so ,null, language)).collect(Collectors.toList()));

        return apiProcessingOrder;
    }

    public static ApiProcessingOrder toApiProcessingOrderHistory(ProcessingOrder entity, Language language) {

        ApiProcessingOrder apiProcessingOrder = toApiProcessingOrderBase(entity);

        if (apiProcessingOrder == null) {
            return null;
        }

        apiProcessingOrder.setCreationTimestamp(entity.getCreationTimestamp());
        apiProcessingOrder.setProcessingDate(entity.getProcessingDate());
        apiProcessingOrder.setProcessingAction(ProcessingActionMapper.toApiProcessingActionHistory(entity.getProcessingAction(), language));

        return apiProcessingOrder;
    }

}
