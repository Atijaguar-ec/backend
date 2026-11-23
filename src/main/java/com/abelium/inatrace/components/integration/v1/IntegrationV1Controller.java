package com.abelium.inatrace.components.integration.v1;

import com.abelium.inatrace.api.ApiDefaultResponse;
import com.abelium.inatrace.api.ApiResponse;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.integration.v1.dto.*;
import com.abelium.inatrace.components.integration.v1.services.IntegrationV1Service;
import com.abelium.inatrace.security.service.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/integration/v1")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Integration V1 - Mieru Cacao", description = "Read-only REST Integration API for Mieru Cacao platform")
@ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Invalid or missing query parameter (INVALID_REQUEST / REQUEST_BODY_ERROR)",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiDefaultResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "Missing, expired, or invalid OAuth 2.0 Bearer JWT token",
                content = @Content
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "Requested company_id is outside the credential's authorized scope (FORBIDDEN_TENANT_ACCESS)",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiDefaultResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "429",
                description = "Request rate limit exceeded",
                headers = @Header(name = "Retry-After", description = "Seconds (or HTTP-date) to wait before retrying", schema = @Schema(type = "string")),
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiDefaultResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "Unexpected server error",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiDefaultResponse.class))
        )
})
public class IntegrationV1Controller {

    private final IntegrationV1Service integrationService;

    @Autowired
    public IntegrationV1Controller(IntegrationV1Service integrationService) {
        this.integrationService = integrationService;
    }

    @GetMapping("/companies")
    @Operation(summary = "List authorized companies for the M2M client")
    public ApiResponse<List<ApiCompanyIntegration>> getCompanies(
            @AuthenticationPrincipal CustomUserDetails authUser) throws ApiException {
        return ApiResponse.ok(integrationService.getCompanies(authUser));
    }

    @GetMapping("/facilities")
    @Operation(summary = "List collection centers and storage facilities")
    public ApiResponse<List<ApiFacilityIntegration>> getFacilities(
            @Parameter(description = "Optional company ID filter")
            @RequestParam(value = "company_id", required = false) Long companyId,
            @Parameter(description = "Incremental synchronization filter (ISO 8601 / RFC 3339 UTC)")
            @RequestParam(value = "updated_since", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant updatedSince,
            @AuthenticationPrincipal CustomUserDetails authUser) throws ApiException {
        return ApiResponse.ok(integrationService.getFacilities(authUser, companyId, updatedSince));
    }

    @GetMapping("/reference-data")
    @Operation(summary = "Get dynamic catalogs, varieties, units and conversion factors")
    public ApiResponse<ApiReferenceDataIntegration> getReferenceData() {
        return ApiResponse.ok(integrationService.getReferenceData());
    }

    @GetMapping("/farmers")
    @Operation(summary = "List farmers with farm and georeferenced plots")
    public ApiResponse<ApiIntegrationPage<ApiFarmerIntegration>> getFarmers(
            @Parameter(description = "Optional company ID filter")
            @RequestParam(value = "company_id", required = false) Long companyId,
            @Parameter(description = "Incremental synchronization filter (ISO 8601 / RFC 3339 UTC)")
            @RequestParam(value = "updated_since", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant updatedSince,
            @Parameter(description = "Page size (default 50, min 1, max 500)", schema = @Schema(type = "integer", defaultValue = "50", minimum = "1", maximum = "500"))
            @RequestParam(value = "limit", defaultValue = "50") Integer limit,
            @Parameter(description = "Zero-based page offset (default 0)", schema = @Schema(type = "integer", defaultValue = "0", minimum = "0"))
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            @AuthenticationPrincipal CustomUserDetails authUser) throws ApiException {
        return ApiResponse.ok(integrationService.getFarmers(authUser, companyId, updatedSince, limit, offset));
    }

    @GetMapping("/farmers/{id}")
    @Operation(summary = "Get farmer details and plots by ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Farmer not found with the specified ID",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiDefaultResponse.class))
    )
    public ApiResponse<ApiFarmerIntegration> getFarmerById(
            @Parameter(description = "Farmer ID", required = true)
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails authUser) throws ApiException {
        return ApiResponse.ok(integrationService.getFarmerById(authUser, id));
    }

