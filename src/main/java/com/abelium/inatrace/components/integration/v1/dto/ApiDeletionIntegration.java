package com.abelium.inatrace.components.integration.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Logical order cancellation or physical entity deletion event")
public class ApiDeletionIntegration {

    @JsonProperty("entity")
    @Schema(description = "Affected entity type", allowableValues = {"farmer", "plot", "delivery", "shipment"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String entity;

    @JsonProperty("id")
    @Schema(description = "Deleted or canceled record ID in INATrace", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @JsonProperty("deleted_at")
    @Schema(description = "UTC timestamp of the cancellation or deletion (RFC 3339)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Instant deletedAt;

    @JsonProperty("reason")
    @Schema(description = "Recorded cancellation or deletion reason", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;

    public ApiDeletionIntegration() {}

    public ApiDeletionIntegration(String entity, Long id, Instant deletedAt, String reason) {
        this.entity = entity;
        this.id = id;
        this.deletedAt = deletedAt;
        this.reason = reason;
    }

    public String getEntity() { return entity; }
    public void setEntity(String entity) { this.entity = entity; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
