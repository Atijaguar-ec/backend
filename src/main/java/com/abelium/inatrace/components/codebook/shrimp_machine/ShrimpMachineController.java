package com.abelium.inatrace.components.codebook.shrimp_machine;

import com.abelium.inatrace.api.ApiBaseEntity;
import com.abelium.inatrace.api.ApiPaginatedList;
import com.abelium.inatrace.api.ApiPaginatedRequest;
import com.abelium.inatrace.api.ApiResponse;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.codebook.shrimp_machine.api.ApiShrimpMachine;
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
 * REST controller for ShrimpMachine catalog.
 */
@RestController
@RequestMapping("/chain/shrimp-machine")
@Tag(name = "Shrimp Machine", description = "Shrimp processing machine catalog operations")
public class ShrimpMachineController {

    private final ShrimpMachineService service;

    @Autowired
    public ShrimpMachineController(ShrimpMachineService service) {
        this.service = service;
    }

    @GetMapping("list")
    @Operation(summary = "Get paginated list of shrimp machines")
    public ApiPaginatedList<ApiShrimpMachine> getShrimpMachineList(
            @Valid ApiPaginatedRequest request,
            @RequestParam(required = false, defaultValue = "ES") Language language) {
        return service.getShrimpMachineList(request, language);
    }

    @GetMapping("list/active")
    @Operation(summary = "Get list of active shrimp machines")
    public ApiResponse<List<ApiShrimpMachine>> getActiveShrimpMachines(
            @RequestParam(required = false, defaultValue = "ES") Language language) {
        return new ApiResponse<>(service.getActiveShrimpMachines(language));
    }

    @GetMapping("{id}")
    @Operation(summary = "Get a shrimp machine by ID")
    public ApiResponse<ApiShrimpMachine> getShrimpMachine(
            @Valid @Parameter(description = "Machine ID", required = true) @PathVariable("id") Long id,
            @RequestParam(required = false, defaultValue = "ES") Language language) throws ApiException {
        return new ApiResponse<>(service.getShrimpMachine(id, language));
    }

    @PutMapping
    @Operation(summary = "Create or update a shrimp machine")
    public ApiResponse<ApiBaseEntity> createOrUpdateShrimpMachine(
            @AuthenticationPrincipal CustomUserDetails authUser,
            @Valid @RequestBody ApiShrimpMachine request) throws ApiException {
        return new ApiResponse<>(service.createOrUpdateShrimpMachine(authUser, request));
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Delete a shrimp machine")
    public ApiResponse<ApiBaseEntity> deleteShrimpMachine(
            @Valid @Parameter(description = "Machine ID", required = true) @PathVariable("id") Long id) throws ApiException {
        return new ApiResponse<>(service.deleteShrimpMachine(id));
    }
}
