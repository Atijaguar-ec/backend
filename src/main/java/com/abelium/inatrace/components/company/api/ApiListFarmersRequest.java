package com.abelium.inatrace.components.company.api;

import com.abelium.inatrace.api.ApiPaginatedRequest;
import com.abelium.inatrace.types.UserCustomerStatus;
import io.swagger.v3.oas.annotations.Parameter;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;

@Validated
@ParameterObject
public class ApiListFarmersRequest extends ApiPaginatedRequest {

    @Parameter(description = "Name or surname")
    private String query;

    @Parameter(description = "Search by parameter")
    private String searchBy;

    @Parameter(description = "Filter by status within the organization. When omitted, user customers of every status are returned.")
    private UserCustomerStatus status;

    @Parameter(description = "When true, only user customers eligible for transactions (status ACTIVE) are returned. Takes precedence over 'status'.")
    private Boolean onlyAvailableForTransactions;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getSearchBy() {
        return searchBy;
    }

    public void setSearchBy(String searchBy) {
        this.searchBy = searchBy;
    }

    public UserCustomerStatus getStatus() {
        return status;
    }

    public void setStatus(UserCustomerStatus status) {
        this.status = status;
    }

    public Boolean getOnlyAvailableForTransactions() {
        return onlyAvailableForTransactions;
    }

    public void setOnlyAvailableForTransactions(Boolean onlyAvailableForTransactions) {
        this.onlyAvailableForTransactions = onlyAvailableForTransactions;
    }
}
