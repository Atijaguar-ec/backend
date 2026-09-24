package com.abelium.inatrace.components.payment;

import com.abelium.inatrace.api.ApiBaseEntity;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.company.CompanyQueries;
import com.abelium.inatrace.components.company.api.ApiCompany;
import com.abelium.inatrace.components.company.api.ApiUserCustomer;
import com.abelium.inatrace.components.payment.api.ApiBulkPayment;
import com.abelium.inatrace.components.payment.api.ApiPayment;
import com.abelium.inatrace.components.stockorder.StockOrderService;
import com.abelium.inatrace.components.stockorder.api.ApiStockOrder;
import com.abelium.inatrace.components.user.UserService;
import com.abelium.inatrace.db.entities.common.User;
import com.abelium.inatrace.db.entities.common.UserCustomer;
import com.abelium.inatrace.db.entities.company.Company;
import com.abelium.inatrace.db.entities.company.CompanyUser;
import com.abelium.inatrace.db.entities.payment.*;
import com.abelium.inatrace.db.entities.stockorder.StockOrder;
import com.abelium.inatrace.db.entities.stockorder.enums.OrderType;
import com.abelium.inatrace.db.entities.stockorder.enums.PreferredWayOfPayment;
import com.abelium.inatrace.security.service.CustomUserDetails;
import com.abelium.inatrace.types.CompanyUserRole;
import com.abelium.inatrace.types.UserRole;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private CompanyQueries companyQueries;

    @Mock
    private StockOrderService stockOrderService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private EntityManager entityManager;

    private PaymentService paymentService;
    private CustomUserDetails authUser;
    private User testUser;
    private Company payingCompany;
    private CompanyUser companyUser;

    @BeforeEach
    void setUp() throws Exception {
        paymentService = spy(new PaymentService(userService, companyQueries, stockOrderService, messageSource));
        ReflectionTestUtils.setField(paymentService, "em", entityManager);

        authUser = new CustomUserDetails(1L, "admin@test.com", "Admin", "User", UserRole.SYSTEM_ADMIN);
        testUser = new User();
        ReflectionTestUtils.setField(testUser, "id", 1L);
        testUser.setEmail("admin@test.com");

        payingCompany = new Company();
        ReflectionTestUtils.setField(payingCompany, "id", 10L);
        payingCompany.setName("Test Coop");

        companyUser = new CompanyUser();
        companyUser.setUser(testUser);
        companyUser.setCompany(payingCompany);
        companyUser.setRole(CompanyUserRole.COMPANY_ADMIN);
        payingCompany.setUsers(new HashSet<>(Collections.singletonList(companyUser)));

        lenient().when(userService.fetchUserById(1L)).thenReturn(testUser);
    }

    private StockOrder createMockPurchaseOrder(Long id, BigDecimal cost, List<Payment> existingPayments) {
        StockOrder so = new StockOrder();
        ReflectionTestUtils.setField(so, "id", id);
        so.setIdentifier("SO-2026-001");
        so.setOrderType(OrderType.PURCHASE_ORDER);
        so.setCompany(payingCompany);
        so.setCost(cost);
        so.setTotalQuantity(new BigDecimal("100.00"));
        so.setPreferredWayOfPayment(PreferredWayOfPayment.BANK_TRANSFER);
        so.setPayments(existingPayments != null ? new HashSet<>(existingPayments) : new HashSet<>());

        UserCustomer producer = new UserCustomer();
        ReflectionTestUtils.setField(producer, "id", 50L);
        producer.setName("Pedro");
        producer.setSurname("Perez");
        so.setProducerUserCustomer(producer);

        return so;
    }

    // -------------------------------------------------------------------------
    // HU-16: Core Calculation & Balance on Payment Processing
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("createOrUpdatePayment on PURCHASE_ORDER correctly deducts payment from stock order balance")
    void createOrUpdatePayment_purchaseOrder_calculatesBalanceCorrectly() throws Exception {
        Payment existingPayment = new Payment();
        ReflectionTestUtils.setField(existingPayment, "id", 101L);
        existingPayment.setTotalPaid(new BigDecimal("300.00"));

        StockOrder stockOrder = createMockPurchaseOrder(200L, new BigDecimal("1000.00"), Collections.singletonList(existingPayment));
        when(entityManager.find(StockOrder.class, 200L)).thenReturn(stockOrder);

        ApiPayment apiPayment = new ApiPayment();
        ApiStockOrder apiStockOrder = new ApiStockOrder();
        apiStockOrder.setId(200L);
        apiPayment.setStockOrder(apiStockOrder);
        apiPayment.setRecipientType(RecipientType.USER_CUSTOMER);
        ApiUserCustomer apiFarmer = new ApiUserCustomer();
        apiFarmer.setId(50L);
        apiPayment.setRecipientUserCustomer(apiFarmer);
        apiPayment.setAmount(new BigDecimal("250.00"));
        apiPayment.setPaymentPurposeType(PaymentPurposeType.SECOND_INSTALLMENT);
        apiPayment.setPaymentStatus(PaymentStatus.CONFIRMED);

        ApiBaseEntity result = paymentService.createOrUpdatePayment(apiPayment, authUser, false);

        assertNotNull(result);
        verify(entityManager).persist(any(Payment.class));
        // Initial cost: 1000.00
        // Previous payments: 300.00
        // New payment: 250.00
        // Balance = 1000 - 300 - 250 = 450.00
        assertEquals(new BigDecimal("450.00"), stockOrder.getBalance());
        assertEquals(testUser, stockOrder.getUpdatedBy());
    }

    @Test
    @DisplayName("createOrUpdatePayment with priceDeterminedLater=true does not recalculate balance")
    void createOrUpdatePayment_priceDeterminedLater_doesNotRecalculateBalance() throws Exception {
        StockOrder stockOrder = createMockPurchaseOrder(201L, new BigDecimal("1000.00"), Collections.emptyList());
        stockOrder.setPriceDeterminedLater(true);
        when(entityManager.find(StockOrder.class, 201L)).thenReturn(stockOrder);

        ApiPayment apiPayment = new ApiPayment();
        ApiStockOrder apiStockOrder = new ApiStockOrder();
        apiStockOrder.setId(201L);
        apiPayment.setStockOrder(apiStockOrder);
        apiPayment.setRecipientType(RecipientType.USER_CUSTOMER);
        ApiUserCustomer apiFarmer = new ApiUserCustomer();
        apiFarmer.setId(50L);
        apiPayment.setRecipientUserCustomer(apiFarmer);
        apiPayment.setAmount(new BigDecimal("200.00"));
        apiPayment.setPaymentPurposeType(PaymentPurposeType.SECOND_INSTALLMENT);

        paymentService.createOrUpdatePayment(apiPayment, authUser, false);

        assertNull(stockOrder.getBalance());
    }

    @Test
    @DisplayName("deletePayment on PURCHASE_ORDER correctly restores balance")
    void deletePayment_purchaseOrder_recalculatesBalance() throws Exception {
        Payment p1 = new Payment();
        ReflectionTestUtils.setField(p1, "id", 101L);
        p1.setTotalPaid(new BigDecimal("300.00"));

        Payment pToDelete = new Payment();
        ReflectionTestUtils.setField(pToDelete, "id", 102L);
        pToDelete.setTotalPaid(new BigDecimal("250.00"));
        pToDelete.setPayingCompany(payingCompany);

        StockOrder stockOrder = createMockPurchaseOrder(200L, new BigDecimal("1000.00"), Arrays.asList(p1, pToDelete));
        pToDelete.setStockOrder(stockOrder);

        when(entityManager.find(Payment.class, 102L)).thenReturn(pToDelete);

        paymentService.deletePayment(102L, authUser);

        // Sum before delete: 300 + 250 = 550
        // Balance formula: cost (1000) - sumTotalPaid (550) + pToDelete (250) = 700.00
        assertEquals(new BigDecimal("700.00"), stockOrder.getBalance());
        assertEquals(testUser, stockOrder.getUpdatedBy());
    }

    // -------------------------------------------------------------------------
    // HU-16: Validation Edge Cases in Payment Creation & Updates
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("createOrUpdatePayment throws when recipientType is null")
    void createOrUpdatePayment_validation_recipientTypeRequired() {
        ApiPayment api = new ApiPayment();
        ApiException ex = assertThrows(ApiException.class, () ->
                paymentService.createOrUpdatePayment(api, authUser, false));
        assertTrue(ex.getMessage().contains("Recipient type is required"));
    }

    @Test
    @DisplayName("createOrUpdatePayment throws when orderType is neither PURCHASE_ORDER nor GENERAL_ORDER")
    void createOrUpdatePayment_validation_orderTypeInvalid() {
        StockOrder processingOrder = new StockOrder();
        ReflectionTestUtils.setField(processingOrder, "id", 300L);
        processingOrder.setOrderType(OrderType.PROCESSING_ORDER);
        processingOrder.setCompany(payingCompany);
        when(entityManager.find(StockOrder.class, 300L)).thenReturn(processingOrder);

        ApiPayment api = new ApiPayment();
        ApiStockOrder so = new ApiStockOrder();
        so.setId(300L);
        api.setStockOrder(so);
        api.setRecipientType(RecipientType.USER_CUSTOMER);

        ApiException ex = assertThrows(ApiException.class, () ->
                paymentService.createOrUpdatePayment(api, authUser, false));
        assertTrue(ex.getMessage().contains("Not a Purchase or Quote order"));
    }

    @Test
    @DisplayName("createOrUpdatePayment throws when FIRST_INSTALLMENT lacks receiptDocument (outside bulk payment)")
    void createOrUpdatePayment_validation_firstInstallmentRequiresReceiptDocument() {
        StockOrder stockOrder = createMockPurchaseOrder(200L, new BigDecimal("500.00"), Collections.emptyList());
        when(entityManager.find(StockOrder.class, 200L)).thenReturn(stockOrder);

        ApiPayment api = new ApiPayment();
        ApiStockOrder so = new ApiStockOrder();
        so.setId(200L);
        api.setStockOrder(so);
        api.setRecipientType(RecipientType.USER_CUSTOMER);
        api.setPaymentPurposeType(PaymentPurposeType.FIRST_INSTALLMENT);
        api.setReceiptDocument(null);

        ApiException ex = assertThrows(ApiException.class, () ->
                paymentService.createOrUpdatePayment(api, authUser, false));
        assertTrue(ex.getMessage().contains("Receipt document has to be provided"));
    }

    @Test
    @DisplayName("createOrUpdatePayment allows FIRST_INSTALLMENT without receiptDocument if isPartOfBulkPayment")
    void createOrUpdatePayment_firstInstallmentAllowedWithoutDocument_whenPartOfBulkPayment() throws Exception {
        StockOrder stockOrder = createMockPurchaseOrder(200L, new BigDecimal("500.00"), Collections.emptyList());
        when(entityManager.find(StockOrder.class, 200L)).thenReturn(stockOrder);

        ApiPayment api = new ApiPayment();
        ApiStockOrder so = new ApiStockOrder();
        so.setId(200L);
        api.setStockOrder(so);
        api.setRecipientType(RecipientType.USER_CUSTOMER);
        ApiUserCustomer farmer = new ApiUserCustomer();
        farmer.setId(50L);
        api.setRecipientUserCustomer(farmer);
        api.setPaymentPurposeType(PaymentPurposeType.FIRST_INSTALLMENT);
        api.setReceiptDocument(null);
        api.setAmount(new BigDecimal("100.00"));

        ApiBaseEntity result = paymentService.createOrUpdatePayment(api, authUser, true);
        assertNotNull(result);
    }

    @Test
    @DisplayName("createOrUpdatePayment throws when amount is negative")
    void createOrUpdatePayment_validation_negativeAmount() {
        StockOrder stockOrder = createMockPurchaseOrder(200L, new BigDecimal("500.00"), Collections.emptyList());
        when(entityManager.find(StockOrder.class, 200L)).thenReturn(stockOrder);

        ApiPayment api = new ApiPayment();
        ApiStockOrder so = new ApiStockOrder();
        so.setId(200L);
        api.setStockOrder(so);
        api.setRecipientType(RecipientType.USER_CUSTOMER);
        api.setPaymentPurposeType(PaymentPurposeType.SECOND_INSTALLMENT);
        api.setAmount(new BigDecimal("-10.00"));

        ApiException ex = assertThrows(ApiException.class, () ->
                paymentService.createOrUpdatePayment(api, authUser, false));
        assertTrue(ex.getMessage().contains("Total amount paid cannot be negative"));
    }

    @Test
    @DisplayName("createOrUpdatePayment throws when recipient COMPANY lacks company details")
    void createOrUpdatePayment_validation_companyRecipientRequiresCompany() {
        StockOrder stockOrder = createMockPurchaseOrder(200L, new BigDecimal("500.00"), Collections.emptyList());
        when(entityManager.find(StockOrder.class, 200L)).thenReturn(stockOrder);

        ApiPayment api = new ApiPayment();
        ApiStockOrder so = new ApiStockOrder();
        so.setId(200L);
        api.setStockOrder(so);
        api.setRecipientType(RecipientType.COMPANY);
        api.setPaymentPurposeType(PaymentPurposeType.SECOND_INSTALLMENT);
        api.setAmount(new BigDecimal("100.00"));
        api.setRecipientCompany(null);

        ApiException ex = assertThrows(ApiException.class, () ->
                paymentService.createOrUpdatePayment(api, authUser, false));
        assertTrue(ex.getMessage().contains("Recipient company is required when type is COMPANY"));
    }

    @Test
    @DisplayName("createOrUpdatePayment throws when recipient USER_CUSTOMER lacks farmer details")
    void createOrUpdatePayment_validation_userCustomerRecipientRequiresUserCustomer() {
        StockOrder stockOrder = createMockPurchaseOrder(200L, new BigDecimal("500.00"), Collections.emptyList());
        when(entityManager.find(StockOrder.class, 200L)).thenReturn(stockOrder);

        ApiPayment api = new ApiPayment();
        ApiStockOrder so = new ApiStockOrder();
        so.setId(200L);
        api.setStockOrder(so);
        api.setRecipientType(RecipientType.USER_CUSTOMER);
        api.setPaymentPurposeType(PaymentPurposeType.SECOND_INSTALLMENT);
        api.setAmount(new BigDecimal("100.00"));
        api.setRecipientUserCustomer(null);

        ApiException ex = assertThrows(ApiException.class, () ->
                paymentService.createOrUpdatePayment(api, authUser, false));
        assertTrue(ex.getMessage().contains("Recipient user customer is required when type is USER_CUSTOMER"));
    }

    @Test
    @DisplayName("createOrUpdatePayment updates existing payment status to CONFIRMED with metadata")
    void createOrUpdatePayment_existingPayment_updatesStatusToConfirmed() throws Exception {
        Payment existingPayment = new Payment();
        ReflectionTestUtils.setField(existingPayment, "id", 105L);
        existingPayment.setPayingCompany(payingCompany);
        existingPayment.setPaymentStatus(PaymentStatus.UNCONFIRMED);
        when(entityManager.find(Payment.class, 105L)).thenReturn(existingPayment);

        ApiPayment apiPayment = new ApiPayment();
        apiPayment.setId(105L);
        apiPayment.setPaymentStatus(PaymentStatus.CONFIRMED);

        ApiBaseEntity result = paymentService.createOrUpdatePayment(apiPayment, authUser, false);

        assertNotNull(result);
        assertEquals(PaymentStatus.CONFIRMED, existingPayment.getPaymentStatus());
        assertNotNull(existingPayment.getPaymentConfirmedAtTime());
        assertEquals(testUser, existingPayment.getPaymentConfirmedByUser());
        assertEquals(payingCompany, existingPayment.getPaymentConfirmedByCompany());
    }

    // -------------------------------------------------------------------------
    // HU-16: Bulk Payment Processing & Validations
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("createBulkPayment throws when id is provided")
    void createBulkPayment_validation_cannotUpdate() {
        ApiBulkPayment bulk = new ApiBulkPayment();
        bulk.setId(99L);
        ApiException ex = assertThrows(ApiException.class, () ->
                paymentService.createBulkPayment(bulk, authUser));
        assertTrue(ex.getMessage().contains("Bulk payment cannot be updated!"));
    }

    @Test
    @DisplayName("createBulkPayment throws when payingCompany is missing")
    void createBulkPayment_validation_missingPayingCompany() {
        ApiBulkPayment bulk = new ApiBulkPayment();
        bulk.setPayingCompany(null);
        ApiException ex = assertThrows(ApiException.class, () ->
                paymentService.createBulkPayment(bulk, authUser));
        assertTrue(ex.getMessage().contains("Paying company ID has to be provided!"));
    }

    @Test
    @DisplayName("createBulkPayment throws when payments list is empty")
    void createBulkPayment_validation_missingPayments() {
        ApiBulkPayment bulk = new ApiBulkPayment();
        ApiCompany company = new ApiCompany();
        company.setId(10L);
        bulk.setPayingCompany(company);
        bulk.setPayments(Collections.emptyList());

        ApiException ex = assertThrows(ApiException.class, () ->
                paymentService.createBulkPayment(bulk, authUser));
        assertTrue(ex.getMessage().contains("At least one payment needs to be provided."));
    }

    @Test
    @DisplayName("createBulkPayment throws when paymentDescription is null")
    void createBulkPayment_validation_missingDescription() {
        ApiBulkPayment bulk = new ApiBulkPayment();
        ApiCompany company = new ApiCompany();
        company.setId(10L);
        bulk.setPayingCompany(company);
        bulk.setPayments(Collections.singletonList(new ApiPayment()));
        bulk.setPaymentDescription(null);

        ApiException ex = assertThrows(ApiException.class, () ->
                paymentService.createBulkPayment(bulk, authUser));
        assertTrue(ex.getMessage().contains("Payment description needs to be provided."));
    }

    @Test
    @DisplayName("createBulkPayment throws when additionalCost given without description")
    void createBulkPayment_validation_additionalCostWithoutDescription() {
        ApiBulkPayment bulk = new ApiBulkPayment();
        ApiCompany company = new ApiCompany();
        company.setId(10L);
        bulk.setPayingCompany(company);
        bulk.setPayments(Collections.singletonList(new ApiPayment()));
        bulk.setPaymentDescription("Batch Payment Sept 2026");
        bulk.setAdditionalCost(new BigDecimal("50.00"));
        bulk.setAdditionalCostDescription(null);

        ApiException ex = assertThrows(ApiException.class, () ->
                paymentService.createBulkPayment(bulk, authUser));
        assertTrue(ex.getMessage().contains("Additional cost description needs to be provided."));
    }

    @Test
    @DisplayName("createBulkPayment throws when receiptNumber is null")
    void createBulkPayment_validation_missingReceiptNumber() {
        ApiBulkPayment bulk = new ApiBulkPayment();
        ApiCompany company = new ApiCompany();
        company.setId(10L);
        bulk.setPayingCompany(company);
        bulk.setPayments(Collections.singletonList(new ApiPayment()));
        bulk.setPaymentDescription("Batch Payment Sept 2026");
        bulk.setReceiptNumber(null);

        ApiException ex = assertThrows(ApiException.class, () ->
                paymentService.createBulkPayment(bulk, authUser));
        assertTrue(ex.getMessage().contains("Recipient number is required."));
    }
}
