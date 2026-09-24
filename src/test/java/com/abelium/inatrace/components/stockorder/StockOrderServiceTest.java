package com.abelium.inatrace.components.stockorder;

import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.codebook.processing_evidence_type.ProcessingEvidenceTypeService;
import com.abelium.inatrace.components.codebook.processingevidencefield.ProcessingEvidenceFieldService;
import com.abelium.inatrace.components.codebook.semiproduct.SemiProductService;
import com.abelium.inatrace.components.company.CompanyQueries;
import com.abelium.inatrace.components.currencies.CurrencyService;
import com.abelium.inatrace.components.facility.FacilityService;
import com.abelium.inatrace.components.product.FinalProductService;
import com.abelium.inatrace.components.stockorder.api.ApiHistoryTimelineItem;
import com.abelium.inatrace.components.stockorder.api.ApiStockOrder;
import com.abelium.inatrace.components.stockorder.api.ApiStockOrderHistory;
import com.abelium.inatrace.components.stockorder.api.ApiStockOrderHistoryTimelineItem;
import com.abelium.inatrace.components.codebook.measure_unit_type.api.ApiMeasureUnitType;
import com.abelium.inatrace.components.payment.api.ApiPayment;
import com.abelium.inatrace.db.entities.payment.Payment;
import com.abelium.inatrace.db.entities.payment.PaymentPurposeType;
import com.abelium.inatrace.db.entities.processingorder.ProcessingOrder;
import com.abelium.inatrace.db.entities.stockorder.StockOrder;
import com.abelium.inatrace.db.entities.stockorder.Transaction;
import com.abelium.inatrace.db.entities.stockorder.enums.OrderType;
import com.abelium.inatrace.db.entities.stockorder.enums.PreferredWayOfPayment;
import com.abelium.inatrace.db.entities.stockorder.enums.TransactionStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockOrderServiceTest {

    @Mock
    private FacilityService facilityService;
    @Mock
    private ProcessingEvidenceFieldService procEvidenceFieldService;
    @Mock
    private ProcessingEvidenceTypeService procEvidenceTypeService;
    @Mock
    private SemiProductService semiProductService;
    @Mock
    private FinalProductService finalProductService;
    @Mock
    private CurrencyService currencyService;
    @Mock
    private CompanyQueries companyQueries;
    @Mock
    private MessageSource messageSource;
    @Mock
    private EntityManager entityManager;
    @Mock
    private TypedQuery<Payment> paymentTypedQuery;

    private StockOrderService stockOrderService;
    private Method calculateNetQuantityMethod;
    private Method calculateBalanceForPurchaseOrderMethod;
    private Method calculateProducerPaymentsMethod;
    private Method calculateFarmerPaymentsMethod;
    private Method calculateRepetitionsMethod;
    private Method calculateFulfilledQuantityMethod;
    private Method calculateUsedQuantityMethod;

    @BeforeEach
    void setUp() throws Exception {
        stockOrderService = new StockOrderService(
                facilityService,
                procEvidenceFieldService,
                procEvidenceTypeService,
                semiProductService,
                finalProductService,
                currencyService,
                companyQueries,
                messageSource
        );
        ReflectionTestUtils.setField(stockOrderService, "em", entityManager);

        calculateNetQuantityMethod = StockOrderService.class.getDeclaredMethod("calculateNetQuantity", ApiStockOrder.class, StockOrder.class);
        calculateNetQuantityMethod.setAccessible(true);

        calculateBalanceForPurchaseOrderMethod = StockOrderService.class.getDeclaredMethod("calculateBalanceForPurchaseOrder", StockOrder.class);
        calculateBalanceForPurchaseOrderMethod.setAccessible(true);

        calculateProducerPaymentsMethod = StockOrderService.class.getDeclaredMethod("calculateProducerPayments", BigDecimal.class, BigDecimal.class);
        calculateProducerPaymentsMethod.setAccessible(true);

        calculateFarmerPaymentsMethod = StockOrderService.class.getDeclaredMethod("calculateFarmerPayments", ApiStockOrderHistory.class);
        calculateFarmerPaymentsMethod.setAccessible(true);

        calculateRepetitionsMethod = StockOrderService.class.getDeclaredMethod("calculateRepetitions", List.class);
        calculateRepetitionsMethod.setAccessible(true);

        calculateFulfilledQuantityMethod = StockOrderService.class.getDeclaredMethod("calculateFulfilledQuantity", List.class, Long.class);
        calculateFulfilledQuantityMethod.setAccessible(true);

        calculateUsedQuantityMethod = StockOrderService.class.getDeclaredMethod("calculateUsedQuantity", List.class, Long.class);
        calculateUsedQuantityMethod.setAccessible(true);
    }

    // -------------------------------------------------------------------------
    // Net Quantity & Moisture Calculations
    // -------------------------------------------------------------------------

    @Test
    void testCalculateNetQuantity_withMoisture() throws Exception {
        ApiStockOrder api = new ApiStockOrder();
        api.setTotalGrossQuantity(new BigDecimal("1000.00"));
        api.setTare(new BigDecimal("50.00"));
        api.setDamagedWeightDeduction(new BigDecimal("20.00"));
        api.setMoisturePercentage(new BigDecimal("10.00"));

        StockOrder entity = new StockOrder();

        calculateNetQuantityMethod.invoke(stockOrderService, api, entity);

        // grossTareDamaged = 1000 - 50 - 20 = 930
        // moistureDeduction = 930 * 10 / 100 = 93
        // expected net = 930 - 93 = 837

        assertEquals(new BigDecimal("93.00"), entity.getMoistureWeightDeduction().setScale(2, RoundingMode.HALF_UP));
        assertEquals(new BigDecimal("837.00"), entity.getNetQuantity().setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    void testCalculateNetQuantity_noMoisture() throws Exception {
        ApiStockOrder api = new ApiStockOrder();
        api.setTotalGrossQuantity(new BigDecimal("500.00"));
        api.setTare(new BigDecimal("10.00"));
        api.setDamagedWeightDeduction(new BigDecimal("5.00"));
        api.setMoisturePercentage(null);

        StockOrder entity = new StockOrder();

        calculateNetQuantityMethod.invoke(stockOrderService, api, entity);

        // grossTareDamaged = 500 - 10 - 5 = 485
        // expected net = 485

        assertEquals(BigDecimal.ZERO, entity.getMoistureWeightDeduction());
        assertEquals(new BigDecimal("485.00"), entity.getNetQuantity().setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    void testCalculateNetQuantity_nullsGracefully() throws Exception {
        ApiStockOrder api = new ApiStockOrder();
        StockOrder entity = new StockOrder();

        calculateNetQuantityMethod.invoke(stockOrderService, api, entity);

        assertEquals(BigDecimal.ZERO, entity.getMoistureWeightDeduction());
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), entity.getNetQuantity().setScale(2, RoundingMode.HALF_UP));
    }

    // -------------------------------------------------------------------------
    // HU-16: Core calculateQuantities Processing Logic
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("calculateQuantities sets total, fulfilled and available quantities on new PURCHASE_ORDER")
    void calculateQuantities_purchaseOrder_setsQuantitiesForNewOrder() throws Exception {
        ApiStockOrder api = new ApiStockOrder();
        api.setOrderType(OrderType.PURCHASE_ORDER);
        api.setTotalQuantity(new BigDecimal("500.00"));
        api.setFulfilledQuantity(new BigDecimal("500.00"));

        StockOrder stockOrder = new StockOrder();

        stockOrderService.calculateQuantities(api, stockOrder, null, null);

        assertEquals(new BigDecimal("500.00"), stockOrder.getTotalQuantity());
        assertEquals(new BigDecimal("500.00"), stockOrder.getFulfilledQuantity());
        assertEquals(new BigDecimal("500.00"), stockOrder.getAvailableQuantity());
    }

    @Test
    @DisplayName("calculateQuantities sets quantities from API on new GENERAL_ORDER")
    void calculateQuantities_generalOrder_setsQuantitiesForNewOrder() throws Exception {
        ApiStockOrder api = new ApiStockOrder();
        api.setOrderType(OrderType.GENERAL_ORDER);
        api.setTotalQuantity(new BigDecimal("1000.00"));
        api.setFulfilledQuantity(new BigDecimal("400.00"));
        api.setAvailableQuantity(new BigDecimal("350.00"));

        StockOrder stockOrder = new StockOrder();

        stockOrderService.calculateQuantities(api, stockOrder, null, null);

        assertEquals(new BigDecimal("1000.00"), stockOrder.getTotalQuantity());
        assertEquals(new BigDecimal("400.00"), stockOrder.getFulfilledQuantity());
        assertEquals(new BigDecimal("350.00"), stockOrder.getAvailableQuantity());
    }

    @Test
    @DisplayName("calculateQuantities validates mandatory parameters")
    void calculateQuantities_nullValidations() {
        ApiStockOrder api = new ApiStockOrder();

        ApiException ex1 = assertThrows(ApiException.class, () ->
                stockOrderService.calculateQuantities(api, new StockOrder(), null, null));
        assertTrue(ex1.getMessage().contains("OrderType needs to be provided!"));

        api.setOrderType(OrderType.PURCHASE_ORDER);
        ApiException ex2 = assertThrows(ApiException.class, () ->
                stockOrderService.calculateQuantities(api, new StockOrder(), null, null));
        assertTrue(ex2.getMessage().contains("Total quantity cannot be null!"));

        api.setTotalQuantity(new BigDecimal("100.00"));
        ApiException ex3 = assertThrows(ApiException.class, () ->
                stockOrderService.calculateQuantities(api, new StockOrder(), null, null));
        assertTrue(ex3.getMessage().contains("Fulfilled quantity cannot be null!"));
    }

    @Test
    @DisplayName("calculateQuantities on existing entity with processing order deducts used transaction quantity")
    void calculateQuantities_existingOrderWithProcessingOrderTransactions() throws Exception {
        StockOrder stockOrder = new StockOrder();
        ReflectionTestUtils.setField(stockOrder, "id", 10L);
        stockOrder.setOrderType(OrderType.PROCESSING_ORDER);
        stockOrder.setFulfilledQuantity(new BigDecimal("500.00"));
        stockOrder.setAvailableQuantity(new BigDecimal("500.00"));

        Transaction tx = new Transaction();
        ReflectionTestUtils.setField(tx, "id", 101L);
        tx.setStatus(TransactionStatus.EXECUTED);
        tx.setSourceStockOrder(stockOrder);
        tx.setOutputQuantity(new BigDecimal("120.00"));
        tx.setInputQuantity(new BigDecimal("120.00"));

        ProcessingOrder processingOrder = new ProcessingOrder();
        com.abelium.inatrace.db.entities.processingaction.ProcessingAction action = new com.abelium.inatrace.db.entities.processingaction.ProcessingAction();
        action.setType(com.abelium.inatrace.types.ProcessingActionType.TRANSFER);
        processingOrder.setProcessingAction(action);
        processingOrder.setInputTransactions(Collections.singleton(tx));

        ApiStockOrder api = new ApiStockOrder();
        api.setOrderType(OrderType.PROCESSING_ORDER);
        api.setTotalQuantity(new BigDecimal("500.00"));
        api.setFulfilledQuantity(new BigDecimal("500.00"));

        stockOrderService.calculateQuantities(api, stockOrder, processingOrder, null);

        // Available quantity = fulfilledQuantity (500) - lastUsedQuantity (0) - usedQuantity (120) = 380
        assertEquals(new BigDecimal("380.00"), stockOrder.getAvailableQuantity());
    }

    // -------------------------------------------------------------------------
    // HU-16: Core calculateBalanceForPurchaseOrder Logic
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("calculateBalanceForPurchaseOrder returns null for non-purchase orders or null cost")
    void calculateBalanceForPurchaseOrder_returnsNullForInvalidOrder() throws Exception {
        StockOrder nonPurchase = new StockOrder();
        nonPurchase.setOrderType(OrderType.PROCESSING_ORDER);
        nonPurchase.setCost(new BigDecimal("1000.00"));

        BigDecimal b1 = (BigDecimal) calculateBalanceForPurchaseOrderMethod.invoke(stockOrderService, nonPurchase);
        assertNull(b1);

        StockOrder noCost = new StockOrder();
        noCost.setOrderType(OrderType.PURCHASE_ORDER);
        noCost.setCost(null);

        BigDecimal b2 = (BigDecimal) calculateBalanceForPurchaseOrderMethod.invoke(stockOrderService, noCost);
        assertNull(b2);
    }

    @Test
    @DisplayName("calculateBalanceForPurchaseOrder deducts FIRST_INSTALLMENT and collector amounts")
    void calculateBalanceForPurchaseOrder_withFirstInstallmentAndCollector() throws Exception {
        StockOrder stockOrder = new StockOrder();
        ReflectionTestUtils.setField(stockOrder, "id", 55L);
        stockOrder.setOrderType(OrderType.PURCHASE_ORDER);
        stockOrder.setCost(new BigDecimal("1000.00"));

        Payment payment = new Payment();
        payment.setPaymentPurposeType(PaymentPurposeType.FIRST_INSTALLMENT);
        payment.setAmount(new BigDecimal("300.00"));
        payment.setPreferredWayOfPayment(PreferredWayOfPayment.BANK_TRANSFER);
        payment.setAmountPaidToTheCollector(new BigDecimal("50.00"));

        when(entityManager.createNamedQuery("Payment.listPaymentsByPurchaseId", Payment.class)).thenReturn(paymentTypedQuery);
        when(paymentTypedQuery.setParameter("purchaseId", 55L)).thenReturn(paymentTypedQuery);
        when(paymentTypedQuery.getResultList()).thenReturn(Collections.singletonList(payment));

        BigDecimal balance = (BigDecimal) calculateBalanceForPurchaseOrderMethod.invoke(stockOrderService, stockOrder);

        // Cost (1000) - amount (300) - collector (50) = 650.00
        assertEquals(new BigDecimal("650.00"), balance);
    }

    @Test
    @DisplayName("calculateBalanceForPurchaseOrder skips collector deduction when CASH_VIA_COLLECTOR")
    void calculateBalanceForPurchaseOrder_withCashViaCollector() throws Exception {
        StockOrder stockOrder = new StockOrder();
        ReflectionTestUtils.setField(stockOrder, "id", 56L);
        stockOrder.setOrderType(OrderType.PURCHASE_ORDER);
        stockOrder.setCost(new BigDecimal("1000.00"));

        Payment payment = new Payment();
        payment.setPaymentPurposeType(PaymentPurposeType.FIRST_INSTALLMENT);
        payment.setAmount(new BigDecimal("300.00"));
        payment.setPreferredWayOfPayment(PreferredWayOfPayment.CASH_VIA_COLLECTOR);
        payment.setAmountPaidToTheCollector(new BigDecimal("50.00"));

        when(entityManager.createNamedQuery("Payment.listPaymentsByPurchaseId", Payment.class)).thenReturn(paymentTypedQuery);
        when(paymentTypedQuery.setParameter("purchaseId", 56L)).thenReturn(paymentTypedQuery);
        when(paymentTypedQuery.getResultList()).thenReturn(Collections.singletonList(payment));

        BigDecimal balance = (BigDecimal) calculateBalanceForPurchaseOrderMethod.invoke(stockOrderService, stockOrder);

        // When CASH_VIA_COLLECTOR: balance = cost (1000) - amount (300) = 700.00 (collector amount not deducted)
        assertEquals(new BigDecimal("700.00"), balance);
    }

    // -------------------------------------------------------------------------
    // HU-16: Core Producer & Transaction Quantities Calculations
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("calculateProducerPayments calculates price per kg correctly and handles edge cases")
    void calculateProducerPayments_calculations() throws Exception {
        BigDecimal paidEur = new BigDecimal("1500.00");
        BigDecimal coffeeKg = new BigDecimal("500.00");

        BigDecimal pricePerKg = (BigDecimal) calculateProducerPaymentsMethod.invoke(stockOrderService, paidEur, coffeeKg);
        assertEquals(new BigDecimal("3.000"), pricePerKg);

        // Zero checks: returns null if either paidEur or coffeeKg is BigDecimal.ZERO
        assertNull(calculateProducerPaymentsMethod.invoke(stockOrderService, BigDecimal.ZERO, coffeeKg));
        assertNull(calculateProducerPaymentsMethod.invoke(stockOrderService, paidEur, BigDecimal.ZERO));
    }

    @Test
    @DisplayName("calculateFulfilledQuantity and calculateUsedQuantity calculate correctly and ignore CANCELED")
    void calculateFulfilledQuantity_and_calculateUsedQuantity_logic() throws Exception {
        StockOrder so = new StockOrder();
        ReflectionTestUtils.setField(so, "id", 77L);

        Transaction activeTx = new Transaction();
        activeTx.setStatus(TransactionStatus.EXECUTED);
        activeTx.setSourceStockOrder(so);
        activeTx.setInputQuantity(new BigDecimal("200.00"));
        activeTx.setOutputQuantity(new BigDecimal("180.00"));

        Transaction canceledTx = new Transaction();
        canceledTx.setStatus(TransactionStatus.CANCELED);
        canceledTx.setSourceStockOrder(so);
        canceledTx.setInputQuantity(new BigDecimal("50.00"));
        canceledTx.setOutputQuantity(new BigDecimal("45.00"));

        List<Transaction> txList = Arrays.asList(activeTx, canceledTx);

        // Fulfilled quantity: sums inputQuantity of non-canceled tx
        BigDecimal fulfilled = (BigDecimal) calculateFulfilledQuantityMethod.invoke(stockOrderService, txList, 77L);
        assertEquals(new BigDecimal("200.00"), fulfilled);

        // Used quantity: sums outputQuantity of non-canceled tx
        BigDecimal used = (BigDecimal) calculateUsedQuantityMethod.invoke(stockOrderService, txList, 77L);
        assertEquals(new BigDecimal("180.00"), used);

        // Empty list returns ZERO
        assertEquals(BigDecimal.ZERO, calculateFulfilledQuantityMethod.invoke(stockOrderService, Collections.emptyList(), 77L));
        assertEquals(BigDecimal.ZERO, calculateUsedQuantityMethod.invoke(stockOrderService, Collections.emptyList(), 77L));
    }

    // -------------------------------------------------------------------------
    // Variety & Certification Classifiers
    // -------------------------------------------------------------------------

    @Test
    void isCcn51Variety_reconoceTextoYNumero() {
        assertTrue(StockOrderService.isCcn51Variety("CCN51"));
        assertTrue(StockOrderService.isCcn51Variety("ccn51"));
        assertTrue(StockOrderService.isCcn51Variety("2"));
        assertFalse(StockOrderService.isCcn51Variety("1"));
        assertFalse(StockOrderService.isCcn51Variety("NACIONAL"));
        assertFalse(StockOrderService.isCcn51Variety(null));
    }

    @Test
    void isNonOrganicCertificationName_convencionalYTransicion() {
        assertTrue(StockOrderService.isNonOrganicCertificationName("Convencional Fairtrade"));
        assertTrue(StockOrderService.isNonOrganicCertificationName("Transición / Fairtrade / SPP"));
        assertTrue(StockOrderService.isNonOrganicCertificationName("Transition / Fairtrade / SPP"));
        assertFalse(StockOrderService.isNonOrganicCertificationName("Organico UE/NOP/Fairtrade/SPP"));
        assertFalse(StockOrderService.isNonOrganicCertificationName(null));
    }

    // -------------------------------------------------------------------------
    // Farmer Payments, Repetitions & Quantities Edge Cases
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("calculateFarmerPayments returns weighted price per kg when all farmers are fully paid")
    void calculateFarmerPayments_farmersFullyPaid_calculatesWeightedAverage() throws Exception {
        ApiStockOrder order = new ApiStockOrder();
        order.setBalance(BigDecimal.ZERO);
        order.setFulfilledQuantity(new BigDecimal("100.00"));
        ApiMeasureUnitType unitType = new ApiMeasureUnitType();
        unitType.setWeight(new BigDecimal("1.00"));
        order.setMeasureUnitType(unitType);

        ApiPayment payment = new ApiPayment();
        payment.setAmount(new BigDecimal("300.00"));
        payment.setCurrency("USD");
        payment.setFormalCreationTime(LocalDate.of(2026, 9, 24));
        order.setPayments(Collections.singletonList(payment));

        when(currencyService.convertAtDate(eq("USD"), eq("USD"), eq(new BigDecimal("300.00")), any(Date.class)))
                .thenReturn(new BigDecimal("300.00"));

        ApiStockOrderHistoryTimelineItem item = new ApiStockOrderHistoryTimelineItem();
        item.setPurchaseOrders(Collections.singletonList(order));

        ApiStockOrderHistory history = new ApiStockOrderHistory();
        history.setTimelineItems(Collections.singletonList(item));

        BigDecimal result = (BigDecimal) calculateFarmerPaymentsMethod.invoke(stockOrderService, history);
        // 300 USD / (100 kg * 1.00) = 3.000
        assertEquals(new BigDecimal("3.000"), result);
    }

    @Test
    @DisplayName("calculateFarmerPayments returns null when any farmer has outstanding balance")
    void calculateFarmerPayments_farmerNotFullyPaid_returnsNull() throws Exception {
        ApiStockOrder order = new ApiStockOrder();
        order.setBalance(new BigDecimal("50.00")); // outstanding balance > 0
        order.setFulfilledQuantity(new BigDecimal("100.00"));

        ApiStockOrderHistoryTimelineItem item = new ApiStockOrderHistoryTimelineItem();
        item.setPurchaseOrders(Collections.singletonList(order));

        ApiStockOrderHistory history = new ApiStockOrderHistory();
        history.setTimelineItems(Collections.singletonList(item));

        BigDecimal result = (BigDecimal) calculateFarmerPaymentsMethod.invoke(stockOrderService, history);
        assertNull(result);
    }

    @Test
    @DisplayName("calculateRepetitions counts consecutive repeated event names correctly")
    void calculateRepetitions_countsConsecutiveEvents() throws Exception {
        ApiHistoryTimelineItem ev1 = new ApiHistoryTimelineItem();
        ev1.setName("HARVEST");
        ApiHistoryTimelineItem ev2 = new ApiHistoryTimelineItem();
        ev2.setName("FERMENTATION");
        ApiHistoryTimelineItem ev3 = new ApiHistoryTimelineItem();
        ev3.setName("FERMENTATION");
        ApiHistoryTimelineItem ev4 = new ApiHistoryTimelineItem();
        ev4.setName("FERMENTATION");
        ApiHistoryTimelineItem ev5 = new ApiHistoryTimelineItem();
        ev5.setName("DRYING");

        @SuppressWarnings("unchecked")
        List<Integer> repetitions = (List<Integer>) calculateRepetitionsMethod.invoke(
                stockOrderService, Arrays.asList(ev1, ev2, ev3, ev4, ev5));

        assertEquals(Arrays.asList(0, 0, 1, 2, 0), repetitions);
    }

    @Test
    @DisplayName("calculateQuantities with newInputTransactionId subtracts new transaction output quantity")
    void calculateQuantities_existingOrderWithNewInputTransactionId() throws Exception {
        StockOrder stockOrder = new StockOrder();
        ReflectionTestUtils.setField(stockOrder, "id", 20L);
        stockOrder.setOrderType(OrderType.PURCHASE_ORDER);
        stockOrder.setFulfilledQuantity(new BigDecimal("1000.00"));
        stockOrder.setAvailableQuantity(new BigDecimal("1000.00"));

        Transaction tx1 = new Transaction();
        ReflectionTestUtils.setField(tx1, "id", 501L);
        tx1.setOutputQuantity(new BigDecimal("250.00"));

        ProcessingOrder processingOrder = new ProcessingOrder();
        com.abelium.inatrace.db.entities.processingaction.ProcessingAction action = new com.abelium.inatrace.db.entities.processingaction.ProcessingAction();
        action.setType(com.abelium.inatrace.types.ProcessingActionType.TRANSFER);
        processingOrder.setProcessingAction(action);
        processingOrder.setInputTransactions(Collections.singleton(tx1));

        ApiStockOrder api = new ApiStockOrder();
        api.setOrderType(OrderType.PURCHASE_ORDER);
        api.setTotalQuantity(new BigDecimal("1000.00"));
        api.setFulfilledQuantity(new BigDecimal("1000.00"));

        stockOrderService.calculateQuantities(api, stockOrder, processingOrder, 501L);

        // Available quantity = 1000 - 250 = 750.00
        assertEquals(new BigDecimal("750.00"), stockOrder.getAvailableQuantity());
    }

    @Test
    @DisplayName("calculateQuantities recalculates available quantity when lastUsedQuantity is present and no processingOrder")
    void calculateQuantities_existingOrderWithoutProcessingOrder_recalculatesAvailable() throws Exception {
        StockOrder stockOrder = new StockOrder();
        ReflectionTestUtils.setField(stockOrder, "id", 21L);
        stockOrder.setOrderType(OrderType.PURCHASE_ORDER);
        // Fulfilled 800, available 600 -> lastUsedQuantity = 200
        stockOrder.setFulfilledQuantity(new BigDecimal("800.00"));
        stockOrder.setAvailableQuantity(new BigDecimal("600.00"));

        ApiStockOrder api = new ApiStockOrder();
        api.setOrderType(OrderType.PURCHASE_ORDER);
        api.setTotalQuantity(new BigDecimal("900.00"));
        api.setFulfilledQuantity(new BigDecimal("900.00"));

        stockOrderService.calculateQuantities(api, stockOrder, null, null);

        // Available quantity = api.fulfilledQuantity (900) - lastUsedQuantity (200) = 700.00
        assertEquals(new BigDecimal("700.00"), stockOrder.getAvailableQuantity());
    }
}
