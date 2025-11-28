package com.abelium.inatrace.components.codebook.shrimp_flavor_defect;

import com.abelium.inatrace.api.*;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.codebook.shrimp_flavor_defect.api.ApiShrimpFlavorDefect;
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
 * REST controller for shrimp flavor defect entity.
 *
 * @author INATrace Team
 */
@RestController
@RequestMapping("/chain/shrimp-flavor-defect")
public class ShrimpFlavorDefectController {

    private final ShrimpFlavorDefectService service;

    @Autowired
    public ShrimpFlavorDefectController(ShrimpFlavorDefectService service) {
        this.service = service;
    }

    @GetMapping("list")
    @Operation(summary = "Get a paginated list of shrimp flavor defects.")
    public ApiPaginatedResponse<ApiShrimpFlavorDefect> getShrimpFlavorDefectList(
            @Valid ApiPaginatedRequest request,
            @RequestHeader(value = "language", defaultValue = "ES", required = false) Language language) {

        return new ApiPaginatedResponse<>(service.getShrimpFlavorDefectList(request, language));
    }

    @GetMapping("list/active")
    @Operation(summary = "Get a list of active shrimp flavor defects.")
    public ApiResponse<List<ApiShrimpFlavorDefect>> getActiveShrimpFlavorDefects(
            @RequestHeader(value = "language", defaultValue = "ES", required = false) Language language) {

        return new ApiResponse<>(service.getActiveShrimpFlavorDefects(language));
    }

    @GetMapping("{id}")
    @Operation(summary = "Get a single shrimp flavor defect with the provided ID.")
    public ApiResponse<ApiShrimpFlavorDefect> getShrimpFlavorDefect(
            @Valid @Parameter(description = "Shrimp flavor defect ID", required = true) @PathVariable("id") Long id,
            @RequestHeader(value = "language", defaultValue = "ES", required = false) Language language) throws ApiException {

        return new ApiResponse<>(service.getShrimpFlavorDefect(id, language));
    }

    @PutMapping
    @PreAuthorize("hasAnyAuthority('SYSTEM_ADMIN', 'REGIONAL_ADMIN')")
    @Operation(summary = "Create or update shrimp flavor defect. If ID is provided, the entity with the provided ID is updated.")
    public ApiResponse<ApiBaseEntity> createOrUpdateShrimpFlavorDefect(
            @AuthenticationPrincipal CustomUserDetails authUser,
            @Valid @RequestBody ApiShrimpFlavorDefect api) throws ApiException {

        return new ApiResponse<>(service.createOrUpdateShrimpFlavorDefect(authUser, api));
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    @Operation(summary = "Deletes a shrimp flavor defect with the provided ID.")
    public ApiDefaultResponse deleteShrimpFlavorDefect(
            @Valid @Parameter(description = "Shrimp flavor defect ID", required = true) @PathVariable("id") Long id) throws ApiException {

        service.deleteShrimpFlavorDefect(id);
        return new ApiDefaultResponse();
    }
}
