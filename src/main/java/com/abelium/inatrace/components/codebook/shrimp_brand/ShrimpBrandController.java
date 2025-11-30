package com.abelium.inatrace.components.codebook.shrimp_brand;

import com.abelium.inatrace.api.ApiBaseEntity;
import com.abelium.inatrace.api.ApiPaginatedList;
import com.abelium.inatrace.api.ApiPaginatedRequest;
import com.abelium.inatrace.api.ApiResponse;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.codebook.shrimp_brand.api.ApiShrimpBrand;
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
 * REST controller for ShrimpBrand catalog.
 */
@RestController
@RequestMapping("/chain/shrimp-brand")
@Tag(name = "Shrimp Brand", description = "Shrimp brand catalog operations with weight per box")
public class ShrimpBrandController {

    private final ShrimpBrandService service;

    @Autowired
    public ShrimpBrandController(ShrimpBrandService service) {
        this.service = service;
    }

    @GetMapping("list")
    @Operation(summary = "Get paginated list of shrimp brands")
    public ApiPaginatedList<ApiShrimpBrand> getShrimpBrandList(
            @Valid ApiPaginatedRequest request,
            @RequestParam(required = false, defaultValue = "ES") Language language) {
        return service.getShrimpBrandList(request, language);
    }

    @GetMapping("list/active")
    @Operation(summary = "Get list of active shrimp brands")
    public ApiResponse<List<ApiShrimpBrand>> getActiveShrimpBrands(
            @RequestParam(required = false, defaultValue = "ES") Language language) {
        return new ApiResponse<>(service.getActiveShrimpBrands(language));
    }

    @GetMapping("{id}")
    @Operation(summary = "Get a shrimp brand by ID")
    public ApiResponse<ApiShrimpBrand> getShrimpBrand(
            @Valid @Parameter(description = "Brand ID", required = true) @PathVariable("id") Long id,
            @RequestParam(required = false, defaultValue = "ES") Language language) throws ApiException {
        return new ApiResponse<>(service.getShrimpBrand(id, language));
    }

    @PutMapping
    @Operation(summary = "Create or update a shrimp brand")
    public ApiResponse<ApiBaseEntity> createOrUpdateShrimpBrand(
            @AuthenticationPrincipal CustomUserDetails authUser,
            @Valid @RequestBody ApiShrimpBrand request) throws ApiException {
        return new ApiResponse<>(service.createOrUpdateShrimpBrand(authUser, request));
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Delete a shrimp brand")
    public ApiResponse<ApiBaseEntity> deleteShrimpBrand(
            @Valid @Parameter(description = "Brand ID", required = true) @PathVariable("id") Long id) throws ApiException {
        return new ApiResponse<>(service.deleteShrimpBrand(id));
    }
}
