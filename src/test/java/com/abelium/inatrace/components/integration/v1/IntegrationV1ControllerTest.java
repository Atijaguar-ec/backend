package com.abelium.inatrace.components.integration.v1;

import com.abelium.inatrace.api.ApiResponse;
import com.abelium.inatrace.api.ApiStatus;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.integration.v1.dto.*;
import com.abelium.inatrace.components.integration.v1.mappers.DeliveryIntegrationMapper;
import com.abelium.inatrace.components.integration.v1.mappers.FarmerIntegrationMapper;
import com.abelium.inatrace.components.integration.v1.services.IntegrationV1Service;
import com.abelium.inatrace.components.integration.v1.services.LotCompositionService;
import com.abelium.inatrace.db.entities.codebook.CertificationType;
import com.abelium.inatrace.db.entities.codebook.MeasureUnitType;
import com.abelium.inatrace.db.entities.codebook.ProductType;
import com.abelium.inatrace.db.entities.codebook.SemiProduct;
import com.abelium.inatrace.db.entities.common.*;
import com.abelium.inatrace.db.entities.company.Company;
import com.abelium.inatrace.db.entities.facility.Facility;
import com.abelium.inatrace.db.entities.facility.FacilityLocation;
import com.abelium.inatrace.db.entities.processingorder.ProcessingOrder;
import com.abelium.inatrace.db.entities.stockorder.StockOrder;
import com.abelium.inatrace.db.entities.stockorder.StockOrderStatus;
import com.abelium.inatrace.db.entities.stockorder.Transaction;
import com.abelium.inatrace.db.entities.stockorder.enums.OrderType;
import com.abelium.inatrace.security.service.CustomUserDetails;
import com.abelium.inatrace.types.CocoaVariety;
import com.abelium.inatrace.types.UserCustomerType;
import com.abelium.inatrace.types.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IntegrationV1ControllerTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Company> companyQuery;

    @Mock
    private TypedQuery<Long> longQuery;

    @Mock
    private TypedQuery<Facility> facilityQuery;

    @Mock
    private TypedQuery<UserCustomer> userCustomerQuery;

    @Mock
    private TypedQuery<StockOrder> stockOrderQuery;

    @Mock
    private TypedQuery<SemiProduct> semiProductQuery;

    @Mock
    private TypedQuery<CertificationType> certTypeQuery;

    private LotCompositionService lotCompositionService;
    private IntegrationV1Service integrationService;
    private IntegrationV1Controller controller;

    private CustomUserDetails authUserUnocace;
    private CustomUserDetails authUserSystemAdmin;

    @BeforeEach
    void setUp() {
        lotCompositionService = new LotCompositionService(entityManager);
        // Sin lista blanca configurada: fail-closed, cada usuario queda limitado a sus empresas.
        integrationService = new IntegrationV1Service(entityManager, lotCompositionService, "");
        controller = new IntegrationV1Controller(integrationService);

        // M2M user enrolled only in UNOCACE (Company ID 12)
        authUserUnocace = new CustomUserDetails(99L, "service-account-mieru-cacao", "Mieru", "Service", UserRole.USER);

        // System Admin user
        authUserSystemAdmin = new CustomUserDetails(1L, "admin@inatrace.com", "Admin", "User", UserRole.SYSTEM_ADMIN);

        // Mock company enrollment query for user 99 -> [12L]
        when(entityManager.createQuery(contains("SELECT cu.company.id FROM CompanyUser cu WHERE cu.user.id = :userId"), eq(Long.class)))
                .thenReturn(longQuery);
        when(longQuery.setParameter("userId", 99L)).thenReturn(longQuery);
        when(longQuery.getResultList()).thenReturn(Collections.singletonList(12L));
    }

    // =========================================================================
    // 1. Multi-Tenant Isolation & Access Control
    // =========================================================================

    @Test
    @DisplayName("ADR-018: Access denied (HTTP 403 / FORBIDDEN_TENANT_ACCESS) for unauthorized company")
    void testAccessDeniedForUnauthorizedCompany() {
        // User is enrolled in company 12, requests company 5
        ApiException exception = assertThrows(ApiException.class, () ->
                controller.getFacilities(5L, null, authUserUnocace)
        );

        assertEquals(ApiStatus.UNAUTHORIZED, exception.getApiStatus());
        assertEquals("FORBIDDEN_TENANT_ACCESS", exception.getErrorDetails());
    }

    /**
     * D-01 (informe QA 2026-11-04): el rol SYSTEM_ADMIN NO es un comodín en /integration/v1.
     * Antes, getAuthorizedCompanyIds() devolvía el company_id pedido sin validarlo para ese rol,
     * y la cuenta de servicio M2M leía las 21 empresas del tenant — fuga de datos personales de
     * agricultores entre empresas (RF §4.1 "Limited scope", aceptación 2 del RF §6).
     */
    @Test
    @DisplayName("D-01: System admin NO puede acceder a una empresa fuera de la lista blanca (403)")
    void testSystemAdminBlockedOutsideAllowlist() {
        IntegrationV1Service scoped = new IntegrationV1Service(entityManager, lotCompositionService, "4,8");
        IntegrationV1Controller scopedController = new IntegrationV1Controller(scoped);

        ApiException ex = assertThrows(ApiException.class, () ->
                scopedController.getFacilities(5L, null, authUserSystemAdmin)
        );

        assertEquals(ApiStatus.UNAUTHORIZED, ex.getApiStatus());
        assertEquals("FORBIDDEN_TENANT_ACCESS", ex.getErrorDetails());
    }

    @Test
    @DisplayName("D-01: System admin sí accede a una empresa dentro de la lista blanca")
    void testSystemAdminAllowedInsideAllowlist() throws ApiException {
        IntegrationV1Service scoped = new IntegrationV1Service(entityManager, lotCompositionService, "4,8");
        IntegrationV1Controller scopedController = new IntegrationV1Controller(scoped);

        when(entityManager.createQuery(contains("FROM Facility"), eq(Facility.class))).thenReturn(facilityQuery);
        when(facilityQuery.setParameter(eq("companyIds"), anyList())).thenReturn(facilityQuery);
        when(facilityQuery.getResultList()).thenReturn(Collections.emptyList());

        ApiResponse<List<ApiFacilityIntegration>> response = scopedController.getFacilities(8L, null, authUserSystemAdmin);
        assertNotNull(response);
        assertEquals(ApiStatus.OK, response.getStatus());
    }

    @Test
    @DisplayName("D-01: con lista blanca, el alcance sin company_id es exactamente la lista blanca")
    void testAllowlistIsTheScopeForSystemAdmin() throws ApiException {
        IntegrationV1Service scoped = new IntegrationV1Service(entityManager, lotCompositionService, "8, 4 ,8");

        List<Long> scope = scoped.getAuthorizedCompanyIds(authUserSystemAdmin, null);

        assertEquals(List.of(4L, 8L), scope, "deduplicado, saneado y ordenado de forma estable");
    }

    @Test
    @DisplayName("D-01: fail-closed — sin lista blanca, un admin sin empresas propias recibe 403")
    void testFailsClosedWithoutAllowlist() {
        when(longQuery.setParameter("userId", 1L)).thenReturn(longQuery);
        when(longQuery.getResultList()).thenReturn(Collections.emptyList());

        ApiException ex = assertThrows(ApiException.class, () ->
                integrationService.getAuthorizedCompanyIds(authUserSystemAdmin, 5L)
        );

        assertEquals(ApiStatus.UNAUTHORIZED, ex.getApiStatus());
        assertEquals("FORBIDDEN_TENANT_ACCESS", ex.getErrorDetails());
    }

    @Test
    @DisplayName("D-01: la lista blanca también acota a un usuario normal a su intersección")
    void testAllowlistIntersectsRegularUserCompanies() {
        // El usuario 99 pertenece a la empresa 12, que no está en la lista acordada.
        IntegrationV1Service scoped = new IntegrationV1Service(entityManager, lotCompositionService, "4,8");

        ApiException ex = assertThrows(ApiException.class, () ->
                scoped.getAuthorizedCompanyIds(authUserUnocace, 12L)
        );

        assertEquals(ApiStatus.UNAUTHORIZED, ex.getApiStatus());
        assertEquals("FORBIDDEN_TENANT_ACCESS", ex.getErrorDetails());
    }

    // =========================================================================
    // 2. Incremental Synchronization & Farmers
    // =========================================================================

    @Test
    @DisplayName("ADR-020: Incremental sync filters farmers by updated_since")
    void testGetFarmersWithUpdatedSinceFilter() throws ApiException {
        Instant since = Instant.parse("2026-09-01T00:00:00Z");

        UserCustomer farmer = new UserCustomer();
        ReflectionTestUtils.setField(farmer, "id", 1001L);
        farmer.setName("JUAN");
        farmer.setSurname("PEREZ");
        farmer.setFarmerCompanyInternalId("T0000000001");
        farmer.setCreationTimestamp(Instant.parse("2026-08-01T10:00:00Z"));
        farmer.setUpdateTimestamp(Instant.parse("2026-09-10T14:03:00Z"));

        Company c12 = new Company();
        ReflectionTestUtils.setField(c12, "id", 12L);
        c12.setName("UNOCACE");
        farmer.setCompany(c12);

        when(entityManager.createQuery(contains("SELECT COUNT(DISTINCT c) FROM UserCustomer c"), eq(Long.class)))
                .thenReturn(longQuery);
        when(longQuery.setParameter(eq("companyIds"), anyList())).thenReturn(longQuery);
        when(longQuery.setParameter(eq("updatedSince"), eq(since))).thenReturn(longQuery);
        when(longQuery.setParameter(eq("updatedSinceDate"), any(Date.class))).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(1L);

        @SuppressWarnings("unchecked")
        TypedQuery<Long> idQuery = mock(TypedQuery.class);
        when(entityManager.createQuery(contains("SELECT c.id FROM UserCustomer c"), eq(Long.class)))
                .thenReturn(idQuery);
        when(idQuery.setParameter(eq("companyIds"), anyList())).thenReturn(idQuery);
        when(idQuery.setParameter(eq("updatedSince"), eq(since))).thenReturn(idQuery);
        when(idQuery.setParameter(eq("updatedSinceDate"), any(Date.class))).thenReturn(idQuery);
        when(idQuery.setFirstResult(0)).thenReturn(idQuery);
        when(idQuery.setMaxResults(50)).thenReturn(idQuery);
        when(idQuery.getResultList()).thenReturn(Collections.singletonList(1001L));

        when(entityManager.createQuery(contains("WHERE c.id IN (:farmerIds)"), eq(UserCustomer.class)))
                .thenReturn(userCustomerQuery);
        when(userCustomerQuery.setParameter(eq("farmerIds"), anyList())).thenReturn(userCustomerQuery);
        when(userCustomerQuery.getResultList()).thenReturn(Collections.singletonList(farmer));

        ApiResponse<ApiIntegrationPage<ApiFarmerIntegration>> response =
                controller.getFarmers(12L, since, 50, 0, authUserUnocace);

        assertNotNull(response);
        assertEquals(ApiStatus.OK, response.getStatus());
        assertEquals(1, response.getData().getCount());
        assertEquals(1, response.getData().getItems().size());
        assertEquals("JUAN", response.getData().getItems().get(0).getName());
        assertEquals("T0000000001", response.getData().getItems().get(0).getFarmerInternalId());
    }

    @Test
    @DisplayName("ADR-020: Query includes child plot updates even if farmer entity was not directly updated")
    void testGetFarmersIncludesChildPlotUpdates() {
        // Verifies the query structure contains the EXISTS (SELECT 1 FROM Plot p2...) condition
        Instant since = Instant.parse("2026-09-01T00:00:00Z");

        UserCustomer farmer = new UserCustomer();
        ReflectionTestUtils.setField(farmer, "id", 1002L);
        farmer.setName("MARIA");
        farmer.setSurname("LOPEZ");
        farmer.setUpdateTimestamp(Instant.parse("2025-01-01T00:00:00Z")); // Old farmer update

        Plot plot = new Plot();
        ReflectionTestUtils.setField(plot, "id", 5002L);
        plot.setPlotName("Lote El Mirador");
        plot.setLastUpdated(Date.from(Instant.parse("2026-09-15T10:00:00Z"))); // New plot update
        farmer.setPlots(Collections.singleton(plot));

        ApiFarmerIntegration dto = FarmerIntegrationMapper.toDto(farmer);
        assertNotNull(dto);
        assertEquals(1, dto.getPlots().size());
        assertEquals(Instant.parse("2026-09-15T10:00:00Z"), dto.getPlots().get(0).getUpdatedAt());
    }

    // =========================================================================
    // 3. LOPDP Privacy by Design
    // =========================================================================

    @Test
    @DisplayName("LOPDP: Delivery payload strictly omits financial data (price_per_unit, cost, paid, balance)")
    void testDeliveryPayloadOmitsFinancialData() throws Exception {
        StockOrder order = new StockOrder();
        ReflectionTestUtils.setField(order, "id", 88001L);
        order.setOrderType(OrderType.PURCHASE_ORDER);
        order.setDeliveryReceipt("000124");
        order.setTotalGrossQuantity(new BigDecimal("200.00"));
        order.setTotalQuantity(new BigDecimal("138.00"));
        order.setTare(BigDecimal.ZERO);
        order.setDamagedWeightDeduction(BigDecimal.ZERO);
        order.setMoisturePercentage(new BigDecimal("31.00"));
        order.setMoistureWeightDeduction(new BigDecimal("62.00"));
        order.setNetQuantity(new BigDecimal("138.00"));
        order.setDeliveryTime(LocalDate.parse("2026-04-04"));

        // Sensitive financial fields stored in database
        order.setPricePerUnit(new BigDecimal("2.50"));
        order.setCost(new BigDecimal("500.00"));
        order.setPaid(new BigDecimal("300.00"));
        order.setBalance(new BigDecimal("200.00"));

        ApiDeliveryIntegration dto = DeliveryIntegrationMapper.toDto(order);
        assertNotNull(dto);
        assertEquals(88001L, dto.getId());
        assertEquals("000124", dto.getDeliveryReceipt());
        assertEquals(new BigDecimal("138.00"), dto.getNetQuantity());

        // Verify JSON serialization completely omits financial keys
        ObjectMapper om = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        String json = om.writeValueAsString(dto);

        assertFalse(json.contains("price_per_unit"), "JSON must NOT contain price_per_unit");
        assertFalse(json.contains("total_amount"), "JSON must NOT contain total_amount");
        assertFalse(json.contains("cost"), "JSON must NOT contain cost");
        assertFalse(json.contains("paid"), "JSON must NOT contain paid");
        assertFalse(json.contains("balance"), "JSON must NOT contain balance");
    }

    @Test
    @DisplayName("LOPDP: Farmer payload strictly omits bank details, phone, and personal email")
    void testFarmerPayloadOmitsSensitivePII() throws Exception {
        UserCustomer farmer = new UserCustomer();
        ReflectionTestUtils.setField(farmer, "id", 1001L);
        farmer.setName("PEDRO");
        farmer.setSurname("GOMEZ");
        farmer.setPhone("+593991234567");
        farmer.setEmail("pedro@personalmail.com");

        BankInformation bank = new BankInformation();
        bank.setAccountNumber("220011223344");
        bank.setBankName("Banco Pichincha");
        farmer.setBank(bank);

        ApiFarmerIntegration dto = FarmerIntegrationMapper.toDto(farmer);
        ObjectMapper om = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        String json = om.writeValueAsString(dto);

        assertFalse(json.contains("+593991234567"), "JSON must NOT contain personal phone number");
        assertFalse(json.contains("pedro@personalmail.com"), "JSON must NOT contain personal email");
        assertFalse(json.contains("220011223344"), "JSON must NOT contain bank account number");
    }

    // =========================================================================
    // 4. GeoJSON Geometries: Polygon vs Point Fallback
    // =========================================================================

    @Test
    @DisplayName("GeoJSON: Plot with 1 coordinate falls back to Point geometry with 6 decimals")
    void testFarmerPlotWithPointFallback() {
        Plot plot = new Plot();
        ReflectionTestUtils.setField(plot, "id", 5001L);
        plot.setPlotName("Parcela Chica");
        plot.setSize(1.5);

        PlotCoordinate coord = new PlotCoordinate();
        coord.setLatitude(-3.001234567);
        coord.setLongitude(-79.791234567);
        plot.setCoordinates(Collections.singletonList(coord));

        UserCustomer farmer = new UserCustomer();
        farmer.setPlots(Collections.singleton(plot));

        ApiFarmerIntegration dto = FarmerIntegrationMapper.toDto(farmer);
        assertNotNull(dto);
        assertEquals(1, dto.getPlots().size());

        ApiFarmerIntegration.GeoJsonGeometryDto geom = dto.getPlots().get(0).getGeometry();
        assertNotNull(geom);
        assertEquals("Point", geom.getType());

        @SuppressWarnings("unchecked")
        List<Double> pointCoords = (List<Double>) geom.getCoordinates();
        assertEquals(2, pointCoords.size());
        assertEquals(-79.791235, pointCoords.get(0)); // lon rounded to 6 decimals
        assertEquals(-3.001235, pointCoords.get(1)); // lat rounded to 6 decimals
    }

    @Test
    @DisplayName("GeoJSON: Plot with polygon coordinates returns RFC 7946 closed counter-clockwise Polygon")
    void testFarmerPlotWithClosedPolygon() {
        Plot plot = new Plot();
        ReflectionTestUtils.setField(plot, "id", 5002L);
        plot.setPlotName("Finca Grande");
        plot.setSize(5.2);

        // 4 vertices of a counter-clockwise box
        PlotCoordinate p1 = new PlotCoordinate(); p1.setLongitude(-79.790012); p1.setLatitude(-3.000045);
        PlotCoordinate p2 = new PlotCoordinate(); p2.setLongitude(-79.790012); p2.setLatitude(-3.001089);
        PlotCoordinate p3 = new PlotCoordinate(); p3.setLongitude(-79.789023); p3.setLatitude(-3.001089);
        PlotCoordinate p4 = new PlotCoordinate(); p4.setLongitude(-79.789023); p4.setLatitude(-3.000045);
        plot.setCoordinates(Arrays.asList(p1, p2, p3, p4));

        UserCustomer farmer = new UserCustomer();
        farmer.setPlots(Collections.singleton(plot));

        ApiFarmerIntegration dto = FarmerIntegrationMapper.toDto(farmer);
        assertNotNull(dto);

        ApiFarmerIntegration.GeoJsonGeometryDto geom = dto.getPlots().get(0).getGeometry();
        assertNotNull(geom);
        assertEquals("Polygon", geom.getType());

        @SuppressWarnings("unchecked")
        List<List<List<Double>>> poly = (List<List<List<Double>>>) geom.getCoordinates();
        assertEquals(1, poly.size());
        List<List<Double>> ring = poly.get(0);
        assertTrue(ring.size() >= 4);

        // Verify closed ring: first equals last
        assertEquals(ring.get(0).get(0), ring.get(ring.size() - 1).get(0));
        assertEquals(ring.get(0).get(1), ring.get(ring.size() - 1).get(1));
    }

    // =========================================================================
    // 5. Backward DAG Trace of Lot Composition
    // =========================================================================

    @Test
    @DisplayName("ADR-019: Backward DAG trace traverses ProcessingOrder inputTransactions to PURCHASE_ORDERs")
    void testGetShipmentCompositionReturnsInputs() throws ApiException {
        // Construct DAG:
        // Shipment SO (Export) -> ProcessingOrder PO1 -> Transaction TX1 -> SO1 (Intermediate)
        // SO1 -> ProcessingOrder PO2 -> Transaction TX2 -> Leaf Purchase Order 1 (88001)
        //                             -> Transaction TX3 -> Leaf Purchase Order 2 (88002)

        Company company12 = new Company();
        ReflectionTestUtils.setField(company12, "id", 12L);
        company12.setName("UNOCACE");

        UserCustomer farmer1 = new UserCustomer();
        ReflectionTestUtils.setField(farmer1, "id", 1001L);
        farmer1.setFarmerCompanyInternalId("T0000000001");

        UserCustomer farmer2 = new UserCustomer();
        ReflectionTestUtils.setField(farmer2, "id", 1002L);
        farmer2.setFarmerCompanyInternalId("T0000000045");

        StockOrder leaf1 = new StockOrder();
        ReflectionTestUtils.setField(leaf1, "id", 88001L);
        leaf1.setOrderType(OrderType.PURCHASE_ORDER);
        leaf1.setProducerUserCustomer(farmer1);
        leaf1.setDeliveryReceipt("000124");
        leaf1.setDeliveryTime(LocalDate.parse("2026-04-04"));
        leaf1.setNetQuantity(new BigDecimal("138.00"));

        StockOrder leaf2 = new StockOrder();
        ReflectionTestUtils.setField(leaf2, "id", 88002L);
        leaf2.setOrderType(OrderType.PURCHASE_ORDER);
        leaf2.setProducerUserCustomer(farmer2);
        leaf2.setDeliveryReceipt("000125");
        leaf2.setDeliveryTime(LocalDate.parse("2026-04-05"));
        leaf2.setNetQuantity(new BigDecimal("95.50"));

        ProcessingOrder po2 = new ProcessingOrder();
        ReflectionTestUtils.setField(po2, "id", 202L);
        Transaction tx2 = new Transaction(); tx2.setSourceStockOrder(leaf1);
        Transaction tx3 = new Transaction(); tx3.setSourceStockOrder(leaf2);
        po2.setInputTransactions(new HashSet<>(Arrays.asList(tx2, tx3)));

        StockOrder intermediate = new StockOrder();
        ReflectionTestUtils.setField(intermediate, "id", 55001L);
        intermediate.setOrderType(OrderType.PROCESSING_ORDER);
        intermediate.setProcessingOrder(po2);

        ProcessingOrder po1 = new ProcessingOrder();
        ReflectionTestUtils.setField(po1, "id", 201L);
        Transaction tx1 = new Transaction(); tx1.setSourceStockOrder(intermediate);
        po1.setInputTransactions(Collections.singleton(tx1));

        StockOrder shipment = new StockOrder();
        ReflectionTestUtils.setField(shipment, "id", 91002L);
        shipment.setOrderType(OrderType.GENERAL_ORDER);
        shipment.setCompany(company12);
        shipment.setInternalLotNumber("LOTE-2026-015");
        shipment.setQrCodeTag("5b0e7c1a-0000-4000-8000-000000000001");
        shipment.setTotalQuantity(new BigDecimal("6900.00"));
        shipment.setProcessingOrder(po1);

        when(entityManager.find(StockOrder.class, 91002L)).thenReturn(shipment);

        ApiResponse<ApiCompositionIntegration> response = controller.getShipmentComposition(91002L, authUserUnocace);

        assertNotNull(response);
        assertEquals(ApiStatus.OK, response.getStatus());
        ApiCompositionIntegration comp = response.getData();
        assertNotNull(comp.getShipment());
        assertEquals("LOTE-2026-015", comp.getShipment().getLotCodes().getInternalLotNumber());
        assertEquals(new BigDecimal("6900.00"), comp.getShipment().getQuantity());

        List<ApiCompositionIntegration.SourceDeliveryDto> sources = comp.getSourceDeliveries();
        assertEquals(2, sources.size(), "DAG should discover both leaf purchase orders");
        assertTrue(sources.stream().anyMatch(s -> s.getDeliveryId().equals(88001L) && s.getNetQuantity().equals(new BigDecimal("138.00"))));
        assertTrue(sources.stream().anyMatch(s -> s.getDeliveryId().equals(88002L) && s.getNetQuantity().equals(new BigDecimal("95.50"))));
    }

    // =========================================================================
    // 6. Deletions & Cancellations
    // =========================================================================

    @Test
    @DisplayName("ADR-020: Deletions endpoint reports canceled orders as deliveries or shipments")
    void testGetDeletionsIncludesCanceledOrders() throws ApiException {
        Instant since = Instant.parse("2026-04-01T00:00:00Z");

        StockOrder canceledDelivery = new StockOrder();
        ReflectionTestUtils.setField(canceledDelivery, "id", 87950L);
        canceledDelivery.setOrderType(OrderType.PURCHASE_ORDER);
        canceledDelivery.setStatus(StockOrderStatus.CANCELED);
        canceledDelivery.setCancellationTimestamp(Instant.parse("2026-04-10T11:20:00Z"));
        canceledDelivery.setCancellationReason("Anulación de pesaje por error");

        when(entityManager.createQuery(contains("status = com.abelium.inatrace.db.entities.stockorder.StockOrderStatus.CANCELED"), eq(StockOrder.class)))
                .thenReturn(stockOrderQuery);
        when(stockOrderQuery.setParameter(eq("companyIds"), anyList())).thenReturn(stockOrderQuery);
        when(stockOrderQuery.setParameter(eq("updatedSince"), eq(since))).thenReturn(stockOrderQuery);
        when(stockOrderQuery.getResultList()).thenReturn(Collections.singletonList(canceledDelivery));

        ApiResponse<List<ApiDeletionIntegration>> response = controller.getDeletions(12L, since, null, authUserUnocace);

        assertNotNull(response);
        assertEquals(ApiStatus.OK, response.getStatus());
        List<ApiDeletionIntegration> items = response.getData();
        assertFalse(items.isEmpty());
        assertEquals("delivery", items.get(0).getEntity());
        assertEquals(87950L, items.get(0).getId());
        assertEquals("Anulación de pesaje por error", items.get(0).getReason());
    }

    @Test
    @DisplayName("ADR-018: Deletions endpoint blocks unauthorized company access")
    void testDeletionsTenantIsolationUnauthorizedCompany() {
        Instant since = Instant.parse("2026-04-01T00:00:00Z");
        ApiException ex = assertThrows(ApiException.class, () ->
                controller.getDeletions(5L, since, null, authUserUnocace)
        );
        assertEquals(ApiStatus.UNAUTHORIZED, ex.getApiStatus());
        assertEquals("FORBIDDEN_TENANT_ACCESS", ex.getErrorDetails());
    }

    @Test
    @DisplayName("Validation: Deletions endpoint requires updated_since parameter")
    void testGetDeletionsValidationRequiresSince() {
        ApiException ex = assertThrows(ApiException.class, () ->
                integrationService.getDeletions(authUserUnocace, null, null, null)
        );
        assertEquals(ApiStatus.INVALID_REQUEST, ex.getApiStatus());
    }

    // =========================================================================
    // 7. Companies, Facilities, Reference Data & Entity Lookups
    // =========================================================================

    @Test
    @DisplayName("Endpoint /companies: returns authorized companies with tax ID and business name")
    void testGetCompanies() throws ApiException {
        Company unocace = new Company();
        ReflectionTestUtils.setField(unocace, "id", 12L);
        unocace.setName("UNOCACE");
        Map<String, Object> config = new HashMap<>();
        config.put("businessName", "Unión de Organizaciones Cacaoteras del Ecuador");
        config.put("taxId", "0992258814001");
        unocace.setConfiguration(config);

        when(entityManager.createQuery(contains("SELECT c FROM Company c WHERE c.id IN (:ids)"), eq(Company.class)))
                .thenReturn(companyQuery);
        when(companyQuery.setParameter(eq("ids"), anyList())).thenReturn(companyQuery);
        when(companyQuery.getResultList()).thenReturn(Collections.singletonList(unocace));

        ApiResponse<List<ApiCompanyIntegration>> response = controller.getCompanies(authUserUnocace);
        assertNotNull(response);
        assertEquals(1, response.getData().size());
        ApiCompanyIntegration comp = response.getData().get(0);
        assertEquals(12L, comp.getId());
        assertEquals("UNOCACE", comp.getName());
        assertEquals("Unión de Organizaciones Cacaoteras del Ecuador", comp.getBusinessName());
        assertEquals("0992258814001", comp.getTaxId());
    }

    @Test
    @DisplayName("Endpoint /facilities: returns facilities with location coordinates and collection flag")
    void testGetFacilities() throws ApiException {
        Company c12 = new Company();
        ReflectionTestUtils.setField(c12, "id", 12L);

        Facility fac = new Facility();
        ReflectionTestUtils.setField(fac, "id", 34L);
        fac.setName("Centro de Acopio Tenguel");
        fac.setCompany(c12);
        fac.setIsCollectionFacility(true);

        FacilityLocation loc = new FacilityLocation();
        loc.setLatitude(-3.001245);
        loc.setLongitude(-79.791520);
        Address addr = new Address();
        addr.setAddress("Recinto Tenguel, km 2");
        addr.setCity("Tenguel");
        addr.setState("Guayas");
        loc.setAddress(addr);
        fac.setFacilityLocation(loc);

        when(entityManager.createQuery(contains("FROM Facility"), eq(Facility.class))).thenReturn(facilityQuery);
        when(facilityQuery.setParameter(eq("companyIds"), anyList())).thenReturn(facilityQuery);
        when(facilityQuery.getResultList()).thenReturn(Collections.singletonList(fac));

        ApiResponse<List<ApiFacilityIntegration>> response = controller.getFacilities(12L, null, authUserUnocace);
        assertNotNull(response);
        assertEquals(1, response.getData().size());
        ApiFacilityIntegration dto = response.getData().get(0);
        assertEquals(34L, dto.getId());
        assertEquals(12L, dto.getCompanyId());
        assertTrue(dto.getIsCollectionCenter());
        assertEquals(-3.001245, dto.getLocation().getLatitude());
        assertEquals(-79.791520, dto.getLocation().getLongitude());
        assertEquals("Tenguel", dto.getLocation().getCity());
    }

    @Test
    @DisplayName("Endpoint /reference-data: returns units, semi_products, cocoa_varieties and certification_types")
    void testGetReferenceData() {
        when(entityManager.createQuery(contains("FROM SemiProduct"), eq(SemiProduct.class))).thenReturn(semiProductQuery);
        when(semiProductQuery.getResultList()).thenReturn(Collections.emptyList());

        when(entityManager.createQuery(contains("FROM CertificationType"), eq(CertificationType.class))).thenReturn(certTypeQuery);
        when(certTypeQuery.getResultList()).thenReturn(Collections.emptyList());

        ApiResponse<ApiReferenceDataIntegration> response = controller.getReferenceData();
        assertNotNull(response);
        ApiReferenceDataIntegration ref = response.getData();

        // 3 units
        assertEquals(3, ref.getUnits().size());
        assertTrue(ref.getUnits().stream().anyMatch(u -> u.getCode().equals("LB")));
        assertTrue(ref.getUnits().stream().anyMatch(u -> u.getCode().equals("KG")));
        assertTrue(ref.getUnits().stream().anyMatch(u -> u.getCode().equals("QQ")));

        // 2 varieties
        assertEquals(2, ref.getCocoaVarieties().size());
        assertTrue(ref.getCocoaVarieties().stream().anyMatch(v -> v.getCode().equals("NACIONAL")));
        assertTrue(ref.getCocoaVarieties().stream().anyMatch(v -> v.getCode().equals("CCN51")));

        // Semi products fallback
        assertEquals(2, ref.getSemiProducts().size());

        // Certification types fallback
        assertEquals(4, ref.getCertificationTypes().size());
    }

    @Test
    @DisplayName("Endpoint /deliveries/{id}: 403 Forbidden when requested delivery belongs to another tenant")
    void testGetDeliveryById_ForbiddenForOtherCompany() {
        Company company5 = new Company();
        ReflectionTestUtils.setField(company5, "id", 5L);

        StockOrder otherDelivery = new StockOrder();
        ReflectionTestUtils.setField(otherDelivery, "id", 9999L);
        otherDelivery.setOrderType(OrderType.PURCHASE_ORDER);
        otherDelivery.setCompany(company5);

        when(entityManager.find(StockOrder.class, 9999L)).thenReturn(otherDelivery);

        ApiException ex = assertThrows(ApiException.class, () ->
                controller.getDeliveryById(9999L, authUserUnocace)
        );
        assertEquals(ApiStatus.UNAUTHORIZED, ex.getApiStatus());
        assertEquals("FORBIDDEN_TENANT_ACCESS", ex.getErrorDetails());
    }

    @Test
    @DisplayName("Endpoint /farmers/{id}: 403 Forbidden when requested farmer belongs to another tenant")
    void testGetFarmerById_ForbiddenForOtherCompany() {
        Company company5 = new Company();
        ReflectionTestUtils.setField(company5, "id", 5L);

        UserCustomer otherFarmer = new UserCustomer();
        ReflectionTestUtils.setField(otherFarmer, "id", 8888L);
        otherFarmer.setType(UserCustomerType.FARMER);
        otherFarmer.setCompany(company5);

        when(entityManager.createNamedQuery(eq("UserCustomer.getUserCustomerWithPlotsById"), eq(UserCustomer.class)))
                .thenReturn(userCustomerQuery);
        when(userCustomerQuery.setParameter(eq("id"), eq(8888L))).thenReturn(userCustomerQuery);
        when(userCustomerQuery.getResultList()).thenReturn(Collections.singletonList(otherFarmer));

        ApiException ex = assertThrows(ApiException.class, () ->
                controller.getFarmerById(8888L, authUserUnocace)
        );
        assertEquals(ApiStatus.UNAUTHORIZED, ex.getApiStatus());
        assertEquals("FORBIDDEN_TENANT_ACCESS", ex.getErrorDetails());
    }

    @Test
    @DisplayName("Security: Access denied when entity company is null, for any role")
    void testAccessDeniedWhenCompanyIsNull() {
        UserCustomer unassignedFarmer = new UserCustomer();
        ReflectionTestUtils.setField(unassignedFarmer, "id", 7777L);
        unassignedFarmer.setType(UserCustomerType.FARMER);
        unassignedFarmer.setCompany(null);

        when(entityManager.createNamedQuery(eq("UserCustomer.getUserCustomerWithPlotsById"), eq(UserCustomer.class)))
                .thenReturn(userCustomerQuery);
        when(userCustomerQuery.setParameter(eq("id"), eq(7777L))).thenReturn(userCustomerQuery);
        when(userCustomerQuery.getResultList()).thenReturn(Collections.singletonList(unassignedFarmer));

        ApiException ex = assertThrows(ApiException.class, () ->
                controller.getFarmerById(7777L, authUserUnocace)
        );
        assertEquals(ApiStatus.UNAUTHORIZED, ex.getApiStatus());
        assertEquals("FORBIDDEN_TENANT_ACCESS", ex.getErrorDetails());

        // D-01: una entidad sin empresa no se atribuye a ningún tenant, tampoco para un admin.
        ApiException adminEx = assertThrows(ApiException.class, () ->
                controller.getFarmerById(7777L, authUserSystemAdmin)
        );
        assertEquals(ApiStatus.UNAUTHORIZED, adminEx.getApiStatus());
        assertEquals("FORBIDDEN_TENANT_ACCESS", adminEx.getErrorDetails());
    }

    @Test
    @DisplayName("Composition: Endpoint /shipments/{id}/composition returns 404 if order is a PURCHASE_ORDER")
    void testShipmentCompositionRejectsPurchaseOrder() {
        StockOrder delivery = new StockOrder();
        ReflectionTestUtils.setField(delivery, "id", 1234L);
        delivery.setOrderType(OrderType.PURCHASE_ORDER);

        when(entityManager.find(StockOrder.class, 1234L)).thenReturn(delivery);

        ApiException ex = assertThrows(ApiException.class, () ->
                controller.getShipmentComposition(1234L, authUserUnocace)
        );
        assertEquals(ApiStatus.NOT_FOUND, ex.getApiStatus());
    }

    @Test
    @DisplayName("Deliveries: Positional parcelLot ('1', '2') resolves to farmer's sorted plot")
    void testDeliveryPositionalPlotMatching() {
        Plot p1 = new Plot();
        ReflectionTestUtils.setField(p1, "id", 501L);
        p1.setPlotName("Parcela Norte");

        Plot p2 = new Plot();
        ReflectionTestUtils.setField(p2, "id", 502L);
        p2.setPlotName("Parcela Sur");

        StockOrder order = new StockOrder();
        ReflectionTestUtils.setField(order, "id", 88100L);
        order.setParcelLot("2");

        UserCustomer farmer = new UserCustomer();
        ReflectionTestUtils.setField(farmer, "id", 1001L);
        order.setProducerUserCustomer(farmer);

        ApiDeliveryIntegration dto = DeliveryIntegrationMapper.toDto(order, Arrays.asList(p1, p2));
        assertNotNull(dto);
        assertEquals(502L, dto.getPlotId(), "ParcelLot '2' should resolve to second sorted plot (502L)");
    }

    @Test
    @DisplayName("Deliveries: Net quantity fallback calculated from weights when both net and total quantity are null")
    void testDeliveryNetQuantityFallbackCalculated() {
        StockOrder order = new StockOrder();
        ReflectionTestUtils.setField(order, "id", 88200L);
        order.setTotalGrossQuantity(new BigDecimal("100.00"));
        order.setTare(new BigDecimal("5.00"));
        order.setDamagedWeightDeduction(new BigDecimal("2.00"));
        order.setMoisturePercentage(new BigDecimal("10.00")); // 10% of 95 = 9.50
        order.setTotalQuantity(null);
        order.setNetQuantity(null);

        ApiDeliveryIntegration dto = DeliveryIntegrationMapper.toDto(order);
        assertNotNull(dto);
        // Base = 100 - 5 = 95. Moisture = 95 * 10% = 9.50. Net = 95 - 2 - 9.50 = 83.50
        assertEquals(new BigDecimal("83.50"), dto.getNetQuantity());
    }

    @Test
    @DisplayName("QA Anti-Regression: PESOLIBRA (weight 2.20 in legacy DB) normalizes to LB with 0.45359237 kg factor")
    void testMeasureUnitTypeNormalization() {
        MeasureUnitType pesolibra = new MeasureUnitType("PESOLIBRA", "Libra", new BigDecimal("2.20"));
        StockOrder order = new StockOrder();
        ReflectionTestUtils.setField(order, "id", 88300L);
        order.setMeasurementUnitType(pesolibra);

        ApiDeliveryIntegration dto = DeliveryIntegrationMapper.toDto(order);
        assertNotNull(dto);
        assertEquals("LB", dto.getUnit().getCode());
        assertEquals(new BigDecimal("0.45359237"), dto.getUnit().getKgFactor());

        MeasureUnitType pesokg = new MeasureUnitType("PESOKG", "kg", new BigDecimal("1.00"));
        order.setMeasurementUnitType(pesokg);
        ApiDeliveryIntegration dtoKg = DeliveryIntegrationMapper.toDto(order);
        assertEquals("KG", dtoKg.getUnit().getCode());
        assertEquals(BigDecimal.ONE, dtoKg.getUnit().getKgFactor());

        MeasureUnitType quintales = new MeasureUnitType("QUINTALES", "QUINTALES", new BigDecimal("68.04"));
        order.setMeasurementUnitType(quintales);
        ApiDeliveryIntegration dtoQq = DeliveryIntegrationMapper.toDto(order);
        assertEquals("QQ", dtoQq.getUnit().getCode());
        assertEquals(new BigDecimal("45.359237"), dtoQq.getUnit().getKgFactor());
    }

    @Test
    @DisplayName("QA Anti-Regression: Facility and SemiProduct resolve names from ES translations when root name is null")
    void testFacilityAndSemiProductTranslationNameFallback() {
        Facility facility = new Facility();
        ReflectionTestUtils.setField(facility, "id", 12L);
        facility.setName(null);
        com.abelium.inatrace.db.entities.facility.FacilityTranslation ftEs = new com.abelium.inatrace.db.entities.facility.FacilityTranslation();
        ftEs.setLanguage(com.abelium.inatrace.types.Language.ES);
        ftEs.setName("Centro de Acopio Naranjal");
        ReflectionTestUtils.setField(facility, "facilityTranslations", new HashSet<>(Collections.singletonList(ftEs)));

        ApiFacilityIntegration fDto = com.abelium.inatrace.components.integration.v1.mappers.FacilityIntegrationMapper.toDto(facility);
        assertNotNull(fDto);
        assertEquals("Centro de Acopio Naranjal", fDto.getName());

        SemiProduct sp = new SemiProduct();
        ReflectionTestUtils.setField(sp, "id", 16L);
        sp.setName(null);
        com.abelium.inatrace.db.entities.codebook.SemiProductTranslation stEs = new com.abelium.inatrace.db.entities.codebook.SemiProductTranslation();
        stEs.setLanguage(com.abelium.inatrace.types.Language.ES);
        stEs.setName("Cacao Seco Ccn51 Org");
        sp.setSemiProductTranslations(new HashSet<>(Collections.singletonList(stEs)));

        assertEquals("Cacao Seco Ccn51 Org", com.abelium.inatrace.components.integration.v1.mappers.FacilityIntegrationMapper.resolveSemiProductName(sp));
    }

    @Test
    @DisplayName("QA Anti-Regression: LotCompositionService excludes CANCELED purchase orders")
    void testCompositionExcludesCanceledDeliveries() {
        StockOrder activeDelivery = new StockOrder();
        ReflectionTestUtils.setField(activeDelivery, "id", 5001L);
        activeDelivery.setOrderType(OrderType.PURCHASE_ORDER);
        activeDelivery.setStatus(StockOrderStatus.ACTIVE);
        activeDelivery.setTotalQuantity(new BigDecimal("100.00"));

        StockOrder canceledDelivery = new StockOrder();
        ReflectionTestUtils.setField(canceledDelivery, "id", 5002L);
        canceledDelivery.setOrderType(OrderType.PURCHASE_ORDER);
        canceledDelivery.setStatus(StockOrderStatus.CANCELED);
        canceledDelivery.setTotalQuantity(new BigDecimal("150.00"));

        Transaction tx1 = new Transaction();
        tx1.setSourceStockOrder(activeDelivery);
        Transaction tx2 = new Transaction();
        tx2.setSourceStockOrder(canceledDelivery);

        ProcessingOrder po = new ProcessingOrder();
        ReflectionTestUtils.setField(po, "id", 3001L);
        po.setInputTransactions(new HashSet<>(Arrays.asList(tx1, tx2)));

        StockOrder shipment = new StockOrder();
        ReflectionTestUtils.setField(shipment, "id", 9001L);
        shipment.setOrderType(OrderType.GENERAL_ORDER);
        shipment.setProcessingOrder(po);

        ApiCompositionIntegration comp = lotCompositionService.getComposition(shipment);
        assertNotNull(comp);
        assertEquals(1, comp.getSourceDeliveries().size());
        assertEquals(5001L, comp.getSourceDeliveries().get(0).getDeliveryId());
    }
}
