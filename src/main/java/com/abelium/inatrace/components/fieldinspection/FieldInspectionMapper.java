package com.abelium.inatrace.components.fieldinspection;

import com.abelium.inatrace.components.fieldinspection.api.ApiFieldInspection;
import com.abelium.inatrace.db.entities.fieldinspection.FieldInspection;

/**
 * Mapper for FieldInspection entity.
 * Converts between entity and API models.
 * 
 * @author INATrace Team
 */
public final class FieldInspectionMapper {

    private FieldInspectionMapper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Convert FieldInspection entity to API model
     * 
     * @param entity The entity to convert
     * @return The API model, or null if entity is null
     */
    public static ApiFieldInspection toApiFieldInspection(FieldInspection entity) {

        if (entity == null) {
            return null;
        }

        ApiFieldInspection api = new ApiFieldInspection();
        
        // Basic fields
        api.setId(entity.getId());
        api.setCreationTimestamp(entity.getCreationTimestamp());
        api.setUpdateTimestamp(entity.getUpdateTimestamp());

        // Audit fields
        if (entity.getCreatedBy() != null) {
            api.setCreatedById(entity.getCreatedBy().getId());
        }
        if (entity.getUpdatedBy() != null) {
            api.setUpdatedById(entity.getUpdatedBy().getId());
        }

        // Stock order references
        if (entity.getSourceStockOrder() != null) {
            api.setSourceStockOrderId(entity.getSourceStockOrder().getId());
            api.setSourceStockOrderIdentifier(entity.getSourceStockOrder().getIdentifier());
        }
        if (entity.getDestinationStockOrder() != null) {
            api.setDestinationStockOrderId(entity.getDestinationStockOrder().getId());
        }

        // Company
        if (entity.getCompany() != null) {
            api.setCompanyId(entity.getCompany().getId());
        }

        // Inspection data
        api.setInspectionDate(entity.getInspectionDate());
        api.setInspectionTime(entity.getInspectionTime());

        // Producer info
        if (entity.getProducerUserCustomer() != null) {
            api.setProducerUserCustomerId(entity.getProducerUserCustomer().getId());
        }
        api.setProducerName(entity.getProducerName());

        // Sensorial results
        if (entity.getFlavorTestResult() != null) {
            api.setFlavorTestResult(entity.getFlavorTestResult().name());
        }
        api.setFlavorDefectTypeId(entity.getFlavorDefectTypeId());
        api.setFlavorDefectTypeCode(entity.getFlavorDefectTypeCode());
        api.setFlavorDefectTypeLabel(entity.getFlavorDefectTypeLabel());
        api.setPurchaseRecommended(entity.getPurchaseRecommended());
        api.setInspectionNotes(entity.getInspectionNotes());

        // Reception data
        api.setNumberOfGavetas(entity.getNumberOfGavetas());
        api.setNumberOfBines(entity.getNumberOfBines());
        api.setNumberOfPiscinas(entity.getNumberOfPiscinas());
        api.setGuiaRemisionNumber(entity.getGuiaRemisionNumber());
        api.setTotalQuantity(entity.getTotalQuantity());

        // Computed
        api.setAvailable(entity.isAvailable());

        return api;
    }
}
