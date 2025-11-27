package com.abelium.inatrace.components.laboratory;

import com.abelium.inatrace.api.ApiStatus;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.common.BaseService;
import com.abelium.inatrace.components.company.CompanyQueries;
import com.abelium.inatrace.components.laboratory.api.ApiLaboratoryAnalysis;
import com.abelium.inatrace.db.entities.laboratory.LaboratoryAnalysis;
import com.abelium.inatrace.db.entities.stockorder.StockOrder;
import com.abelium.inatrace.db.repositories.laboratory.LaboratoryAnalysisRepository;
import com.abelium.inatrace.security.service.CustomUserDetails;
import com.abelium.inatrace.security.utils.PermissionsUtil;
import com.abelium.inatrace.tools.Queries;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for LaboratoryAnalysis.
 */
@Lazy
@Service
public class LaboratoryAnalysisService extends BaseService {

    private final LaboratoryAnalysisRepository laboratoryAnalysisRepository;
    private final CompanyQueries companyQueries;

    @Autowired
    public LaboratoryAnalysisService(LaboratoryAnalysisRepository laboratoryAnalysisRepository,
                                     CompanyQueries companyQueries) {
        this.laboratoryAnalysisRepository = laboratoryAnalysisRepository;
        this.companyQueries = companyQueries;
    }

    /**
     * List all analyses approved for purchase and not yet linked to a destination stock order,
     * filtered by source stock order company.
     */
    @Transactional
    public List<ApiLaboratoryAnalysis> getApprovedAvailableForCompany(Long companyId, CustomUserDetails user) throws ApiException {

        // Validate company and permissions (user must be enrolled in company)
        var company = companyQueries.fetchCompany(companyId);
        PermissionsUtil.checkUserIfCompanyEnrolled(company.getUsers().stream().toList(), user);

        return laboratoryAnalysisRepository
                .findByApprovedForPurchaseTrueAndDestinationStockOrderIsNull()
                .stream()
                .filter(analysis -> analysis.getStockOrder() != null
                        && analysis.getStockOrder().getCompany() != null
                        && companyId.equals(analysis.getStockOrder().getCompany().getId()))
                .map(LaboratoryAnalysisMapper::toApiLaboratoryAnalysis)
                .collect(Collectors.toList());
    }

