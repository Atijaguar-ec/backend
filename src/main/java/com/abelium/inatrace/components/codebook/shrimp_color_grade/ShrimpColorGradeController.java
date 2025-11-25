package com.abelium.inatrace.components.codebook.shrimp_color_grade;

import com.abelium.inatrace.api.*;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.codebook.shrimp_color_grade.api.ApiShrimpColorGrade;
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
 * REST controller for shrimp color grade entity.
 *
 * @author INATrace Team
 */
@RestController
@RequestMapping("/chain/shrimp-color-grade")
public class ShrimpColorGradeController {

    private final ShrimpColorGradeService service;

    @Autowired
    public ShrimpColorGradeController(ShrimpColorGradeService service) {
        this.service = service;
    }

    @GetMapping("list")
    @Operation(summary = "Get a paginated list of shrimp color grades.")
    public ApiPaginatedResponse<ApiShrimpColorGrade> getShrimpColorGradeList(@Valid ApiPaginatedRequest request) {
        return new ApiPaginatedResponse<>(service.getShrimpColorGradeList(request));
    }

    @GetMapping("list/active")
    @Operation(summary = "Get a list of active shrimp color grades.")
    public ApiResponse<List<ApiShrimpColorGrade>> getActiveShrimpColorGrades() {
        return new ApiResponse<>(service.getActiveShrimpColorGrades());
    }

    @GetMapping("{id}")
    @Operation(summary = "Get a single shrimp color grade with the provided ID.")
    public ApiResponse<ApiShrimpColorGrade> getShrimpColorGrade(
            @Valid @Parameter(description = "Shrimp color grade ID", required = true) @PathVariable("id") Long id) throws ApiException {
        return new ApiResponse<>(service.getShrimpColorGrade(id));
    }

    @PutMapping
    @PreAuthorize("hasAnyAuthority('SYSTEM_ADMIN', 'REGIONAL_ADMIN')")
    @Operation(summary = "Create or update shrimp color grade. If ID is provided, the entity with the provided ID is updated.")
    public ApiResponse<ApiBaseEntity> createOrUpdateShrimpColorGrade(
            @AuthenticationPrincipal CustomUserDetails authUser,
            @Valid @RequestBody ApiShrimpColorGrade api) throws ApiException {
        return new ApiResponse<>(service.createOrUpdateShrimpColorGrade(authUser, api));
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    @Operation(summary = "Deletes a shrimp color grade with the provided ID.")
    public ApiDefaultResponse deleteShrimpColorGrade(
            @Valid @Parameter(description = "Shrimp color grade ID", required = true) @PathVariable("id") Long id) throws ApiException {
        service.deleteShrimpColorGrade(id);
        return new ApiDefaultResponse();
    }
}
