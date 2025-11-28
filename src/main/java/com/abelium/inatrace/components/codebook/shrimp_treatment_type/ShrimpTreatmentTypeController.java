package com.abelium.inatrace.components.codebook.shrimp_treatment_type;

import com.abelium.inatrace.api.ApiBaseEntity;
import com.abelium.inatrace.api.ApiPaginatedList;
import com.abelium.inatrace.api.ApiPaginatedRequest;
import com.abelium.inatrace.api.ApiResponse;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.codebook.shrimp_treatment_type.api.ApiShrimpTreatmentType;
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
 * REST controller for ShrimpTreatmentType catalog.
 */
@RestController
@RequestMapping("/chain/shrimp-treatment-type")
@Tag(name = "Shrimp Treatment Type", description = "Shrimp treatment type catalog operations")
public class ShrimpTreatmentTypeController {

    private final ShrimpTreatmentTypeService service;

    @Autowired
    public ShrimpTreatmentTypeController(ShrimpTreatmentTypeService service) {
        this.service = service;
    }

    @GetMapping("list")
    @Operation(summary = "Get paginated list of shrimp treatment types")
    public ApiPaginatedList<ApiShrimpTreatmentType> getShrimpTreatmentTypeList(
            @Valid ApiPaginatedRequest request,
            @RequestParam(required = false, defaultValue = "ES") Language language) {
        return service.getShrimpTreatmentTypeList(request, language);
    }

    @GetMapping("list/active")
    @Operation(summary = "Get list of active shrimp treatment types")
    public ApiResponse<List<ApiShrimpTreatmentType>> getActiveShrimpTreatmentTypes(
            @RequestParam(required = false, defaultValue = "ES") Language language) {
        return new ApiResponse<>(service.getActiveShrimpTreatmentTypes(language));
    }

    @GetMapping("{id}")
    @Operation(summary = "Get a shrimp treatment type by ID")
    public ApiResponse<ApiShrimpTreatmentType> getShrimpTreatmentType(
            @Valid @Parameter(description = "Treatment type ID", required = true) @PathVariable("id") Long id,
            @RequestParam(required = false, defaultValue = "ES") Language language) throws ApiException {
        return new ApiResponse<>(service.getShrimpTreatmentType(id, language));
    }

    @PutMapping
    @Operation(summary = "Create or update a shrimp treatment type")
    public ApiResponse<ApiBaseEntity> createOrUpdateShrimpTreatmentType(
            @AuthenticationPrincipal CustomUserDetails authUser,
            @Valid @RequestBody ApiShrimpTreatmentType request) throws ApiException {
        return new ApiResponse<>(service.createOrUpdateShrimpTreatmentType(authUser, request));
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Delete a shrimp treatment type")
    public ApiResponse<ApiBaseEntity> deleteShrimpTreatmentType(
            @Valid @Parameter(description = "Treatment type ID", required = true) @PathVariable("id") Long id) throws ApiException {
        return new ApiResponse<>(service.deleteShrimpTreatmentType(id));
    }
}
