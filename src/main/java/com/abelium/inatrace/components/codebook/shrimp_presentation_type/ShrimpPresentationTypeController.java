package com.abelium.inatrace.components.codebook.shrimp_presentation_type;

import com.abelium.inatrace.api.*;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.codebook.shrimp_presentation_type.api.ApiShrimpPresentationType;
import com.abelium.inatrace.db.enums.ShrimpPresentationCategory;
import com.abelium.inatrace.security.service.CustomUserDetails;
import com.abelium.inatrace.types.Language;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for shrimp presentation type entity.
 *
 * @author INATrace Team
 */
@RestController
@RequestMapping("/chain/shrimp-presentation-type")
public class ShrimpPresentationTypeController {

    private final ShrimpPresentationTypeService service;

    @Autowired
    public ShrimpPresentationTypeController(ShrimpPresentationTypeService service) {
        this.service = service;
    }

    @GetMapping("list")
    @Operation(summary = "Get a paginated list of shrimp presentation types.")
    public ApiPaginatedResponse<ApiShrimpPresentationType> getShrimpPresentationTypeList(
            @Valid ApiPaginatedRequest request,
            @RequestHeader(value = "language", defaultValue = "ES", required = false) Language language) {

        return new ApiPaginatedResponse<>(service.getShrimpPresentationTypeList(request, language));
    }

    @GetMapping("list/active")
    @Operation(summary = "Get a list of active shrimp presentation types.")
    public ApiResponse<List<ApiShrimpPresentationType>> getActiveShrimpPresentationTypes(
            @RequestHeader(value = "language", defaultValue = "ES", required = false) Language language) {

        return new ApiResponse<>(service.getActiveShrimpPresentationTypes(language));
    }

    @GetMapping("list/category/{category}")
    @Operation(summary = "Get a list of active shrimp presentation types by category (SHELL_ON, BROKEN, OTHER).")
    public ApiResponse<List<ApiShrimpPresentationType>> getShrimpPresentationTypesByCategory(
            @Valid @Parameter(description = "Category: SHELL_ON, BROKEN, or OTHER", required = true) @PathVariable("category") ShrimpPresentationCategory category,
            @RequestHeader(value = "language", defaultValue = "ES", required = false) Language language) {

        return new ApiResponse<>(service.getShrimpPresentationTypesByCategory(category, language));
    }

    @GetMapping("{id}")
    @Operation(summary = "Get a single shrimp presentation type with the provided ID.")
    public ApiResponse<ApiShrimpPresentationType> getShrimpPresentationType(
            @Valid @Parameter(description = "Shrimp presentation type ID", required = true) @PathVariable("id") Long id,
            @RequestHeader(value = "language", defaultValue = "ES", required = false) Language language) throws ApiException {

        return new ApiResponse<>(service.getShrimpPresentationType(id, language));
    }

    @PutMapping
    @PreAuthorize("hasAnyAuthority('SYSTEM_ADMIN', 'REGIONAL_ADMIN')")
    @Operation(summary = "Create or update shrimp presentation type. If ID is provided, the entity with the provided ID is updated.")
    public ApiResponse<ApiBaseEntity> createOrUpdateShrimpPresentationType(
            @AuthenticationPrincipal CustomUserDetails authUser,
            @Valid @RequestBody ApiShrimpPresentationType api) throws ApiException {

        return new ApiResponse<>(service.createOrUpdateShrimpPresentationType(authUser, api));
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    @Operation(summary = "Deletes a shrimp presentation type with the provided ID.")
    public ApiDefaultResponse deleteShrimpPresentationType(
            @Valid @Parameter(description = "Shrimp presentation type ID", required = true) @PathVariable("id") Long id) throws ApiException {

        service.deleteShrimpPresentationType(id);
        return new ApiDefaultResponse();
    }
}
