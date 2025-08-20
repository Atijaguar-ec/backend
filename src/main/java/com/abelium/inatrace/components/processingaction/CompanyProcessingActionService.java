package com.abelium.inatrace.components.processingaction;

import com.abelium.inatrace.api.ApiStatus;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.processingaction.api.ApiCompanyProcessingAction;
import com.abelium.inatrace.db.entities.company.Company;
import com.abelium.inatrace.db.entities.processingaction.CompanyProcessingAction;
import com.abelium.inatrace.db.entities.processingaction.ProcessingAction;
import com.abelium.inatrace.db.repositories.company.CompanyRepository;
import com.abelium.inatrace.db.repositories.processingaction.CompanyProcessingActionRepository;
import com.abelium.inatrace.db.repositories.processingaction.ProcessingActionRepository;
import com.abelium.inatrace.types.Language;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing company-specific processing action configurations.
 */
@Service
public class CompanyProcessingActionService {

    @Autowired
    private CompanyProcessingActionRepository companyProcessingActionRepository;

    @Autowired
    private ProcessingActionRepository processingActionRepository;

    @Autowired
    private CompanyRepository companyRepository;

    /**
     * Get all enabled processing actions for a company, ordered by effective order.
     * This is the main method used by the frontend to get the available processing actions.
     * 
     * @param companyId the company ID
     * @param language the language for translations
     * @return list of enabled processing actions with effective ordering
     */
    @Transactional(readOnly = true)
    public List<ApiCompanyProcessingAction> getEnabledProcessingActionsForCompany(Long companyId, Language language) {
        List<CompanyProcessingAction> companyActions = companyProcessingActionRepository
                .findEnabledByCompanyIdOrderByEffectiveOrder(companyId);

        return companyActions.stream()
                .map(cpa -> CompanyProcessingActionMapper.toApiCompanyProcessingAction(cpa, language))
                .collect(Collectors.toList());
    }

    /**
     * Get all processing action configurations for a company (for management UI).
     * 
     * @param companyId the company ID
     * @param language the language for translations
     * @return list of all processing action configurations
     */
    @Transactional(readOnly = true)
    public List<ApiCompanyProcessingAction> getAllProcessingActionConfigurationsForCompany(Long companyId, Language language) {
        List<CompanyProcessingAction> companyActions = companyProcessingActionRepository
                .findAllByCompanyIdOrderByEffectiveOrder(companyId);

        return companyActions.stream()
                .map(cpa -> CompanyProcessingActionMapper.toApiCompanyProcessingAction(cpa, language))
                .collect(Collectors.toList());
    }

    /**
     * Update a company processing action configuration.
     * 
     * @param companyId the company ID
     * @param processingActionId the processing action ID
     * @param apiUpdate the update data
     * @param language the language for response
     * @return updated configuration
     */
    @Transactional
    public ApiCompanyProcessingAction updateCompanyProcessingAction(Long companyId, Long processingActionId, 
                                                                   ApiCompanyProcessingAction apiUpdate, Language language) {
        
        // Validate company exists
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ApiException(ApiStatus.INVALID_REQUEST, "Company not found"));

        // Validate processing action exists
        ProcessingAction processingAction = processingActionRepository.findById(processingActionId)
                .orElseThrow(() -> new ApiException(ApiStatus.INVALID_REQUEST, "Processing action not found"));

        // Find or create company processing action configuration
        CompanyProcessingAction companyProcessingAction = companyProcessingActionRepository
                .findByCompanyIdAndProcessingActionId(companyId, processingActionId)
                .orElseGet(() -> {
                    CompanyProcessingAction newConfig = new CompanyProcessingAction(company, processingAction);
                    return companyProcessingActionRepository.save(newConfig);
                });

        // Validate order override if provided
        if (apiUpdate.getOrderOverride() != null && apiUpdate.getOrderOverride() < 0) {
            throw new ApiException(ApiStatus.INVALID_REQUEST, "Order override must be >= 0");
        }

        // Update the configuration
        CompanyProcessingActionMapper.updateCompanyProcessingActionFromApi(companyProcessingAction, apiUpdate);
        
        CompanyProcessingAction saved = companyProcessingActionRepository.save(companyProcessingAction);
        return CompanyProcessingActionMapper.toApiCompanyProcessingAction(saved, language);
    }

    /**
     * Initialize company processing action configurations for a new company.
     * Creates enabled configurations for all existing processing actions.
     * 
     * @param companyId the new company ID
     */
    @Transactional
    public void initializeCompanyProcessingActions(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ApiException(ApiStatus.INVALID_REQUEST, "Company not found"));

        List<ProcessingAction> allProcessingActions = processingActionRepository.findAll();
        
        for (ProcessingAction processingAction : allProcessingActions) {
            // Check if configuration already exists
            Optional<CompanyProcessingAction> existing = companyProcessingActionRepository
                    .findByCompanyIdAndProcessingActionId(companyId, processingAction.getId());
            
            if (existing.isEmpty()) {
                CompanyProcessingAction config = new CompanyProcessingAction(company, processingAction);
                companyProcessingActionRepository.save(config);
            }
        }
    }

    /**
     * Initialize company processing action configurations for a new processing action.
     * Creates enabled configurations for all existing companies.
     * 
     * @param processingActionId the new processing action ID
     */
    @Transactional
    public void initializeProcessingActionForAllCompanies(Long processingActionId) {
        ProcessingAction processingAction = processingActionRepository.findById(processingActionId)
                .orElseThrow(() -> new ApiException(ApiStatus.INVALID_REQUEST, "Processing action not found"));

        List<Company> allCompanies = companyRepository.findAll();
        
        for (Company company : allCompanies) {
            // Check if configuration already exists
            Optional<CompanyProcessingAction> existing = companyProcessingActionRepository
                    .findByCompanyIdAndProcessingActionId(company.getId(), processingActionId);
            
            if (existing.isEmpty()) {
                CompanyProcessingAction config = new CompanyProcessingAction(company, processingAction);
                companyProcessingActionRepository.save(config);
            }
        }
    }
}
