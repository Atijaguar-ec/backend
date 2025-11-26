package com.abelium.inatrace.components.fieldinspection;

import com.abelium.inatrace.api.ApiDefaultResponse;
import com.abelium.inatrace.api.ApiResponse;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.fieldinspection.api.ApiFieldInspection;
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
 * REST controller for FieldInspection operations.
 * Handles field inspections performed at shrimp farms (camaroneras).
 * 
 * @author INATrace Team
 */
@RestController
@RequestMapping("/chain/field-inspection")
@Tag(name = "Field Inspections", description = "Field inspection management endpoints for shrimp sensorial tests")
public class FieldInspectionController {

    private final FieldInspectionService fieldInspectionService;

    @Autowired
    public FieldInspectionController(FieldInspectionService fieldInspectionService) {
        this.fieldInspectionService = fieldInspectionService;
    }

    /**
     * List all available field inspections for a company.
     * Available inspections are those not yet linked to a destination stock order.
     */
    @GetMapping("available/company/{companyId}")
    @Operation(summary = "List available field inspections for a company",
               description = "Returns field inspections that are not yet linked to a packing plant delivery")
    public ApiResponse<List<ApiFieldInspection>> getAvailableForCompany(
            @AuthenticationPrincipal CustomUserDetails authUser,
            @Valid @Parameter(description = "Company ID", required = true) 
            @PathVariable("companyId") Long companyId,
            @Parameter(description = "Only return inspections where purchase is recommended") 
            @RequestParam(value = "onlyRecommended", defaultValue = "false") boolean onlyRecommended,
            @RequestHeader(value = "language", defaultValue = "ES", required = false) Language language
    ) throws ApiException {

        List<ApiFieldInspection> inspections = fieldInspectionService.getAvailableForCompany(
            companyId, onlyRecommended, authUser);
        return new ApiResponse<>(inspections);
    }

    /**
     * Get a specific field inspection by ID.
     */
    @GetMapping("{id}")
    @Operation(summary = "Get field inspection by ID")
    public ApiResponse<ApiFieldInspection> getById(
            @AuthenticationPrincipal CustomUserDetails authUser,
            @Valid @Parameter(description = "Field inspection ID", required = true) 
            @PathVariable("id") Long id
    ) throws ApiException {

        ApiFieldInspection inspection = fieldInspectionService.getById(id, authUser);
        return new ApiResponse<>(inspection);
    }

    /**
     * Mark a field inspection as used by linking it to a destination stock order.
     * This is called when a delivery at the packing plant is linked to a prior field inspection.
     */
    @PostMapping("{id}/mark-used")
    @Operation(summary = "Mark field inspection as used",
               description = "Links a field inspection to a destination stock order at the packing plant")
    public ApiDefaultResponse markInspectionUsed(
            @AuthenticationPrincipal CustomUserDetails authUser,
            @Valid @Parameter(description = "Field inspection ID", required = true) 
            @PathVariable("id") Long inspectionId,
            @Valid @Parameter(description = "Destination StockOrder ID at packing plant", required = true) 
            @RequestParam("destinationStockOrderId") Long destinationStockOrderId
    ) throws ApiException {

        fieldInspectionService.markInspectionUsed(inspectionId, destinationStockOrderId, authUser);
        return new ApiDefaultResponse();
    }
}
