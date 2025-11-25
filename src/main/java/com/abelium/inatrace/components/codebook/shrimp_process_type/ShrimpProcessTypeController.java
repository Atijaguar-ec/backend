package com.abelium.inatrace.components.codebook.shrimp_process_type;

import com.abelium.inatrace.api.*;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.codebook.shrimp_process_type.api.ApiShrimpProcessType;
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
 * REST controller for shrimp process type entity.
 *
 * @author INATrace Team
 */
@RestController
@RequestMapping("/chain/shrimp-process-type")
public class ShrimpProcessTypeController {

    private final ShrimpProcessTypeService service;

    @Autowired
    public ShrimpProcessTypeController(ShrimpProcessTypeService service) {
        this.service = service;
    }

    @GetMapping("list")
    @Operation(summary = "Get a paginated list of shrimp process types.")
    public ApiPaginatedResponse<ApiShrimpProcessType> getShrimpProcessTypeList(
            @Valid ApiPaginatedRequest request,
            @RequestHeader(value = "language", defaultValue = "ES", required = false) Language language) {

        return new ApiPaginatedResponse<>(service.getShrimpProcessTypeList(request, language));
    }

    @GetMapping("list/active")
    @Operation(summary = "Get a list of active shrimp process types.")
    public ApiResponse<List<ApiShrimpProcessType>> getActiveShrimpProcessTypes(
            @RequestHeader(value = "language", defaultValue = "ES", required = false) Language language) {

        return new ApiResponse<>(service.getActiveShrimpProcessTypes(language));
    }

    @GetMapping("{id}")
    @Operation(summary = "Get a single shrimp process type with the provided ID.")
    public ApiResponse<ApiShrimpProcessType> getShrimpProcessType(
            @Valid @Parameter(description = "Shrimp process type ID", required = true) @PathVariable("id") Long id,
            @RequestHeader(value = "language", defaultValue = "ES", required = false) Language language) throws ApiException {

        return new ApiResponse<>(service.getShrimpProcessType(id, language));
    }

    @PutMapping
    @PreAuthorize("hasAnyAuthority('SYSTEM_ADMIN', 'REGIONAL_ADMIN')")
    @Operation(summary = "Create or update shrimp process type. If ID is provided, the entity with the provided ID is updated.")
    public ApiResponse<ApiBaseEntity> createOrUpdateShrimpProcessType(
            @AuthenticationPrincipal CustomUserDetails authUser,
            @Valid @RequestBody ApiShrimpProcessType api) throws ApiException {

        return new ApiResponse<>(service.createOrUpdateShrimpProcessType(authUser, api));
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    @Operation(summary = "Deletes a shrimp process type with the provided ID.")
    public ApiDefaultResponse deleteShrimpProcessType(
            @Valid @Parameter(description = "Shrimp process type ID", required = true) @PathVariable("id") Long id) throws ApiException {

        service.deleteShrimpProcessType(id);
        return new ApiDefaultResponse();
    }
}
