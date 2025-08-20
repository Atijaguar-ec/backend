package com.abelium.inatrace.components.processingaction;

import com.abelium.inatrace.components.processingaction.api.ApiCompanyProcessingAction;
import com.abelium.inatrace.db.entities.processingaction.CompanyProcessingAction;
import com.abelium.inatrace.types.Language;

/**
 * Mapper for CompanyProcessingAction entity to API model.
 */
public class CompanyProcessingActionMapper {

    /**
     * Maps CompanyProcessingAction entity to API model with computed effective values.
     * 
     * @param entity the CompanyProcessingAction entity
     * @param language the language for processing action translations
     * @return ApiCompanyProcessingAction with effective order and label computed
     */
    public static ApiCompanyProcessingAction toApiCompanyProcessingAction(CompanyProcessingAction entity, Language language) {
        if (entity == null) {
            return null;
        }

        ApiCompanyProcessingAction api = new ApiCompanyProcessingAction();
        api.setId(entity.getId());
        api.setEnabled(entity.getEnabled());
        api.setOrderOverride(entity.getOrderOverride());
        api.setAliasLabel(entity.getAliasLabel());
        
        // Set computed effective values
        api.setEffectiveOrder(entity.getEffectiveOrder());
        api.setEffectiveLabel(entity.getEffectiveLabel());
        
        // Map the base processing action
        if (entity.getProcessingAction() != null) {
            api.setProcessingAction(ProcessingActionMapper.toApiProcessingAction(entity.getProcessingAction(), language));
        }

        return api;
    }

    /**
     * Updates CompanyProcessingAction entity from API model.
     * Only updates mutable fields: enabled, orderOverride, aliasLabel.
     * 
     * @param entity the entity to update
     * @param api the API model with new values
     */
    public static void updateCompanyProcessingActionFromApi(CompanyProcessingAction entity, ApiCompanyProcessingAction api) {
        if (entity == null || api == null) {
            return;
        }

        if (api.getEnabled() != null) {
            entity.setEnabled(api.getEnabled());
        }
        
        entity.setOrderOverride(api.getOrderOverride()); // Allow null to reset to global order
        entity.setAliasLabel(api.getAliasLabel()); // Allow null to reset to processing action name
    }
}
