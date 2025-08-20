package com.abelium.inatrace.components.processingaction.api;

import com.abelium.inatrace.api.ApiBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * API model for company-specific processing action configuration.
 * Represents the effective processing action available to a company with overrides.
 */
public class ApiCompanyProcessingAction extends ApiBaseEntity {

    @Schema(description = "The base processing action")
    private ApiProcessingAction processingAction;

    @Schema(description = "Whether this processing action is enabled for the company")
    private Boolean enabled;

    @Schema(description = "Company-specific order override (null uses global sortOrder)")
    private Integer orderOverride;

    @Schema(description = "Company-specific alias label (null uses processing action name)")
    private String aliasLabel;

    @Schema(description = "Effective order (computed from orderOverride or global sortOrder)")
    private Long effectiveOrder;

    @Schema(description = "Effective label (computed from aliasLabel or processing action name)")
    private String effectiveLabel;

    public ApiCompanyProcessingAction() {
        super();
    }

    public ApiProcessingAction getProcessingAction() {
        return processingAction;
    }

    public void setProcessingAction(ApiProcessingAction processingAction) {
        this.processingAction = processingAction;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getOrderOverride() {
        return orderOverride;
    }

    public void setOrderOverride(Integer orderOverride) {
        this.orderOverride = orderOverride;
    }

    public String getAliasLabel() {
        return aliasLabel;
    }

    public void setAliasLabel(String aliasLabel) {
        this.aliasLabel = aliasLabel;
    }

    public Long getEffectiveOrder() {
        return effectiveOrder;
    }

    public void setEffectiveOrder(Long effectiveOrder) {
        this.effectiveOrder = effectiveOrder;
    }

    public String getEffectiveLabel() {
        return effectiveLabel;
    }

    public void setEffectiveLabel(String effectiveLabel) {
        this.effectiveLabel = effectiveLabel;
    }
}
