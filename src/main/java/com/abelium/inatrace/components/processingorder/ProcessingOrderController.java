package com.abelium.inatrace.components.processingorder;

import com.abelium.inatrace.api.ApiBaseEntity;
import com.abelium.inatrace.api.ApiDefaultResponse;
import com.abelium.inatrace.api.ApiResponse;
import com.abelium.inatrace.api.ApiStatus;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.processingorder.api.ApiProcessingOrder;
import com.abelium.inatrace.security.service.CustomUserDetails;
import com.abelium.inatrace.types.Language;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("chain/processing-order")
public class ProcessingOrderController {

    private final ProcessingOrderService processingOrderService;
    private final ClassificationExcelService classificationExcelService;

    @Autowired
    public ProcessingOrderController(ProcessingOrderService processingOrderService,
                                    ClassificationExcelService classificationExcelService) {
        this.processingOrderService = processingOrderService;
        this.classificationExcelService = classificationExcelService;
    }

    @GetMapping("{id}")
    @Operation(summary ="Get a single processing order with the provided ID.")
    public ApiResponse<ApiProcessingOrder> getProcessingOrder(
            @Valid @Parameter(description = "ProcessingOrder ID", required = true) @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails authUser,
            @RequestHeader(value = "language", defaultValue = "ES", required = false) Language language) throws ApiException {

        return new ApiResponse<>(processingOrderService.getProcessingOrder(id, authUser, language));
    }

    @PutMapping
    @Operation(summary ="Create or update processing order. If the ID is provided, then the entity with the provided ID is updated.")
    public ApiResponse<ApiBaseEntity> createOrUpdateProcessingOrder(
            @Valid @RequestBody ApiProcessingOrder apiProcessingOrder,
            @AuthenticationPrincipal CustomUserDetails authUser,
            @RequestHeader(value = "language", defaultValue = "ES", required = false) Language language) throws ApiException {

        return new ApiResponse<>(processingOrderService.createOrUpdateProcessingOrder(apiProcessingOrder, authUser, language));
    }

    @DeleteMapping("{id}")
    @Operation(summary ="Deletes a processing order with the provided ID.")
    public ApiDefaultResponse deleteProcessingOrder(
            @Valid @Parameter(description = "ProcessingOrder ID", required = true) @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails authUser) throws ApiException {

        processingOrderService.deleteProcessingOrder(id, authUser);
        return new ApiDefaultResponse();
    }

    @GetMapping(value = "{id}/classification/liquidacion", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(summary = "Export classification batch 'Liquidación de Pesca' Excel for the provided stock order ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    content = @Content(schema = @Schema(type = "string", format = "binary"))
            )
    })
    public ResponseEntity<byte[]> downloadClassificationLiquidacion(
            @Valid @Parameter(description = "Stock Order ID (target output)", required = true) @PathVariable("id") Long stockOrderId,
            @AuthenticationPrincipal CustomUserDetails authUser) throws ApiException {

        byte[] response;
        try {
            response = processingOrderService.exportClassificationLiquidacion(stockOrderId, authUser);
        } catch (Exception e) {
            throw new ApiException(ApiStatus.ERROR, "Error while generating Excel file: " + e.getMessage());
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=Liquidacion_Pesca_" + stockOrderId + ".xlsx")
                .body(response);
    }

}
