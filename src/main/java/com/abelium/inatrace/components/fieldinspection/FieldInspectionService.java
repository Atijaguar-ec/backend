package com.abelium.inatrace.components.fieldinspection;

import com.abelium.inatrace.api.ApiStatus;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.common.BaseService;
import com.abelium.inatrace.components.company.CompanyQueries;
import com.abelium.inatrace.components.fieldinspection.api.ApiFieldInspection;
import com.abelium.inatrace.db.entities.common.UserCustomer;
import com.abelium.inatrace.db.entities.common.User;
import com.abelium.inatrace.db.entities.company.Company;
import com.abelium.inatrace.db.entities.fieldinspection.FieldInspection;
import com.abelium.inatrace.db.entities.stockorder.StockOrder;
import com.abelium.inatrace.db.repositories.fieldinspection.FieldInspectionRepository;
import com.abelium.inatrace.security.service.CustomUserDetails;
import com.abelium.inatrace.security.utils.PermissionsUtil;
import com.abelium.inatrace.tools.Queries;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for FieldInspection operations.
 * Handles field inspections performed at shrimp farms before delivery to packing plant.
 * 
 * @author INATrace Team
 */
@Lazy
@Service
public class FieldInspectionService extends BaseService {

    private final FieldInspectionRepository fieldInspectionRepository;
    private final CompanyQueries companyQueries;

    @Autowired
    public FieldInspectionService(FieldInspectionRepository fieldInspectionRepository,
                                  CompanyQueries companyQueries) {
        this.fieldInspectionRepository = fieldInspectionRepository;
        this.companyQueries = companyQueries;
    }

    /**
     * Get all available field inspections for a company.
     * Available means not yet linked to a destination stock order.
     * 
     * @param companyId Company ID to filter by
     * @param onlyRecommended If true, only return inspections where purchase is recommended
     * @param user Current user for permission check
     * @return List of available field inspections
     */
    @Transactional
    public List<ApiFieldInspection> getAvailableForCompany(Long companyId, 
                                                            boolean onlyRecommended,
                                                            CustomUserDetails user) throws ApiException {

        // Validate company and permissions
        var company = companyQueries.fetchCompany(companyId);
        PermissionsUtil.checkUserIfCompanyEnrolled(company.getUsers().stream().toList(), user);

        List<FieldInspection> inspections;
        if (onlyRecommended) {
            inspections = fieldInspectionRepository.findAvailableRecommendedByCompanyId(companyId);
        } else {
            inspections = fieldInspectionRepository.findAvailableByCompanyId(companyId);
        }

        return inspections.stream()
                .map(FieldInspectionMapper::toApiFieldInspection)
                .collect(Collectors.toList());
    }

