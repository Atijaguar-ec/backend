package com.abelium.inatrace.components.codebook.shrimp_size_grade;

import com.abelium.inatrace.api.*;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.codebook.shrimp_size_grade.api.ApiShrimpSizeGrade;
import com.abelium.inatrace.db.enums.ShrimpSizeType;
import com.abelium.inatrace.security.service.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for shrimp size grade entity.
 *
 * @author INATrace Team
 */
@RestController
@RequestMapping("/chain/shrimp-size-grade")
public class ShrimpSizeGradeController {

    private final ShrimpSizeGradeService service;

    @Autowired
    public ShrimpSizeGradeController(ShrimpSizeGradeService service) {
        this.service = service;
    }

    @GetMapping("list")
    @Operation(summary = "Get a paginated list of shrimp size grades.")
    public ApiPaginatedResponse<ApiShrimpSizeGrade> getShrimpSizeGradeList(@Valid ApiPaginatedRequest request) {
        return new ApiPaginatedResponse<>(service.getShrimpSizeGradeList(request));
    }

    @GetMapping("list/active")
    @Operation(summary = "Get a list of active shrimp size grades.")
    public ApiResponse<List<ApiShrimpSizeGrade>> getActiveShrimpSizeGrades() {
        return new ApiResponse<>(service.getActiveShrimpSizeGrades());
    }

    @GetMapping("list/type/{sizeType}")
    @Operation(summary = "Get a list of active shrimp size grades by type (WHOLE or TAIL).")
    public ApiResponse<List<ApiShrimpSizeGrade>> getShrimpSizeGradesByType(
            @Valid @Parameter(description = "Size type: WHOLE or TAIL", required = true) @PathVariable("sizeType") ShrimpSizeType sizeType) {
        return new ApiResponse<>(service.getShrimpSizeGradesByType(sizeType));
    }

    @GetMapping("{id}")
    @Operation(summary = "Get a single shrimp size grade with the provided ID.")
    public ApiResponse<ApiShrimpSizeGrade> getShrimpSizeGrade(
            @Valid @Parameter(description = "Shrimp size grade ID", required = true) @PathVariable("id") Long id) throws ApiException {
        return new ApiResponse<>(service.getShrimpSizeGrade(id));
    }

    @PutMapping
    @PreAuthorize("hasAnyAuthority('SYSTEM_ADMIN', 'REGIONAL_ADMIN')")
    @Operation(summary = "Create or update shrimp size grade. If ID is provided, the entity with the provided ID is updated.")
    public ApiResponse<ApiBaseEntity> createOrUpdateShrimpSizeGrade(
            @AuthenticationPrincipal CustomUserDetails authUser,
            @Valid @RequestBody ApiShrimpSizeGrade api) throws ApiException {
        return new ApiResponse<>(service.createOrUpdateShrimpSizeGrade(authUser, api));
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    @Operation(summary = "Deletes a shrimp size grade with the provided ID.")
    public ApiDefaultResponse deleteShrimpSizeGrade(
            @Valid @Parameter(description = "Shrimp size grade ID", required = true) @PathVariable("id") Long id) throws ApiException {
        service.deleteShrimpSizeGrade(id);
        return new ApiDefaultResponse();
    }
}
