package com.abelium.inatrace.components.integration.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public class ApiDeletionIntegration {

    @JsonProperty("entity")
    private String entity;

    @JsonProperty("id")
    private Long id;

    @JsonProperty("deleted_at")
    private Instant deletedAt;

    @JsonProperty("reason")
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
