package com.abelium.inatrace.components.groupstockorder;

import com.abelium.inatrace.api.ApiPaginatedRequest;
import com.abelium.inatrace.api.ApiPaginatedResponse;
import com.abelium.inatrace.components.groupstockorder.api.ApiGroupStockOrder;
import com.abelium.inatrace.types.Language;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/chain/group-stock-order")
public class GroupStockOrderController {

    private final GroupStockOrderService groupStockOrderService;

    @Autowired
    public GroupStockOrderController(GroupStockOrderService groupStockOrderService) {
        this.groupStockOrderService = groupStockOrderService;
    }

    @GetMapping("/list/facility/{facilityId}")
    @Operation(summary = "Get a paginated list of grouped stock orders.")
    public ApiPaginatedResponse<ApiGroupStockOrder> getGroupedStockOrderList(
            @Valid ApiPaginatedRequest request,
            @Valid @Parameter(description = "Facility ID", required = true) @PathVariable Long facilityId,
            @Valid @Parameter(description = "Available orders only") @RequestParam(value = "availableOnly", required = false) Boolean availableOnly,
            @Valid @Parameter(description = "Is purchase orders only") @RequestParam(value = "isPurchaseOrderOnly", required = false) Boolean isPurchaseOrderOnly,
            @Valid @Parameter(description = "Semi-product ID") @RequestParam(value = "semiProductId", required = false) Long semiProductId,
            @RequestHeader(value = "language", defaultValue = "ES", required = false) Language language
    ) {
        return new ApiPaginatedResponse<>(this.groupStockOrderService.getGroupedStockOrderList(
                request,
                new GroupStockOrderQueryRequest(
                        facilityId,
                        availableOnly,
                        isPurchaseOrderOnly,
                        semiProductId
                ),
                language
        ));
    }

    @Operation(summary = "Export grouped stock orders to Excel for a facility (last year)")
    public ResponseEntity<Resource> exportGroupedStockOrdersExcel(
            @Valid @Parameter(description = "Facility ID", required = true) @PathVariable Long facilityId,
            @RequestHeader(value = "language", defaultValue = "ES", required = false) Language language
    ) throws Exception {

        ByteArrayInputStream excelStream = this.groupStockOrderService.exportGroupedStockOrdersToExcel(facilityId, language);

        InputStreamResource resource = new InputStreamResource(excelStream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=grouped-stock-orders.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    @GetMapping(value = "/export/company/{companyId}", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @Operation(summary = "Export grouped stock orders to Excel for a company (last year)")
    public ResponseEntity<Resource> exportGroupedStockOrdersExcelByCompany(
            @Valid @Parameter(description = "Company ID", required = true) @PathVariable Long companyId,
            @RequestHeader(value = "language", defaultValue = "ES", required = false) Language language
    ) throws Exception {

        ByteArrayInputStream excelStream = this.groupStockOrderService.exportGroupedStockOrdersToExcelByCompany(companyId, language);

        InputStreamResource resource = new InputStreamResource(excelStream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=grouped-stock-orders.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }
}
