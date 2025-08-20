package com.abelium.inatrace.db.entities.processingaction;

import com.abelium.inatrace.db.base.TimestampEntity;
import com.abelium.inatrace.db.entities.company.Company;
import jakarta.persistence.*;

/**
 * Company-specific configuration for processing actions.
 * Allows companies to enable/disable and override order of global processing actions.
 */
@Entity
@Table(name = "company_processing_action")
public class CompanyProcessingAction extends TimestampEntity {

    @Version
    private Long entityVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processing_action_id", nullable = false)
    private ProcessingAction processingAction;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(name = "order_override")
    private Integer orderOverride;

    @Column(name = "alias_label")
    private String aliasLabel;

    public CompanyProcessingAction() {
        super();
    }

    public CompanyProcessingAction(Company company, ProcessingAction processingAction) {
        this();
        this.company = company;
        this.processingAction = processingAction;
        this.enabled = true;
    }

    public Long getEntityVersion() {
        return entityVersion;
    }

    public void setEntityVersion(Long entityVersion) {
        this.entityVersion = entityVersion;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public ProcessingAction getProcessingAction() {
        return processingAction;
    }

    public void setProcessingAction(ProcessingAction processingAction) {
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

    /**
     * Gets the effective order for this processing action.
     * Uses orderOverride if set, otherwise falls back to the global sortOrder.
     * 
     * @return effective order value
     */
    public Long getEffectiveOrder() {
        if (orderOverride != null) {
            return orderOverride.longValue();
        }
        return processingAction != null ? processingAction.getSortOrder() : 0L;
    }

    /**
     * Gets the effective label for this processing action.
     * Uses aliasLabel if set, otherwise falls back to the processing action name.
     * 
     * @return effective label
     */
    public String getEffectiveLabel() {
        if (aliasLabel != null && !aliasLabel.trim().isEmpty()) {
            return aliasLabel;
        }
        // Note: ProcessingAction name comes from translations, this is a fallback
        return processingAction != null ? processingAction.getPrefix() : "";
    }
}
