package com.abelium.inatrace.components.processingaction;

import com.abelium.inatrace.api.ApiDefaultResponse;
import com.abelium.inatrace.api.ApiPaginatedList;
import com.abelium.inatrace.api.ApiResponse;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.processingaction.api.ApiCompanyProcessingAction;
import com.abelium.inatrace.security.service.CustomUserDetails;
import com.abelium.inatrace.types.Language;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST controller for company-specific processing action management.
 */
@RestController
@RequestMapping("/api/company/{companyId}/processing-actions")
@Tag(name = "Company Processing Actions", description = "Company-specific processing action configuration endpoints")
public class CompanyProcessingActionController {

    @Autowired
    private CompanyProcessingActionService companyProcessingActionService;

    @GetMapping
    @Operation(summary = "Get enabled processing actions for company", 
               description = "Returns list of processing actions enabled for the company, ordered by effective order")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "List of enabled processing actions",
            content = @Content(schema = @Schema(implementation = ApiPaginatedList.class))
        )
    })
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN') or @companyQueries.canViewCompany(authentication, #companyId)")
    public ApiResponse<List<ApiCompanyProcessingAction>> getEnabledProcessingActions(
            @Parameter(description = "Company ID", required = true)
            @PathVariable Long companyId,
            @Parameter(description = "Language for translations")
            @RequestParam(value = "language", defaultValue = "EN") Language language,
            @AuthenticationPrincipal CustomUserDetails authUser) {

        List<ApiCompanyProcessingAction> processingActions = companyProcessingActionService
                .getEnabledProcessingActionsForCompany(companyId, language);

        return new ApiResponse<>(processingActions);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all processing action configurations for company", 
               description = "Returns all processing action configurations (enabled and disabled) for management UI")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "List of all processing action configurations",
            content = @Content(schema = @Schema(implementation = ApiPaginatedList.class))
        )
    })
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN') or @companyQueries.canManageCompany(authentication, #companyId)")
    public ApiResponse<List<ApiCompanyProcessingAction>> getAllProcessingActionConfigurations(
            @Parameter(description = "Company ID", required = true)
            @PathVariable Long companyId,
            @Parameter(description = "Language for translations")
            @RequestParam(value = "language", defaultValue = "EN") Language language,
            @AuthenticationPrincipal CustomUserDetails authUser) {

        List<ApiCompanyProcessingAction> processingActions = companyProcessingActionService
                .getAllProcessingActionConfigurationsForCompany(companyId, language);

        return new ApiResponse<>(processingActions);
    }

    @PutMapping("/{processingActionId}")
    @Operation(summary = "Update company processing action configuration", 
               description = "Updates enabled status, order override, and alias label for a processing action")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Updated processing action configuration",
            content = @Content(schema = @Schema(implementation = ApiCompanyProcessingAction.class))
        )
    })
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN') or @companyQueries.canManageCompany(authentication, #companyId)")
    public ApiResponse<ApiCompanyProcessingAction> updateCompanyProcessingAction(
            @Parameter(description = "Company ID", required = true)
            @PathVariable Long companyId,
            @Parameter(description = "Processing Action ID", required = true)
            @PathVariable Long processingActionId,
            @Parameter(description = "Language for translations")
            @RequestParam(value = "language", defaultValue = "EN") Language language,
            @Valid @RequestBody ApiCompanyProcessingAction request,
            @AuthenticationPrincipal CustomUserDetails authUser) {

        ApiCompanyProcessingAction updated = companyProcessingActionService
                .updateCompanyProcessingAction(companyId, processingActionId, request, language);

        return new ApiResponse<>(updated);
    }

    @PostMapping("/initialize")
    @Operation(summary = "Initialize processing actions for company", 
               description = "Creates default configurations for all processing actions for a new company")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Processing actions initialized successfully"
        )
    })
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN') or @companyQueries.canManageCompany(authentication, #companyId)")
    public ApiDefaultResponse initializeCompanyProcessingActions(
            @Parameter(description = "Company ID", required = true)
            @PathVariable Long companyId,
            @AuthenticationPrincipal CustomUserDetails authUser) {

        companyProcessingActionService.initializeCompanyProcessingActions(companyId);
        return new ApiDefaultResponse();
    }
}
