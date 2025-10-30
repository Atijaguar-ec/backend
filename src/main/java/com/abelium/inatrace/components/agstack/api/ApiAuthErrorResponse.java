package com.abelium.inatrace.components.agstack.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ApiAuthErrorResponse {

    @JsonProperty("message")
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
