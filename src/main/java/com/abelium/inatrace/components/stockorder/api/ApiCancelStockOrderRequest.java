package com.abelium.inatrace.components.stockorder.api;

import io.swagger.v3.oas.annotations.media.Schema;

public class ApiCancelStockOrderRequest {

    @Schema(description = "Cancellation reason")
    private String reason;

    public ApiCancelStockOrderRequest() {
    }

    public ApiCancelStockOrderRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
