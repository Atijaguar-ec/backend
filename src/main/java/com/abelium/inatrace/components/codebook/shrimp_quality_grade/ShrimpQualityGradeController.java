package com.abelium.inatrace.components.codebook.shrimp_quality_grade;

import com.abelium.inatrace.api.ApiBaseEntity;
import com.abelium.inatrace.api.ApiPaginatedList;
import com.abelium.inatrace.api.ApiPaginatedRequest;
import com.abelium.inatrace.api.ApiResponse;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.codebook.shrimp_quality_grade.api.ApiShrimpQualityGrade;
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
 * REST controller for ShrimpQualityGrade catalog.
 */
@RestController
@RequestMapping("/chain/shrimp-quality-grade")
@Tag(name = "Shrimp Quality Grade", description = "Shrimp quality grade catalog operations (A, B, C)")
public class ShrimpQualityGradeController {

    private final ShrimpQualityGradeService service;

    @Autowired
    public ShrimpQualityGradeController(ShrimpQualityGradeService service) {
        this.service = service;
    }

    @GetMapping("list")
    @Operation(summary = "Get paginated list of shrimp quality grades")
    public ApiPaginatedList<ApiShrimpQualityGrade> getShrimpQualityGradeList(
            @Valid ApiPaginatedRequest request,
            @RequestParam(required = false, defaultValue = "ES") Language language) {
        return service.getShrimpQualityGradeList(request, language);
    }

    @GetMapping("list/active")
    @Operation(summary = "Get list of active shrimp quality grades")
    public ApiResponse<List<ApiShrimpQualityGrade>> getActiveShrimpQualityGrades(
            @RequestParam(required = false, defaultValue = "ES") Language language) {
        return new ApiResponse<>(service.getActiveShrimpQualityGrades(language));
    }

    @GetMapping("{id}")
    @Operation(summary = "Get a shrimp quality grade by ID")
    public ApiResponse<ApiShrimpQualityGrade> getShrimpQualityGrade(
            @Valid @Parameter(description = "Quality grade ID", required = true) @PathVariable("id") Long id,
            @RequestParam(required = false, defaultValue = "ES") Language language) throws ApiException {
        return new ApiResponse<>(service.getShrimpQualityGrade(id, language));
    }

    @PutMapping
    @Operation(summary = "Create or update a shrimp quality grade")
    public ApiResponse<ApiBaseEntity> createOrUpdateShrimpQualityGrade(
            @AuthenticationPrincipal CustomUserDetails authUser,
            @Valid @RequestBody ApiShrimpQualityGrade request) throws ApiException {
        return new ApiResponse<>(service.createOrUpdateShrimpQualityGrade(authUser, request));
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Delete a shrimp quality grade")
    public ApiResponse<ApiBaseEntity> deleteShrimpQualityGrade(
            @Valid @Parameter(description = "Quality grade ID", required = true) @PathVariable("id") Long id) throws ApiException {
        return new ApiResponse<>(service.deleteShrimpQualityGrade(id));
    }
}
