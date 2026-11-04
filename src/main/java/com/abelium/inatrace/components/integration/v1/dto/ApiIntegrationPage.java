package com.abelium.inatrace.components.integration.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;

public class ApiIntegrationPage<T> {

    @JsonProperty("count")
    private long count;

    @JsonProperty("offset")
    private int offset;

    @JsonProperty("limit")
    private int limit;

    @JsonProperty("items")
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
