package com.abelium.inatrace.components.integration.v1.services;

import com.abelium.inatrace.api.ApiStatus;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.integration.v1.dto.*;
import com.abelium.inatrace.components.integration.v1.dto.ApiReferenceDataIntegration.*;
import com.abelium.inatrace.components.integration.v1.mappers.DeliveryIntegrationMapper;
import com.abelium.inatrace.components.integration.v1.mappers.FacilityIntegrationMapper;
import com.abelium.inatrace.components.integration.v1.mappers.FarmerIntegrationMapper;
import com.abelium.inatrace.components.integration.v1.mappers.ShipmentIntegrationMapper;
import com.abelium.inatrace.db.entities.codebook.CertificationType;
import com.abelium.inatrace.db.entities.codebook.SemiProduct;
import com.abelium.inatrace.db.entities.common.Plot;
import com.abelium.inatrace.db.entities.common.UserCustomer;
import com.abelium.inatrace.db.entities.company.Company;
import com.abelium.inatrace.db.entities.facility.Facility;
import com.abelium.inatrace.db.entities.stockorder.StockOrder;
import com.abelium.inatrace.db.entities.stockorder.StockOrderStatus;
import com.abelium.inatrace.db.entities.stockorder.enums.OrderType;
import com.abelium.inatrace.security.service.CustomUserDetails;
import com.abelium.inatrace.types.UserCustomerType;
import com.abelium.inatrace.types.UserRole;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class IntegrationV1Service {

    private static final Logger logger = LoggerFactory.getLogger(IntegrationV1Service.class);

    private final EntityManager entityManager;
    private final LotCompositionService lotCompositionService;

    @Autowired
    public IntegrationV1Service(EntityManager entityManager, LotCompositionService lotCompositionService) {
        this.entityManager = entityManager;
        this.lotCompositionService = lotCompositionService;
    }

    public List<Long> getAuthorizedCompanyIds(CustomUserDetails authUser, Long requestedCompanyId) throws ApiException {
        if (authUser == null) {
            throw new ApiException(ApiStatus.UNAUTHORIZED, "No autorizado para consultar la organización especificada.", null, "FORBIDDEN_TENANT_ACCESS");
        }

        List<Long> userCompanyIds;
        if (authUser.getUserRole() == UserRole.SYSTEM_ADMIN) {
            if (requestedCompanyId != null) {
                return Collections.singletonList(requestedCompanyId);
            }
            userCompanyIds = entityManager.createQuery("SELECT c.id FROM Company c WHERE c.status = com.abelium.inatrace.types.CompanyStatus.ACTIVE", Long.class).getResultList();
        } else {
            userCompanyIds = entityManager.createQuery(
                    "SELECT cu.company.id FROM CompanyUser cu WHERE cu.user.id = :userId",
                    Long.class
            ).setParameter("userId", authUser.getUserId()).getResultList();
        }

        if (userCompanyIds == null || userCompanyIds.isEmpty()) {
            throw new ApiException(ApiStatus.UNAUTHORIZED, "No autorizado para consultar la organización especificada.", null, "FORBIDDEN_TENANT_ACCESS");
        }

        if (requestedCompanyId != null) {
            if (!userCompanyIds.contains(requestedCompanyId)) {
                logger.warn("Access denied: user {} attempted to access unauthorized company {}", authUser.getUsername(), requestedCompanyId);
                throw new ApiException(ApiStatus.UNAUTHORIZED, "No autorizado para consultar la organización especificada.", null, "FORBIDDEN_TENANT_ACCESS");
            }
            return Collections.singletonList(requestedCompanyId);
        }

        return userCompanyIds;
    }

    public void validateCompanyAccess(CustomUserDetails authUser, Long companyId) throws ApiException {
        if (companyId == null) {
            if (authUser != null && authUser.getUserRole() == UserRole.SYSTEM_ADMIN) {
                return;
            }
            throw new ApiException(ApiStatus.UNAUTHORIZED, "No autorizado para consultar la organización especificada.", null, "FORBIDDEN_TENANT_ACCESS");
        }
        getAuthorizedCompanyIds(authUser, companyId);
    }

    @Transactional(readOnly = true)
    public List<ApiCompanyIntegration> getCompanies(CustomUserDetails authUser) throws ApiException {
        List<Long> authorizedIds = getAuthorizedCompanyIds(authUser, null);

        List<Company> companies = entityManager.createQuery(
                "SELECT c FROM Company c WHERE c.id IN (:ids) AND c.status = com.abelium.inatrace.types.CompanyStatus.ACTIVE ORDER BY c.id ASC",
                Company.class
        ).setParameter("ids", authorizedIds).getResultList();

        List<ApiCompanyIntegration> result = new ArrayList<>();
        for (Company c : companies) {
            String businessName = c.getName();
            String taxId = null;
            if (c.getConfiguration() != null) {
                Object bName = c.getConfiguration().get("businessName");
                if (bName != null) {
                    businessName = String.valueOf(bName);
                }
                Object tId = c.getConfiguration().get("taxId");
                if (tId != null) {
                    taxId = String.valueOf(tId);
                } else {
                    Object ruc = c.getConfiguration().get("ruc");
                    if (ruc != null) {
                        taxId = String.valueOf(ruc);
                    }
                }
            }
            if (taxId == null && c.getName() != null && c.getName().toUpperCase().contains("UNOCACE")) {
                taxId = "0992258814001";
            }
            result.add(new ApiCompanyIntegration(c.getId(), c.getName(), businessName, taxId));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<ApiFacilityIntegration> getFacilities(CustomUserDetails authUser, Long companyId, Instant updatedSince) throws ApiException {
        List<Long> authorizedCompanyIds = getAuthorizedCompanyIds(authUser, companyId);

        StringBuilder hql = new StringBuilder(
                "SELECT f FROM Facility f WHERE f.company.id IN (:companyIds) AND (f.isDeactivated IS NULL OR f.isDeactivated = false)"
        );
        if (updatedSince != null) {
            hql.append(" AND (f.updateTimestamp >= :updatedSince OR (f.updateTimestamp IS NULL AND f.creationTimestamp >= :updatedSince))");
        }
        hql.append(" ORDER BY f.id ASC");

        TypedQuery<Facility> query = entityManager.createQuery(hql.toString(), Facility.class);
        query.setParameter("companyIds", authorizedCompanyIds);
        if (updatedSince != null) {
            query.setParameter("updatedSince", updatedSince);
        }

        List<Facility> facilities = query.getResultList();
        List<ApiFacilityIntegration> result = new ArrayList<>();
        for (Facility f : facilities) {
            result.add(FacilityIntegrationMapper.toDto(f));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public ApiReferenceDataIntegration getReferenceData() {
        ApiReferenceDataIntegration refData = new ApiReferenceDataIntegration();

        // 1. Units
        refData.setUnits(Arrays.asList(
                new UnitDto("LB", "Libras", new BigDecimal("0.45359237")),
                new UnitDto("KG", "Kilogramos", BigDecimal.ONE),
                new UnitDto("QQ", "Quintales", new BigDecimal("45.359237"))
        ));

        // 2. Semi Products
        List<SemiProduct> semiProducts = entityManager.createQuery(
                "SELECT sp FROM SemiProduct sp ORDER BY sp.id ASC",
                SemiProduct.class
        ).getResultList();

        List<SemiProductDto> spDtos = new ArrayList<>();
        if (semiProducts != null && !semiProducts.isEmpty()) {
            for (SemiProduct sp : semiProducts) {
                String spName = FacilityIntegrationMapper.resolveSemiProductName(sp);
                String code = "SEMI_" + sp.getId();
                if (sp.getId() == 1 || (spName != null && spName.toLowerCase().contains("baba"))) {
                    code = "BABA";
                } else if (sp.getId() == 2 || (spName != null && spName.toLowerCase().contains("seco"))) {
                    code = "SECO";
                }
                spDtos.add(new SemiProductDto(sp.getId(), code, spName));
            }
        } else {
            spDtos.add(new SemiProductDto(1L, "BABA", "Cacao en Baba"));
            spDtos.add(new SemiProductDto(2L, "SECO", "Cacao Seco Fermentado"));
        }
        refData.setSemiProducts(spDtos);

        // 3. Cocoa Varieties
        refData.setCocoaVarieties(Arrays.asList(
                new VarietyDto("NACIONAL", "Nacional / Fino de Aroma"),
                new VarietyDto("CCN51", "CCN-51")
        ));

        // 4. Certification Types
        List<CertificationType> certTypes = entityManager.createQuery(
                "SELECT ct FROM CertificationType ct WHERE ct.status = com.abelium.inatrace.db.enums.CertificationStatus.ACTIVE ORDER BY ct.id ASC",
                CertificationType.class
        ).getResultList();

        List<CertificationTypeDto> certDtos = new ArrayList<>();
        if (certTypes != null && !certTypes.isEmpty()) {
            for (CertificationType ct : certTypes) {
                certDtos.add(new CertificationTypeDto(ct.getId(), ct.getCode(), ct.getName()));
            }
        } else {
            certDtos.add(new CertificationTypeDto(1L, "ORGANIC", "Orgánico"));
            certDtos.add(new CertificationTypeDto(2L, "FAIRTRADE", "Comercio Justo / SPP"));
            certDtos.add(new CertificationTypeDto(3L, "TRANSITION", "En Transición"));
            certDtos.add(new CertificationTypeDto(4L, "CONVENTIONAL", "Convencional"));
        }
        refData.setCertificationTypes(certDtos);

        return refData;
    }

    @Transactional(readOnly = true)
    public ApiIntegrationPage<ApiFarmerIntegration> getFarmers(CustomUserDetails authUser, Long companyId,
                                                               Instant updatedSince, int limit, int offset) throws ApiException {
        List<Long> authorizedCompanyIds = getAuthorizedCompanyIds(authUser, companyId);

        int safeLimit = Math.max(1, Math.min(limit, 500));
        int safeOffset = Math.max(0, offset);

        StringBuilder whereClause = new StringBuilder(
                " WHERE c.company.id IN (:companyIds) AND c.type = com.abelium.inatrace.types.UserCustomerType.FARMER"
        );
        if (updatedSince != null) {
            whereClause.append(" AND (c.updateTimestamp >= :updatedSince OR (c.updateTimestamp IS NULL AND c.creationTimestamp >= :updatedSince)")
                       .append(" OR EXISTS (SELECT 1 FROM Plot p2 WHERE p2.farmer.id = c.id AND p2.lastUpdated >= :updatedSinceDate))");
        }

        // Count Query (strip order by)
        String countHql = "SELECT COUNT(DISTINCT c) FROM UserCustomer c" + whereClause;
        TypedQuery<Long> countQuery = entityManager.createQuery(countHql, Long.class);
        countQuery.setParameter("companyIds", authorizedCompanyIds);
        if (updatedSince != null) {
            countQuery.setParameter("updatedSince", updatedSince);
            countQuery.setParameter("updatedSinceDate", Date.from(updatedSince));
        }
        Long totalCount = countQuery.getSingleResult();

        if (totalCount == null || totalCount == 0L || safeOffset >= totalCount) {
            return new ApiIntegrationPage<>(totalCount != null ? totalCount : 0L, safeOffset, safeLimit, Collections.emptyList());
        }

        // 1. Paginated ID Query (True SQL LIMIT / OFFSET without HHH000104 in-memory pagination)
        String idHql = "SELECT c.id FROM UserCustomer c" + whereClause + " ORDER BY c.id ASC";
        TypedQuery<Long> idQuery = entityManager.createQuery(idHql, Long.class);
        idQuery.setParameter("companyIds", authorizedCompanyIds);
        if (updatedSince != null) {
            idQuery.setParameter("updatedSince", updatedSince);
            idQuery.setParameter("updatedSinceDate", Date.from(updatedSince));
        }
        idQuery.setFirstResult(safeOffset);
        idQuery.setMaxResults(safeLimit);
        List<Long> farmerIds = idQuery.getResultList();

        if (farmerIds == null || farmerIds.isEmpty()) {
            return new ApiIntegrationPage<>(totalCount, safeOffset, safeLimit, Collections.emptyList());
        }

        // 2. Fetch full farmer graph with plots and location for the paginated slice
        List<UserCustomer> farmers = entityManager.createQuery(
                "SELECT DISTINCT c FROM UserCustomer c " +
                "LEFT JOIN FETCH c.plots p " +
                "LEFT JOIN FETCH c.company " +
                "LEFT JOIN FETCH c.userCustomerLocation ucl " +
                "WHERE c.id IN (:farmerIds) ORDER BY c.id ASC",
                UserCustomer.class
        ).setParameter("farmerIds", farmerIds).getResultList();

        // 3. Batch pre-fetch plot coordinates for all plots belonging to these farmers to avoid N+1 queries
        Set<Long> plotIds = new HashSet<>();
        for (UserCustomer f : farmers) {
            if (f.getPlots() != null) {
                for (Plot p : f.getPlots()) {
                    if (p.getId() != null) {
                        plotIds.add(p.getId());
                    }
                }
            }
        }

        Map<Long, List<com.abelium.inatrace.db.entities.common.PlotCoordinate>> coordinatesByPlotId = new HashMap<>();
        if (!plotIds.isEmpty()) {
            try {
                List<com.abelium.inatrace.db.entities.common.PlotCoordinate> allCoords = entityManager.createQuery(
                        "SELECT pc FROM PlotCoordinate pc WHERE pc.plot.id IN (:plotIds) ORDER BY pc.plot.id ASC, pc.id ASC",
                        com.abelium.inatrace.db.entities.common.PlotCoordinate.class
                ).setParameter("plotIds", plotIds).getResultList();
                for (com.abelium.inatrace.db.entities.common.PlotCoordinate pc : allCoords) {
                    if (pc.getPlot() != null && pc.getPlot().getId() != null) {
                        coordinatesByPlotId.computeIfAbsent(pc.getPlot().getId(), k -> new ArrayList<>()).add(pc);
                    }
                }
            } catch (Exception e) {
                logger.warn("Could not prefetch plot coordinates: {}", e.getMessage());
            }
        }

        List<ApiFarmerIntegration> items = new ArrayList<>();
        for (UserCustomer f : farmers) {
            items.add(FarmerIntegrationMapper.toDto(f, coordinatesByPlotId));
        }

        return new ApiIntegrationPage<>(totalCount, safeOffset, safeLimit, items);
    }

    @Transactional(readOnly = true)
    public ApiFarmerIntegration getFarmerById(CustomUserDetails authUser, Long id) throws ApiException {
        List<UserCustomer> list = entityManager.createNamedQuery("UserCustomer.getUserCustomerWithPlotsById", UserCustomer.class)
                .setParameter("id", id)
                .getResultList();
        UserCustomer farmer = (list != null && !list.isEmpty()) ? list.get(0) : null;
        if (farmer == null || farmer.getType() != UserCustomerType.FARMER) {
            throw new ApiException(ApiStatus.NOT_FOUND, "Farmer not found with ID: " + id);
        }
        Long compId = farmer.getCompany() != null ? farmer.getCompany().getId() : null;
        validateCompanyAccess(authUser, compId);
        return FarmerIntegrationMapper.toDto(farmer);
    }

    @Transactional(readOnly = true)
    public ApiIntegrationPage<ApiDeliveryIntegration> getDeliveries(CustomUserDetails authUser, Long companyId,
                                                                   Long facilityId, Long farmerId,
                                                                   LocalDate deliveryDateFrom, LocalDate deliveryDateTo,
                                                                   Instant updatedSince, int limit, int offset) throws ApiException {
        List<Long> authorizedCompanyIds = getAuthorizedCompanyIds(authUser, companyId);

        int safeLimit = Math.max(1, Math.min(limit, 500));
        int safeOffset = Math.max(0, offset);

        StringBuilder whereClause = new StringBuilder(
                " WHERE so.company.id IN (:companyIds) AND so.orderType = com.abelium.inatrace.db.entities.stockorder.enums.OrderType.PURCHASE_ORDER" +
                " AND (so.status IS NULL OR so.status != com.abelium.inatrace.db.entities.stockorder.StockOrderStatus.CANCELED)"
        );

        if (facilityId != null) {
            whereClause.append(" AND so.facility.id = :facilityId");
        }
        if (farmerId != null) {
            whereClause.append(" AND so.producerUserCustomer.id = :farmerId");
        }
        if (deliveryDateFrom != null) {
            whereClause.append(" AND (so.deliveryTime >= :dateFrom OR (so.deliveryTime IS NULL AND so.productionDate >= :dateFrom))");
        }
        if (deliveryDateTo != null) {
            whereClause.append(" AND (so.deliveryTime <= :dateTo OR (so.deliveryTime IS NULL AND so.productionDate <= :dateTo))");
        }
        if (updatedSince != null) {
            whereClause.append(" AND (so.updateTimestamp >= :updatedSince OR (so.updateTimestamp IS NULL AND so.creationTimestamp >= :updatedSince))");
        }

        // Count Query
        String countHql = "SELECT COUNT(so) FROM StockOrder so" + whereClause;
        TypedQuery<Long> countQuery = entityManager.createQuery(countHql, Long.class);
        setDeliveryParams(countQuery, authorizedCompanyIds, facilityId, farmerId, deliveryDateFrom, deliveryDateTo, updatedSince);
        Long totalCount = countQuery.getSingleResult();

        if (totalCount == null || totalCount == 0L || safeOffset >= totalCount) {
            return new ApiIntegrationPage<>(totalCount != null ? totalCount : 0L, safeOffset, safeLimit, Collections.emptyList());
        }

        // Data Query with fetch joins to eradicate N+1
        String dataHql = "SELECT so FROM StockOrder so " +
                "LEFT JOIN FETCH so.company " +
                "LEFT JOIN FETCH so.facility " +
                "LEFT JOIN FETCH so.producerUserCustomer puc " +
                "LEFT JOIN FETCH so.semiProduct " +
                "LEFT JOIN FETCH so.measurementUnitType " +
                whereClause + " ORDER BY so.id ASC";
        TypedQuery<StockOrder> dataQuery = entityManager.createQuery(dataHql, StockOrder.class);
        setDeliveryParams(dataQuery, authorizedCompanyIds, facilityId, farmerId, deliveryDateFrom, deliveryDateTo, updatedSince);
        dataQuery.setFirstResult(safeOffset);
        dataQuery.setMaxResults(safeLimit);

        List<StockOrder> deliveries = dataQuery.getResultList();

        // Batch pre-fetch plots for farmers in this page to eliminate N+1 queries
        Set<Long> producerFarmerIds = new HashSet<>();
        for (StockOrder so : deliveries) {
            if (so.getProducerUserCustomer() != null && so.getProducerUserCustomer().getId() != null) {
                producerFarmerIds.add(so.getProducerUserCustomer().getId());
            }
        }

        Map<Long, List<Plot>> plotsByFarmerId = new HashMap<>();
        if (!producerFarmerIds.isEmpty()) {
            try {
                List<Plot> plots = entityManager.createQuery(
                        "SELECT p FROM Plot p WHERE p.farmer.id IN (:farmerIds) ORDER BY p.farmer.id ASC, p.id ASC",
                        Plot.class
                ).setParameter("farmerIds", producerFarmerIds).getResultList();
                for (Plot p : plots) {
                    if (p.getFarmer() != null && p.getFarmer().getId() != null) {
                        plotsByFarmerId.computeIfAbsent(p.getFarmer().getId(), k -> new ArrayList<>()).add(p);
                    }
                }
            } catch (Exception e) {
                logger.warn("Could not prefetch producer plots: {}", e.getMessage());
            }
        }

        List<ApiDeliveryIntegration> items = new ArrayList<>();
        for (StockOrder so : deliveries) {
            List<Plot> farmerPlots = so.getProducerUserCustomer() != null
                    ? plotsByFarmerId.get(so.getProducerUserCustomer().getId())
                    : null;
            items.add(DeliveryIntegrationMapper.toDto(so, farmerPlots));
        }

        return new ApiIntegrationPage<>(totalCount, safeOffset, safeLimit, items);
    }

    private void setDeliveryParams(TypedQuery<?> query, List<Long> companyIds, Long facilityId, Long farmerId,
                                   LocalDate dateFrom, LocalDate dateTo, Instant updatedSince) {
        query.setParameter("companyIds", companyIds);
        if (facilityId != null) {
            query.setParameter("facilityId", facilityId);
        }
        if (farmerId != null) {
            query.setParameter("farmerId", farmerId);
        }
        if (dateFrom != null) {
            query.setParameter("dateFrom", dateFrom);
        }
        if (dateTo != null) {
            query.setParameter("dateTo", dateTo);
        }
        if (updatedSince != null) {
            query.setParameter("updatedSince", updatedSince);
        }
    }

    @Transactional(readOnly = true)
    public ApiDeliveryIntegration getDeliveryById(CustomUserDetails authUser, Long id) throws ApiException {
        StockOrder so = entityManager.find(StockOrder.class, id);
        if (so == null || so.getOrderType() != OrderType.PURCHASE_ORDER) {
            throw new ApiException(ApiStatus.NOT_FOUND, "Delivery not found with ID: " + id);
        }
        Long compId = so.getCompany() != null ? so.getCompany().getId() : null;
        validateCompanyAccess(authUser, compId);
        return DeliveryIntegrationMapper.toDto(so);
    }

    @Transactional(readOnly = true)
    public ApiIntegrationPage<ApiShipmentIntegration> getShipments(CustomUserDetails authUser, Long companyId,
                                                                    String lotCode, LocalDate shipmentDateFrom,
                                                                    LocalDate shipmentDateTo, Instant updatedSince,
                                                                    int limit, int offset) throws ApiException {
        List<Long> authorizedCompanyIds = getAuthorizedCompanyIds(authUser, companyId);

        int safeLimit = Math.max(1, Math.min(limit, 500));
        int safeOffset = Math.max(0, offset);

        StringBuilder whereClause = new StringBuilder(
                " WHERE so.company.id IN (:companyIds) AND (so.orderType IS NULL OR so.orderType != com.abelium.inatrace.db.entities.stockorder.enums.OrderType.PURCHASE_ORDER)" +
                " AND (so.status IS NULL OR so.status != com.abelium.inatrace.db.entities.stockorder.StockOrderStatus.CANCELED)"
        );

        if (lotCode != null && !lotCode.trim().isEmpty()) {
            whereClause.append(" AND (LOWER(so.internalLotNumber) = :lotCode OR LOWER(so.qrCodeTag) = :lotCode OR LOWER(so.orderId) = :lotCode)");
        }
        if (shipmentDateFrom != null) {
            whereClause.append(" AND (so.deliveryTime >= :dateFrom OR (so.deliveryTime IS NULL AND so.productionDate >= :dateFrom))");
        }
        if (shipmentDateTo != null) {
            whereClause.append(" AND (so.deliveryTime <= :dateTo OR (so.deliveryTime IS NULL AND so.productionDate <= :dateTo))");
        }
        if (updatedSince != null) {
            whereClause.append(" AND (so.updateTimestamp >= :updatedSince OR (so.updateTimestamp IS NULL AND so.creationTimestamp >= :updatedSince))");
        }

        // Count Query
        String countHql = "SELECT COUNT(so) FROM StockOrder so" + whereClause;
        TypedQuery<Long> countQuery = entityManager.createQuery(countHql, Long.class);
        setShipmentParams(countQuery, authorizedCompanyIds, lotCode, shipmentDateFrom, shipmentDateTo, updatedSince);
        Long totalCount = countQuery.getSingleResult();

        if (totalCount == null || totalCount == 0L || safeOffset >= totalCount) {
            return new ApiIntegrationPage<>(totalCount != null ? totalCount : 0L, safeOffset, safeLimit, Collections.emptyList());
        }

        // Data Query with fetch joins
        String dataHql = "SELECT so FROM StockOrder so " +
                "LEFT JOIN FETCH so.company " +
                "LEFT JOIN FETCH so.facility " +
                "LEFT JOIN FETCH so.consumerCompanyCustomer " +
                "LEFT JOIN FETCH so.quoteCompany " +
                "LEFT JOIN FETCH so.semiProduct " +
                "LEFT JOIN FETCH so.finalProduct " +
                "LEFT JOIN FETCH so.measurementUnitType " +
                whereClause + " ORDER BY so.id ASC";
        TypedQuery<StockOrder> dataQuery = entityManager.createQuery(dataHql, StockOrder.class);
        setShipmentParams(dataQuery, authorizedCompanyIds, lotCode, shipmentDateFrom, shipmentDateTo, updatedSince);
        dataQuery.setFirstResult(safeOffset);
        dataQuery.setMaxResults(safeLimit);

        List<StockOrder> shipments = dataQuery.getResultList();
        List<ApiShipmentIntegration> items = new ArrayList<>();
        for (StockOrder so : shipments) {
            items.add(ShipmentIntegrationMapper.toDto(so));
        }

        return new ApiIntegrationPage<>(totalCount, safeOffset, safeLimit, items);
    }

    private void setShipmentParams(TypedQuery<?> query, List<Long> companyIds, String lotCode,
                                    LocalDate dateFrom, LocalDate dateTo, Instant updatedSince) {
        query.setParameter("companyIds", companyIds);
        if (lotCode != null && !lotCode.trim().isEmpty()) {
            query.setParameter("lotCode", lotCode.trim().toLowerCase());
        }
        if (dateFrom != null) {
            query.setParameter("dateFrom", dateFrom);
        }
        if (dateTo != null) {
            query.setParameter("dateTo", dateTo);
        }
        if (updatedSince != null) {
            query.setParameter("updatedSince", updatedSince);
        }
    }

    @Transactional(readOnly = true)
    public ApiCompositionIntegration getShipmentComposition(CustomUserDetails authUser, Long id) throws ApiException {
        StockOrder shipment = entityManager.find(StockOrder.class, id);
        if (shipment == null) {
            throw new ApiException(ApiStatus.NOT_FOUND, "Shipment not found with ID: " + id);
        }
        if (shipment.getOrderType() == OrderType.PURCHASE_ORDER) {
            throw new ApiException(ApiStatus.NOT_FOUND, "Specified ID " + id + " is a delivery purchase order, not an export shipment");
        }
        Long compId = shipment.getCompany() != null ? shipment.getCompany().getId() : null;
        validateCompanyAccess(authUser, compId);
        return lotCompositionService.getComposition(shipment);
    }

    @Transactional(readOnly = true)
    public List<ApiDeletionIntegration> getDeletions(CustomUserDetails authUser, Long companyId, Instant updatedSince, String entityFilter) throws ApiException {
        if (updatedSince == null) {
            throw new ApiException(ApiStatus.INVALID_REQUEST, "Parameter 'updated_since' is required");
        }

        List<Long> authorizedCompanyIds = getAuthorizedCompanyIds(authUser, companyId);
        List<ApiDeletionIntegration> deletions = new ArrayList<>();

        // 1. Canceled StockOrders (deliveries or shipments)
        List<StockOrder> canceledOrders = entityManager.createQuery(
                "SELECT so FROM StockOrder so WHERE so.company.id IN (:companyIds) " +
                "AND so.status = com.abelium.inatrace.db.entities.stockorder.StockOrderStatus.CANCELED " +
                "AND (so.cancellationTimestamp >= :updatedSince OR (so.cancellationTimestamp IS NULL AND so.updateTimestamp >= :updatedSince)) " +
                "ORDER BY so.id ASC",
                StockOrder.class
        ).setParameter("companyIds", authorizedCompanyIds)
         .setParameter("updatedSince", updatedSince)
         .getResultList();

        for (StockOrder so : canceledOrders) {
            boolean isDelivery = (so.getOrderType() == OrderType.PURCHASE_ORDER);
            String entity = isDelivery ? "delivery" : "shipment";
            if (entityFilter == null || entityFilter.trim().isEmpty() || entityFilter.equalsIgnoreCase(entity)) {
                Instant deletedAt = so.getCancellationTimestamp() != null ? so.getCancellationTimestamp() : so.getUpdateTimestamp();
                String reason = so.getCancellationReason() != null ? so.getCancellationReason() : "Order canceled";
                deletions.add(new ApiDeletionIntegration(entity, so.getId(), deletedAt, reason));
            }
        }

        // 2. Physical Deletions from Envers audit tables (revtype = 2) scoped strictly to authorized companies
        long sinceMs = updatedSince.toEpochMilli();

        if (entityFilter == null || entityFilter.trim().isEmpty() || entityFilter.equalsIgnoreCase("plot")) {
            try {
                var query = entityManager.createNativeQuery(
                        "SELECT p.id, r.revtstmp FROM plot_aud p JOIN revinfo r ON p.rev = r.rev " +
                        "WHERE p.revtype = 2 AND r.revtstmp >= :sinceMs " +
                        "AND EXISTS (SELECT 1 FROM plot_aud p_prev " +
                        "            JOIN usercustomer_aud f_prev ON p_prev.farmer_id = f_prev.id " +
                        "            WHERE p_prev.id = p.id AND f_prev.company_id IN (:companyIds))"
                );
                if (query != null) {
                    query.setParameter("sinceMs", sinceMs);
                    query.setParameter("companyIds", authorizedCompanyIds);
                    @SuppressWarnings("unchecked")
                    List<Object[]> rows = query.getResultList();
                    for (Object[] row : rows) {
                        Long id = ((Number) row[0]).longValue();
                        Long ts = ((Number) row[1]).longValue();
                        deletions.add(new ApiDeletionIntegration("plot", id, Instant.ofEpochMilli(ts), "Physical deletion of plot"));
                    }
                }
            } catch (Exception e) {
                logger.warn("Could not query plot_aud table for deletions: {}", e.getMessage());
            }
        }

        if (entityFilter == null || entityFilter.trim().isEmpty() || entityFilter.equalsIgnoreCase("farmer")) {
            try {
                var query = entityManager.createNativeQuery(
                        "SELECT f.id, r.revtstmp FROM usercustomer_aud f JOIN revinfo r ON f.rev = r.rev " +
                        "WHERE f.revtype = 2 AND r.revtstmp >= :sinceMs " +
                        "AND EXISTS (SELECT 1 FROM usercustomer_aud f_prev " +
                        "            WHERE f_prev.id = f.id AND f_prev.company_id IN (:companyIds))"
                );
                if (query != null) {
                    query.setParameter("sinceMs", sinceMs);
                    query.setParameter("companyIds", authorizedCompanyIds);
                    @SuppressWarnings("unchecked")
                    List<Object[]> rows = query.getResultList();
                    for (Object[] row : rows) {
                        Long id = ((Number) row[0]).longValue();
                        Long ts = ((Number) row[1]).longValue();
                        deletions.add(new ApiDeletionIntegration("farmer", id, Instant.ofEpochMilli(ts), "Physical deletion of farmer"));
                    }
                }
            } catch (Exception e) {
                logger.warn("Could not query usercustomer_aud table for deletions: {}", e.getMessage());
            }
        }

        deletions.sort(Comparator.comparing(ApiDeletionIntegration::getDeletedAt, Comparator.nullsLast(Comparator.naturalOrder())));
        return deletions;
    }
}
