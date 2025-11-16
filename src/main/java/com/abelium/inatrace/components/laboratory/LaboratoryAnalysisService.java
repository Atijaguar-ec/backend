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