    /**
     * Create or update a LaboratoryAnalysis from API data for a StockOrder.
     * This is used when a shrimp collection center captures sensorial analysis data at delivery time.
     * Reutilizes the same pattern as ProcessingOrderService.createOrUpdateLaboratoryAnalysis.
     * 
     * @param apiStockOrder The API stock order containing sensorial analysis data
     * @param stockOrder The persisted StockOrder entity
     * @param userId The user ID for audit fields
     * @return The created/updated LaboratoryAnalysis entity, or null if no sensorial data is present
     */
    @Transactional
    public LaboratoryAnalysis createOrUpdateFromApiStockOrder(
            com.abelium.inatrace.components.stockorder.api.ApiStockOrder apiStockOrder,
            StockOrder stockOrder,
            Long userId) {
        
        // Check if any laboratory analysis data is present in the API request
        boolean hasAnalysisData = apiStockOrder.getSensorialRawOdor() != null
                || apiStockOrder.getSensorialRawTaste() != null
                || apiStockOrder.getSensorialRawColor() != null
                || apiStockOrder.getSensorialCookedOdor() != null
                || apiStockOrder.getSensorialCookedTaste() != null
                || apiStockOrder.getSensorialCookedColor() != null
                || apiStockOrder.getQualityNotes() != null
                || apiStockOrder.getMetabisulfiteLevelAcceptable() != null
                || apiStockOrder.getApprovedForPurchase() != null;

        if (!hasAnalysisData) {
            // No laboratory analysis data to persist
            return null;
        }

        // Find existing analysis for this stock order or create a new one
        List<LaboratoryAnalysis> existingAnalyses = em.createQuery(
                "SELECT la FROM LaboratoryAnalysis la WHERE la.stockOrder.id = :stockOrderId",
                LaboratoryAnalysis.class)
                .setParameter("stockOrderId", stockOrder.getId())
                .getResultList();

        LaboratoryAnalysis analysis;
        if (!existingAnalyses.isEmpty()) {
            // Update existing analysis (take the first one if multiple exist)
            analysis = existingAnalyses.get(0);
            if (userId != null) {
                analysis.setUpdatedBy(Queries.get(em, com.abelium.inatrace.db.entities.common.User.class, userId));
            }
        } else {
            // Create new analysis
            analysis = new LaboratoryAnalysis();
            analysis.setStockOrder(stockOrder);
            if (userId != null) {
                analysis.setCreatedBy(Queries.get(em, com.abelium.inatrace.db.entities.common.User.class, userId));
            }
            analysis.setAnalysisType(com.abelium.inatrace.db.entities.laboratory.enums.AnalysisType.SENSORIAL);
            analysis.setAnalysisDate(stockOrder.getProductionDate() != null 
                    ? stockOrder.getProductionDate().atStartOfDay().toInstant(java.time.ZoneOffset.UTC)
                    : java.time.Instant.now());
        }

        // Map sensorial analysis fields from API to entity
        analysis.setSampleNumber(apiStockOrder.getSampleNumber());
        analysis.setSensorialRawOdor(apiStockOrder.getSensorialRawOdor());
        analysis.setSensorialRawTaste(apiStockOrder.getSensorialRawTaste());
        analysis.setSensorialRawColor(apiStockOrder.getSensorialRawColor());
        analysis.setSensorialCookedOdor(apiStockOrder.getSensorialCookedOdor());
        analysis.setSensorialCookedTaste(apiStockOrder.getSensorialCookedTaste());
        analysis.setSensorialCookedColor(apiStockOrder.getSensorialCookedColor());
        analysis.setQualityNotes(apiStockOrder.getQualityNotes());
        analysis.setMetabisulfiteLevelAcceptable(apiStockOrder.getMetabisulfiteLevelAcceptable());
        analysis.setApprovedForPurchase(apiStockOrder.getApprovedForPurchase());

        // Handle quality document if provided
        if (apiStockOrder.getQualityDocument() != null && apiStockOrder.getQualityDocument().getId() != null) {
            com.abelium.inatrace.db.entities.common.Document qualityDoc = 
                Queries.get(em, com.abelium.inatrace.db.entities.common.Document.class, apiStockOrder.getQualityDocument().getId());
            analysis.setQualityDocument(qualityDoc);
        }

        // Persist the analysis
        if (existingAnalyses.isEmpty()) {
            em.persist(analysis);
            System.out.println("DEBUG: Created LaboratoryAnalysis ID: " + analysis.getId() + 
                " for StockOrder ID: " + stockOrder.getId());
        } else {
            System.out.println("DEBUG: Updated LaboratoryAnalysis ID: " + analysis.getId() + 
                " for StockOrder ID: " + stockOrder.getId());
        }
        
        return analysis;
    }

    /**
     * Mark a laboratory analysis as used by linking it to a destination stock order.
     */
    @Transactional
    public void markAnalysisUsed(Long analysisId, Long destinationStockOrderId, CustomUserDetails user) throws ApiException {

        LaboratoryAnalysis analysis = fetchEntity(analysisId, LaboratoryAnalysis.class);

        if (analysis.getDestinationStockOrder() != null) {
            throw new ApiException(ApiStatus.INVALID_REQUEST, "Laboratory analysis is already linked to a destination stock order");
        }

        StockOrder sourceStockOrder = analysis.getStockOrder();
        if (sourceStockOrder == null || sourceStockOrder.getCompany() == null) {
            throw new ApiException(ApiStatus.INVALID_REQUEST, "Laboratory analysis has no valid source stock order");
        }

        // Validate that user is connected with the products of the source company
        PermissionsUtil.checkUserIfConnectedWithProducts(
                companyQueries.fetchCompanyProducts(sourceStockOrder.getCompany().getId()),
                user
        );

        // Destination stock order must exist and belong to the same company
        StockOrder destinationStockOrder = fetchEntity(destinationStockOrderId, StockOrder.class);
        if (destinationStockOrder.getCompany() == null
                || !destinationStockOrder.getCompany().getId().equals(sourceStockOrder.getCompany().getId())) {
            throw new ApiException(ApiStatus.INVALID_REQUEST, "Destination stock order must belong to the same company as the laboratory analysis");
        }

        analysis.setDestinationStockOrder(destinationStockOrder);
        // Persistence is handled by transactional context / dirty checking
    }

    private <E> E fetchEntity(Long id, Class<E> entityClass) throws ApiException {

        E entity = Queries.get(em, entityClass, id);
        if (entity == null) {
            throw new ApiException(ApiStatus.INVALID_REQUEST, "Invalid " + entityClass.getSimpleName() + " ID");
        }
        return entity;
    }
}
