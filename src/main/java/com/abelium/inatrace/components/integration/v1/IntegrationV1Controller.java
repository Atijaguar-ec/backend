package com.abelium.inatrace.components.integration.v1;

import com.abelium.inatrace.api.ApiResponse;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.integration.v1.dto.*;
import com.abelium.inatrace.components.integration.v1.services.IntegrationV1Service;
import com.abelium.inatrace.security.service.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping({"/api/integration/v1", "/integration/v1"})
@Tag(name = "Integration V1 - Mieru Cacao", description = "Read-only REST Integration API for Mieru Cacao platform")
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
            @Parameter(description = "Incremental synchronization filter (ISO 8601)")
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
            @Parameter(description = "Incremental synchronization filter (ISO 8601)")
            @RequestParam(value = "updated_since", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant updatedSince,
            @Parameter(description = "Page size (default 50, max 500)")
            @RequestParam(value = "limit", defaultValue = "50") Integer limit,
            @Parameter(description = "Page offset (default 0)")
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            @AuthenticationPrincipal CustomUserDetails authUser) throws ApiException {
        return ApiResponse.ok(integrationService.getFarmers(authUser, companyId, updatedSince, limit, offset));
    }

    @GetMapping("/farmers/{id}")
    @Operation(summary = "Get farmer details and plots by ID")
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
            @Parameter(description = "Incremental synchronization filter (ISO 8601)")
            @RequestParam(value = "updated_since", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant updatedSince,
            @Parameter(description = "Page size (default 50, max 500)")
            @RequestParam(value = "limit", defaultValue = "50") Integer limit,
            @Parameter(description = "Page offset (default 0)")
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            @AuthenticationPrincipal CustomUserDetails authUser) throws ApiException {
        return ApiResponse.ok(integrationService.getDeliveries(
                authUser, companyId, facilityId, farmerId, deliveryDateFrom, deliveryDateTo, updatedSince, limit, offset));
    }

    @GetMapping("/deliveries/{id}")
    @Operation(summary = "Get single delivery details by ID")
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
            @Parameter(description = "Lot code search (internalLotNumber, qrCodeTag, orderId)")
            @RequestParam(value = "lot_code", required = false) String lotCode,
            @Parameter(description = "Shipment date from (ISO 8601 date YYYY-MM-DD)")
            @RequestParam(value = "shipment_date_from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate shipmentDateFrom,
            @Parameter(description = "Shipment date to (ISO 8601 date YYYY-MM-DD)")
            @RequestParam(value = "shipment_date_to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate shipmentDateTo,
            @Parameter(description = "Incremental synchronization filter (ISO 8601)")
            @RequestParam(value = "updated_since", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant updatedSince,
            @Parameter(description = "Page size (default 50, max 500)")
            @RequestParam(value = "limit", defaultValue = "50") Integer limit,
            @Parameter(description = "Page offset (default 0)")
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            @AuthenticationPrincipal CustomUserDetails authUser) throws ApiException {
        return ApiResponse.ok(integrationService.getShipments(
                authUser, companyId, lotCode, shipmentDateFrom, shipmentDateTo, updatedSince, limit, offset));
    }

    @GetMapping("/shipments/{id}/composition")
    @Operation(summary = "Backward DAG trace of source deliveries composing the shipment lot")
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
            @Parameter(description = "Incremental sync timestamp (ISO 8601), required", required = true)
            @RequestParam(value = "updated_since", required = true)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant updatedSince,
            @Parameter(description = "Optional entity filter (farmer, plot, delivery, shipment)")
            @RequestParam(value = "entity", required = false) String entity,
            @AuthenticationPrincipal CustomUserDetails authUser) throws ApiException {
        return ApiResponse.ok(integrationService.getDeletions(authUser, companyId, updatedSince, entity));
    }
}
