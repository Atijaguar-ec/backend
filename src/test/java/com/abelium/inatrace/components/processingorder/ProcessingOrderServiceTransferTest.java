package com.abelium.inatrace.components.processingorder;

import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.processingorder.api.ApiProcessingOrder;
import com.abelium.inatrace.components.stockorder.api.ApiStockOrder;
import com.abelium.inatrace.components.transaction.api.ApiTransaction;
import com.abelium.inatrace.db.entities.processingorder.ProcessingOrder;
import com.abelium.inatrace.db.entities.stockorder.StockOrder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Una accion TRANSFER empareja por posicion cada Transaction de entrada con una StockOrder de salida.
 * La salida es una StockOrder propia, con ID distinto al de la de origen (en la orden 316 de Fortaleza
 * del Valle: salidas 7077-7083, origenes 6431-6437). Exigir que los ID coincidieran hacia imposible
 * editar una transferencia guardada.
 */
class ProcessingOrderServiceTransferTest {

    private static final String VALIDATE_METHOD = "validateTransferTargetStockOrders";

    /** Invoca el metodo privado de validacion y propaga la ApiException tal cual. */
    private void validate(ProcessingOrder entity, ApiProcessingOrder api) throws Exception {
        ProcessingOrderService service = new ProcessingOrderService(null, null, null);

        Method method = ProcessingOrderService.class.getDeclaredMethod(
                VALIDATE_METHOD, ProcessingOrder.class, ApiProcessingOrder.class);
        method.setAccessible(true);

        try {
            method.invoke(service, entity, api);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof ApiException) {
                throw (ApiException) e.getCause();
            }
            throw e;
        }
    }

    private ProcessingOrder savedOrderWithTargets(Long orderId, Long... targetIds) {
        ProcessingOrder entity = new ProcessingOrder();
        ReflectionTestUtils.setField(entity, "id", orderId);

        Set<StockOrder> targets = new LinkedHashSet<>();
        for (Long targetId : targetIds) {
            StockOrder target = new StockOrder();
            ReflectionTestUtils.setField(target, "id", targetId);
            targets.add(target);
        }
        entity.setTargetStockOrders(targets);
        return entity;
    }

    private ApiProcessingOrder request(List<Long> targetIds, List<Long> sourceIds) {
        ApiProcessingOrder api = new ApiProcessingOrder();

        List<ApiStockOrder> targets = new ArrayList<>();
        for (Long targetId : targetIds) {
            ApiStockOrder target = new ApiStockOrder();
            target.setId(targetId);
            targets.add(target);
        }
        api.setTargetStockOrders(targets);

        List<ApiTransaction> transactions = new ArrayList<>();
        for (Long sourceId : sourceIds) {
            ApiTransaction transaction = new ApiTransaction();
            ApiStockOrder source = new ApiStockOrder();
            source.setId(sourceId);
            transaction.setSourceStockOrder(source);
            transactions.add(transaction);
        }
        api.setInputTransactions(transactions);

        return api;
    }

    @Test
    @DisplayName("editar una transferencia: las salidas guardadas se aceptan aunque su ID no sea el de la orden de origen")
    void editingTransfer_acceptsExistingTargetsWithDifferentIds() throws Exception {
        ProcessingOrder entity = savedOrderWithTargets(316L, 7077L, 7078L, 7079L);
        ApiProcessingOrder api = request(List.of(7077L, 7078L, 7079L), List.of(6437L, 6436L, 6435L));

        assertDoesNotThrow(() -> validate(entity, api),
                "una transferencia guardada tiene que poder editarse");
    }

    @Test
    @DisplayName("crear una transferencia: las salidas sin ID se aceptan")
    void creatingTransfer_acceptsTargetsWithoutId() throws Exception {
        ProcessingOrder entity = new ProcessingOrder();
        ApiProcessingOrder api = request(new ArrayList<>(), List.of(6437L));
        ApiStockOrder newTarget = new ApiStockOrder();
        api.getTargetStockOrders().add(newTarget);

        assertDoesNotThrow(() -> validate(entity, api));
    }

    @Test
    @DisplayName("una salida que no pertenece a la orden se rechaza")
    void editingTransfer_rejectsForeignTargetStockOrder() {
        ProcessingOrder entity = savedOrderWithTargets(316L, 7077L);
        ApiProcessingOrder api = request(List.of(9999L), List.of(6437L));

        ApiException exception = assertThrows(ApiException.class, () -> validate(entity, api));
        assertTrue(exception.getMessage().contains("does not belong to this Processing order"),
                "el mensaje debe senalar que la salida es ajena: " + exception.getMessage());
    }

    @Test
    @DisplayName("el numero de entradas y de salidas tiene que coincidir")
    void transfer_rejectsSizeMismatch() {
        ProcessingOrder entity = savedOrderWithTargets(316L, 7077L);
        ApiProcessingOrder api = request(List.of(7077L), List.of(6437L, 6436L));

        ApiException exception = assertThrows(ApiException.class, () -> validate(entity, api));
        assertTrue(exception.getMessage().contains("has to match the size of 'targetStockOrders'"),
                exception.getMessage());
    }

    @Test
    @DisplayName("cada transaccion de entrada tiene que traer su StockOrder de origen")
    void transfer_rejectsTransactionWithoutSource() {
        ProcessingOrder entity = savedOrderWithTargets(316L, 7077L);
        ApiProcessingOrder api = request(List.of(7077L), new ArrayList<>());
        api.getInputTransactions().add(new ApiTransaction());

        ApiException exception = assertThrows(ApiException.class, () -> validate(entity, api));
        assertTrue(exception.getMessage().contains("must contain a valid source StockOrder"),
                exception.getMessage());
    }
}