    @GetMapping("/deliveries")
    @Operation(summary = "List cocoa deliveries from farmers without financial pricing")
    public ApiResponse<ApiIntegrationPage<ApiDeliveryIntegration>> getDeliveries(
            @Parameter(description = "Optional company ID filter")
            @RequestParam(value = "company_id", required = false) Long companyId,
            @Parameter(description = "Optional facility ID filter")
            @RequestParam(value = "facility_id", required = false) Long facilityId,
            @Parameter(description = "Optional farmer ID filter")
            @RequestParam(value = "farmer_id", required = false) Long farmerId,
            @Parameter(description = "Delivery date from (ISO 8601 date YYYY-MM-DD)")
            @RequestParam(value = "delivery_date_from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deliveryDateFrom,
            @Parameter(description = "Delivery date to (ISO 8601 date YYYY-MM-DD)")
            @RequestParam(value = "delivery_date_to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deliveryDateTo,
            @Parameter(description = "Incremental synchronization filter (ISO 8601 / RFC 3339 UTC)")
            @RequestParam(value = "updated_since", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant updatedSince,
            @Parameter(description = "Page size (default 50, min 1, max 500)", schema = @Schema(type = "integer", defaultValue = "50", minimum = "1", maximum = "500"))
            @RequestParam(value = "limit", defaultValue = "50") Integer limit,
            @Parameter(description = "Zero-based page offset (default 0)", schema = @Schema(type = "integer", defaultValue = "0", minimum = "0"))
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            @AuthenticationPrincipal CustomUserDetails authUser) throws ApiException {
        return ApiResponse.ok(integrationService.getDeliveries(
                authUser, companyId, facilityId, farmerId, deliveryDateFrom, deliveryDateTo, updatedSince, limit, offset));
    }

    @GetMapping("/deliveries/{id}")
    @Operation(summary = "Get single delivery details by ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Delivery not found with the specified ID",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiDefaultResponse.class))
    )
    public ApiResponse<ApiDeliveryIntegration> getDeliveryById(
            @Parameter(description = "Delivery ID", required = true)
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails authUser) throws ApiException {
        return ApiResponse.ok(integrationService.getDeliveryById(authUser, id));
    }

    @GetMapping("/shipments")
    @Operation(summary = "List export lots and shipments")
    public ApiResponse<ApiIntegrationPage<ApiShipmentIntegration>> getShipments(
            @Parameter(description = "Optional company ID filter")
            @RequestParam(value = "company_id", required = false) Long companyId,
            @Parameter(description = "Case-insensitive exact lot code search (internalLotNumber, qrCodeTag, orderId)")
            @RequestParam(value = "lot_code", required = false) String lotCode,
            @Parameter(description = "Shipment date from (ISO 8601 date YYYY-MM-DD)")
            @RequestParam(value = "shipment_date_from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate shipmentDateFrom,
            @Parameter(description = "Shipment date to (ISO 8601 date YYYY-MM-DD)")
            @RequestParam(value = "shipment_date_to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate shipmentDateTo,
            @Parameter(description = "Incremental synchronization filter (ISO 8601 / RFC 3339 UTC)")
            @RequestParam(value = "updated_since", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant updatedSince,
            @Parameter(description = "Page size (default 50, min 1, max 500)", schema = @Schema(type = "integer", defaultValue = "50", minimum = "1", maximum = "500"))
            @RequestParam(value = "limit", defaultValue = "50") Integer limit,
            @Parameter(description = "Zero-based page offset (default 0)", schema = @Schema(type = "integer", defaultValue = "0", minimum = "0"))
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            @AuthenticationPrincipal CustomUserDetails authUser) throws ApiException {
        return ApiResponse.ok(integrationService.getShipments(
                authUser, companyId, lotCode, shipmentDateFrom, shipmentDateTo, updatedSince, limit, offset));
    }

    @GetMapping("/shipments/{id}/composition")
    @Operation(summary = "Backward DAG trace of source deliveries composing the shipment lot")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Shipment not found or specified ID corresponds to a primary purchase order",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiDefaultResponse.class))
    )
    public ApiResponse<ApiCompositionIntegration> getShipmentComposition(
            @Parameter(description = "Shipment ID", required = true)
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails authUser) throws ApiException {
        return ApiResponse.ok(integrationService.getShipmentComposition(authUser, id));
    }

    @GetMapping("/deletions")
    @Operation(summary = "Report logical cancellations and physical deletions")
    public ApiResponse<List<ApiDeletionIntegration>> getDeletions(
            @Parameter(description = "Optional company ID filter")
            @RequestParam(value = "company_id", required = false) Long companyId,
            @Parameter(description = "Incremental sync cutoff timestamp (ISO 8601 / RFC 3339 UTC), required", required = true)
            @RequestParam(value = "updated_since", required = true)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant updatedSince,
            @Parameter(description = "Optional entity filter", schema = @Schema(type = "string", allowableValues = {"farmer", "plot", "delivery", "shipment"}))
            @RequestParam(value = "entity", required = false) String entity,
            @AuthenticationPrincipal CustomUserDetails authUser) throws ApiException {
        return ApiResponse.ok(integrationService.getDeletions(authUser, companyId, updatedSince, entity));
    }
}
