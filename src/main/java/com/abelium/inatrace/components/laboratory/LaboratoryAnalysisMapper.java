package com.abelium.inatrace.components.laboratory;

import com.abelium.inatrace.components.common.CommonApiTools;
import com.abelium.inatrace.components.laboratory.api.ApiLaboratoryAnalysis;
import com.abelium.inatrace.db.entities.laboratory.LaboratoryAnalysis;

/**
 * Mapper for LaboratoryAnalysis entity.
 */
public final class LaboratoryAnalysisMapper {

    private LaboratoryAnalysisMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static ApiLaboratoryAnalysis toApiLaboratoryAnalysis(LaboratoryAnalysis entity) {

        if (entity == null) {
            return null;
        }

        ApiLaboratoryAnalysis api = new ApiLaboratoryAnalysis();
        api.setId(entity.getId());
        api.setCreationTimestamp(entity.getCreationTimestamp());
        api.setUpdateTimestamp(entity.getUpdateTimestamp());

        if (entity.getCreatedBy() != null) {
            api.setCreatedById(entity.getCreatedBy().getId());
        }
        if (entity.getUpdatedBy() != null) {
            api.setUpdatedById(entity.getUpdatedBy().getId());
        }
        if (entity.getStockOrder() != null) {
            api.setStockOrderId(entity.getStockOrder().getId());
            api.setQualityDocument(CommonApiTools.toApiDocument(entity.getStockOrder().getQualityDocument(), null));
        }

        if (entity.getAnalysisType() != null) {
            api.setAnalysisType(entity.getAnalysisType().name());
        }

        api.setAnalysisDate(entity.getAnalysisDate());
        api.setSensorialRawOdor(entity.getSensorialRawOdor());
        api.setSensorialRawTaste(entity.getSensorialRawTaste());
        api.setSensorialRawColor(entity.getSensorialRawColor());
        api.setSensorialCookedOdor(entity.getSensorialCookedOdor());
        api.setSensorialCookedTaste(entity.getSensorialCookedTaste());
        api.setSensorialCookedColor(entity.getSensorialCookedColor());
        api.setQualityNotes(entity.getQualityNotes());
        api.setMetabisulfiteLevelAcceptable(entity.getMetabisulfiteLevelAcceptable());
        api.setApprovedForPurchase(entity.getApprovedForPurchase());

        return api;
    }
}
