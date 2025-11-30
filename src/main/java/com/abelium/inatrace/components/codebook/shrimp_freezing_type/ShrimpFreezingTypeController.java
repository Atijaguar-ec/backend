package com.abelium.inatrace.components.codebook.shrimp_freezing_type;

import com.abelium.inatrace.api.ApiBaseEntity;
import com.abelium.inatrace.api.ApiPaginatedList;
import com.abelium.inatrace.api.ApiPaginatedRequest;
import com.abelium.inatrace.api.ApiResponse;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.codebook.shrimp_freezing_type.api.ApiShrimpFreezingType;
import com.abelium.inatrace.security.service.CustomUserDetails;
import com.abelium.inatrace.types.Language;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for ShrimpFreezingType catalog.
 */
@RestController
@RequestMapping("/chain/shrimp-freezing-type")
@Tag(name = "Shrimp Freezing Type", description = "Shrimp freezing type catalog operations (IQF, BLOCK, SEMI_IQF)")
public class ShrimpFreezingTypeController {

    private final ShrimpFreezingTypeService service;

    @Autowired
    public ShrimpFreezingTypeController(ShrimpFreezingTypeService service) {
        this.service = service;
    }

    @GetMapping("list")
    @Operation(summary = "Get paginated list of shrimp freezing types")
    public ApiPaginatedList<ApiShrimpFreezingType> getShrimpFreezingTypeList(
            @Valid ApiPaginatedRequest request,
            @RequestParam(required = false, defaultValue = "ES") Language language) {
        return service.getShrimpFreezingTypeList(request, language);
    }

    @GetMapping("list/active")
    @Operation(summary = "Get list of active shrimp freezing types")
    public ApiResponse<List<ApiShrimpFreezingType>> getActiveShrimpFreezingTypes(
            @RequestParam(required = false, defaultValue = "ES") Language language) {
        return new ApiResponse<>(service.getActiveShrimpFreezingTypes(language));
    }

    @GetMapping("{id}")
    @Operation(summary = "Get a shrimp freezing type by ID")
    public ApiResponse<ApiShrimpFreezingType> getShrimpFreezingType(
            @Valid @Parameter(description = "Freezing type ID", required = true) @PathVariable("id") Long id,
            @RequestParam(required = false, defaultValue = "ES") Language language) throws ApiException {
        return new ApiResponse<>(service.getShrimpFreezingType(id, language));
    }

    @PutMapping
    @Operation(summary = "Create or update a shrimp freezing type")
    public ApiResponse<ApiBaseEntity> createOrUpdateShrimpFreezingType(
            @AuthenticationPrincipal CustomUserDetails authUser,
            @Valid @RequestBody ApiShrimpFreezingType request) throws ApiException {
        return new ApiResponse<>(service.createOrUpdateShrimpFreezingType(authUser, request));
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Delete a shrimp freezing type")
    public ApiResponse<ApiBaseEntity> deleteShrimpFreezingType(
            @Valid @Parameter(description = "Freezing type ID", required = true) @PathVariable("id") Long id) throws ApiException {
        return new ApiResponse<>(service.deleteShrimpFreezingType(id));
    }
}