    /**
     * Create a new field inspection record from a stock order.
     * Called when saving a stock order at a field inspection facility.
     * 
     * @param stockOrder The stock order from which to create the inspection
     * @param user Current user
     * @return The created field inspection
     */
    @Transactional
    public FieldInspection createFromStockOrder(StockOrder stockOrder, CustomUserDetails user) throws ApiException {

        if (stockOrder == null) {
            throw new ApiException(ApiStatus.INVALID_REQUEST, "Stock order is required");
        }

        // Check if inspection already exists for this stock order
        if (fieldInspectionRepository.existsBySourceStockOrderId(stockOrder.getId())) {
            // Update existing instead of creating duplicate
            return updateFromStockOrder(stockOrder, user);
        }

        FieldInspection inspection = new FieldInspection();
        
        // Set audit fields
        inspection.setCreatedBy(fetchEntity(user.getUserId(), User.class));
        
        // Set source stock order
        inspection.setSourceStockOrder(stockOrder);
        
        // Set company
        inspection.setCompany(stockOrder.getCompany());
        
        // Set inspection date/time
        if (stockOrder.getProductionDate() != null) {
            LocalDate productionDate = stockOrder.getProductionDate();
            inspection.setInspectionDate(productionDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } else {
            inspection.setInspectionDate(Instant.now());
        }
        
        if (stockOrder.getReceptionTime() != null) {
            inspection.setInspectionTime(stockOrder.getReceptionTime().toString());
        }
        
        // Set producer info
        UserCustomer producer = stockOrder.getProducerUserCustomer();
        if (producer != null) {
            inspection.setProducerUserCustomer(producer);
            inspection.setProducerName(formatProducerName(producer));
        }
        
        // Set sensorial results
        String flavorResult = stockOrder.getFlavorTestResult();
        if (flavorResult != null) {
            try {
                inspection.setFlavorTestResult(FieldInspection.FlavorTestResult.valueOf(flavorResult));
            } catch (IllegalArgumentException e) {
                inspection.setFlavorTestResult(FieldInspection.FlavorTestResult.NORMAL);
            }
        } else {
            inspection.setFlavorTestResult(FieldInspection.FlavorTestResult.NORMAL);
        }
        
        // Set defect info if applicable
        if (stockOrder.getFlavorDefectType() != null) {
            inspection.setFlavorDefectTypeId(stockOrder.getFlavorDefectType().getId());
            inspection.setFlavorDefectTypeCode(stockOrder.getFlavorDefectType().getCode());
            inspection.setFlavorDefectTypeLabel(stockOrder.getFlavorDefectType().getName());
        }
        
        // Set recommendation
        Boolean recommended = stockOrder.getPurchaseRecommended();
        inspection.setPurchaseRecommended(recommended != null ? recommended : true);
        
        // Set notes
        inspection.setInspectionNotes(stockOrder.getInspectionNotes());
        
        // Set reception data
        inspection.setNumberOfGavetas(stockOrder.getNumberOfGavetas());
        inspection.setNumberOfBines(parseInteger(stockOrder.getNumberOfBines()));
        inspection.setNumberOfPiscinas(parseInteger(stockOrder.getNumberOfPiscinas()));
        inspection.setGuiaRemisionNumber(stockOrder.getGuiaRemisionNumber());
        
        BigDecimal quantity = stockOrder.getTotalQuantity();
        if (quantity == null) {
            quantity = stockOrder.getTotalGrossQuantity();
        }
        inspection.setTotalQuantity(quantity);
        
        return fieldInspectionRepository.save(inspection);
    }

    /**
     * Update an existing field inspection from stock order changes.
     */
    @Transactional
    public FieldInspection updateFromStockOrder(StockOrder stockOrder, CustomUserDetails user) throws ApiException {

        FieldInspection inspection = fieldInspectionRepository.findBySourceStockOrderId(stockOrder.getId());
        if (inspection == null) {
            return createFromStockOrder(stockOrder, user);
        }

        // Set audit
        inspection.setUpdatedBy(fetchEntity(user.getUserId(), User.class));

        // Update inspection date/time
        if (stockOrder.getProductionDate() != null) {
            LocalDate productionDate = stockOrder.getProductionDate();
            inspection.setInspectionDate(productionDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
        
        if (stockOrder.getReceptionTime() != null) {
            inspection.setInspectionTime(stockOrder.getReceptionTime().toString());
        }

        // Update producer info
        UserCustomer producer = stockOrder.getProducerUserCustomer();
        if (producer != null) {
            inspection.setProducerUserCustomer(producer);
            inspection.setProducerName(formatProducerName(producer));
        }

        // Update sensorial results
        String flavorResult = stockOrder.getFlavorTestResult();
        if (flavorResult != null) {
            try {
                inspection.setFlavorTestResult(FieldInspection.FlavorTestResult.valueOf(flavorResult));
            } catch (IllegalArgumentException e) {
                // Keep existing value
            }
        }

        // Update defect info
        if (stockOrder.getFlavorDefectType() != null) {
            inspection.setFlavorDefectTypeId(stockOrder.getFlavorDefectType().getId());
            inspection.setFlavorDefectTypeCode(stockOrder.getFlavorDefectType().getCode());
            inspection.setFlavorDefectTypeLabel(stockOrder.getFlavorDefectType().getName());
        } else {
            inspection.setFlavorDefectTypeId(null);
            inspection.setFlavorDefectTypeCode(null);
            inspection.setFlavorDefectTypeLabel(null);
        }

        // Update recommendation
        Boolean recommended = stockOrder.getPurchaseRecommended();
        if (recommended != null) {
            inspection.setPurchaseRecommended(recommended);
        }

        // Update notes
        inspection.setInspectionNotes(stockOrder.getInspectionNotes());

        // Update reception data
        inspection.setNumberOfGavetas(stockOrder.getNumberOfGavetas());
        inspection.setNumberOfBines(parseInteger(stockOrder.getNumberOfBines()));
        inspection.setNumberOfPiscinas(parseInteger(stockOrder.getNumberOfPiscinas()));
        inspection.setGuiaRemisionNumber(stockOrder.getGuiaRemisionNumber());

        BigDecimal quantity = stockOrder.getTotalQuantity();
        if (quantity == null) {
            quantity = stockOrder.getTotalGrossQuantity();
        }
        inspection.setTotalQuantity(quantity);

        return fieldInspectionRepository.save(inspection);
    }

    /**
     * Mark a field inspection as used by linking it to a destination stock order.
     * 
     * @param inspectionId The field inspection ID
     * @param destinationStockOrderId The stock order at the packing plant
     * @param user Current user for permission check
     */
    @Transactional
    public void markInspectionUsed(Long inspectionId, Long destinationStockOrderId, CustomUserDetails user) throws ApiException {

        FieldInspection inspection = fetchEntity(inspectionId, FieldInspection.class);

        if (inspection.getDestinationStockOrder() != null) {
            throw new ApiException(ApiStatus.INVALID_REQUEST, 
                "Field inspection is already linked to a destination stock order");
        }

        Company sourceCompany = inspection.getCompany();
        if (sourceCompany == null) {
            throw new ApiException(ApiStatus.INVALID_REQUEST, 
                "Field inspection has no valid company");
        }

        // Validate user permissions
        PermissionsUtil.checkUserIfConnectedWithProducts(
                companyQueries.fetchCompanyProducts(sourceCompany.getId()),
                user
        );

        // Fetch and validate destination stock order
        StockOrder destinationStockOrder = fetchEntity(destinationStockOrderId, StockOrder.class);
        if (destinationStockOrder.getCompany() == null
                || !destinationStockOrder.getCompany().getId().equals(sourceCompany.getId())) {
            throw new ApiException(ApiStatus.INVALID_REQUEST, 
                "Destination stock order must belong to the same company as the field inspection");
        }

        // Link the inspection to destination
        inspection.setDestinationStockOrder(destinationStockOrder);
        inspection.setUpdatedBy(fetchEntity(user.getUserId(), User.class));

        // Also set the field inspection reference on the stock order
        destinationStockOrder.setFieldInspection(inspection);

        // Persistence handled by transactional context
    }

    /**
     * Get a field inspection by ID.
     */
    @Transactional
    public ApiFieldInspection getById(Long id, CustomUserDetails user) throws ApiException {

        FieldInspection inspection = fetchEntity(id, FieldInspection.class);

        // Validate permissions
        if (inspection.getCompany() != null) {
            var company = companyQueries.fetchCompany(inspection.getCompany().getId());
            PermissionsUtil.checkUserIfCompanyEnrolled(company.getUsers().stream().toList(), user);
        }

        return FieldInspectionMapper.toApiFieldInspection(inspection);
    }

    /**
     * Format producer name for display.
     */
    private String formatProducerName(UserCustomer producer) {
        if (producer == null) {
            return null;
        }
        
        // Prefer company name for legal entities
        if (producer.getPersonType() != null && 
            "LEGAL".equals(producer.getPersonType().name()) && 
            producer.getCompanyName() != null && 
            !producer.getCompanyName().trim().isEmpty()) {
            return producer.getCompanyName().trim();
        }
        
        // Fall back to name + surname
        StringBuilder name = new StringBuilder();
        if (producer.getName() != null) {
            name.append(producer.getName().trim());
        }
        if (producer.getSurname() != null) {
            if (name.length() > 0) {
                name.append(" ");
            }
            name.append(producer.getSurname().trim());
        }
        
        return name.length() > 0 ? name.toString() : null;
    }

    private <E> E fetchEntity(Long id, Class<E> entityClass) throws ApiException {
        E entity = Queries.get(em, entityClass, id);
        if (entity == null) {
            throw new ApiException(ApiStatus.INVALID_REQUEST, 
                "Invalid " + entityClass.getSimpleName() + " ID");
        }
        return entity;
    }

    private Integer parseInteger(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        try {
            return Integer.valueOf(trimmed);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
