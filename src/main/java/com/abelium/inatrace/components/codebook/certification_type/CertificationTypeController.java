package com.abelium.inatrace.components.codebook.certification_type;

import com.abelium.inatrace.api.*;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.codebook.certification_type.api.ApiCertificationType;
import com.abelium.inatrace.security.service.CustomUserDetails;
import com.abelium.inatrace.types.Language;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for certification type entity.
 *
 * @author INATrace Development Team
 */
@RestController
@RequestMapping("/chain/certification-type")
public class CertificationTypeController {

    private final CertificationTypeService certificationTypeService;

    @Autowired
    public CertificationTypeController(CertificationTypeService certificationTypeService) {
        this.certificationTypeService = certificationTypeService;
    }

    @GetMapping("list")
    @Operation(summary = "Get a paginated list of certification types.")
    public ApiPaginatedResponse<ApiCertificationType> getCertificationTypeList(
            @Valid ApiPaginatedRequest request,
            @RequestHeader(value = "language", defaultValue = "ES", required = false) Language language) {

        return new ApiPaginatedResponse<>(certificationTypeService.getCertificationTypeList(request, language));
    }

    @GetMapping("{id}")
    @Operation(summary = "Get a single certification type with the provided ID.")
    public ApiResponse<ApiCertificationType> getCertificationType(
            @Valid @Parameter(description = "Certification type ID", required = true) @PathVariable("id") Long id,
            @RequestHeader(value = "language", defaultValue = "ES", required = false) Language language) throws ApiException {

        return new ApiResponse<>(certificationTypeService.getCertificationType(id, language));
    }

    @PutMapping
    @PreAuthorize("hasAnyAuthority('SYSTEM_ADMIN', 'REGIONAL_ADMIN')")
    @Operation(summary = "Create or update certification type. If ID is provided, the entity with the provided ID is updated.")
    public ApiResponse<ApiBaseEntity> createOrUpdateCertificationType(
            @AuthenticationPrincipal CustomUserDetails authUser,
            @Valid @RequestBody ApiCertificationType apiCertificationType) throws ApiException {

        return new ApiResponse<>(
                certificationTypeService.createOrUpdateCertificationType(authUser, apiCertificationType));
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    @Operation(summary = "Deletes a certification type with the provided ID.")
    public ApiDefaultResponse deleteCertificationType(
            @Valid @Parameter(description = "Certification type ID", required = true) @PathVariable("id") Long id) throws ApiException {

        certificationTypeService.deleteCertificationType(id);
        return new ApiDefaultResponse();
    }
}
