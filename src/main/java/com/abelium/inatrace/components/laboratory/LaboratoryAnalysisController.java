package com.abelium.inatrace.components.laboratory;

import com.abelium.inatrace.api.ApiDefaultResponse;
import com.abelium.inatrace.api.ApiResponse;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.laboratory.api.ApiLaboratoryAnalysis;
import com.abelium.inatrace.security.service.CustomUserDetails;
import com.abelium.inatrace.types.Language;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST controller for LaboratoryAnalysis.
 */
@RestController
@RequestMapping("/chain/laboratory-analysis")
@Tag(name = "Laboratory Analyses", description = "Laboratory analysis management endpoints")
public class LaboratoryAnalysisController {

    private final LaboratoryAnalysisService laboratoryAnalysisService;

    @Autowired
    public LaboratoryAnalysisController(LaboratoryAnalysisService laboratoryAnalysisService) {
        this.laboratoryAnalysisService = laboratoryAnalysisService;
    }

    @GetMapping("approved-available/company/{companyId}")
    @Operation(summary = "List approved and unused laboratory analyses for a company")
    public ApiResponse<List<ApiLaboratoryAnalysis>> getApprovedAvailableForCompany(
            @AuthenticationPrincipal CustomUserDetails authUser,
            @Valid @Parameter(description = "Company ID", required = true) @PathVariable("companyId") Long companyId,
            @RequestHeader(value = "language", defaultValue = "ES", required = false) Language language
    ) throws ApiException {

        List<ApiLaboratoryAnalysis> analyses = laboratoryAnalysisService.getApprovedAvailableForCompany(companyId, authUser);
        return new ApiResponse<>(analyses);
    }

    @PostMapping("{id}/mark-used")
    @Operation(summary = "Mark laboratory analysis as used by linking it to a destination stock order")
    public ApiDefaultResponse markAnalysisUsed(
            @AuthenticationPrincipal CustomUserDetails authUser,
            @Valid @Parameter(description = "Laboratory analysis ID", required = true) @PathVariable("id") Long analysisId,
            @Valid @Parameter(description = "Destination StockOrder ID", required = true) @RequestParam("destinationStockOrderId") Long destinationStockOrderId
    ) throws ApiException {

        laboratoryAnalysisService.markAnalysisUsed(analysisId, destinationStockOrderId, authUser);
        return new ApiDefaultResponse();
    }
}
