package com.abelium.inatrace.components.integration.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collections;
import java.util.List;

@Schema(description = "Deterministic paginated response wrapper")
public class ApiIntegrationPage<T> {

    @JsonProperty("count")
    @Schema(description = "Total number of matching records", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private long count;

    @JsonProperty("offset")
    @Schema(description = "Zero-based page offset", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private int offset;

    @JsonProperty("limit")
    @Schema(description = "Effective page size", minimum = "1", maximum = "500", requiredMode = Schema.RequiredMode.REQUIRED)
    private int limit;

    @JsonProperty("items")
    @Schema(description = "Page records ordered by ascending ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<T> items;

    public ApiIntegrationPage() {
        this.items = Collections.emptyList();
    }

    public ApiIntegrationPage(long count, int offset, int limit, List<T> items) {
        this.count = count;
        this.offset = offset;
        this.limit = limit;
        this.items = items != null ? items : Collections.emptyList();
    }

    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }

    public int getOffset() { return offset; }
    public void setOffset(int offset) { this.offset = offset; }

    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }

    public List<T> getItems() { return items; }
    public void setItems(List<T> items) { this.items = items; }
}
