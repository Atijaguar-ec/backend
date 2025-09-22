package com.abelium.inatrace.db.entities.processingaction;

import com.abelium.inatrace.db.base.TimestampEntity;
import com.abelium.inatrace.db.entities.company.Company;
import jakarta.persistence.*;
import java.util.Objects;

/**
 * Company-specific configuration for processing actions.
 * Allows companies to enable/disable and override order of global processing actions.
 */
@Entity
@Table(name = "CompanyProcessingAction", 
       uniqueConstraints = @UniqueConstraint(
           name = "uk_company_processing_action_company_action",
           columnNames = {"company_id", "processing_action_id"}
       ),
       indexes = {
           @Index(name = "idx_company_processing_action_company_enabled", 
                  columnList = "company_id, enabled, order_override"),
           @Index(name = "idx_company_processing_action_processing_action", 
                  columnList = "processing_action_id")
       })
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
    private Boolean enabled = Boolean.TRUE;

    @Column(name = "order_override")
    private Integer orderOverride;

    @Column(name = "alias_label")
    private String aliasLabel;

    public CompanyProcessingAction() {
        super();
    }

    public CompanyProcessingAction(Company company, ProcessingAction processingAction) {
        this();
        this.company = Objects.requireNonNull(company, "Company cannot be null");
        this.processingAction = Objects.requireNonNull(processingAction, "ProcessingAction cannot be null");
        this.enabled = Boolean.TRUE;
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
        this.company = Objects.requireNonNull(company, "Company cannot be null");
    }

    public ProcessingAction getProcessingAction() {
        return processingAction;
    }

    public void setProcessingAction(ProcessingAction processingAction) {
        this.processingAction = Objects.requireNonNull(processingAction, "ProcessingAction cannot be null");
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled != null ? enabled : Boolean.TRUE;
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
        this.aliasLabel = aliasLabel != null ? aliasLabel.trim() : null;
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
            return aliasLabel.trim();
        }
        // Note: ProcessingAction name comes from translations, this is a fallback
        return processingAction != null ? processingAction.getPrefix() : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompanyProcessingAction that = (CompanyProcessingAction) o;
        return Objects.equals(company, that.company) &&
               Objects.equals(processingAction, that.processingAction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(company, processingAction);
    }

    @Override
    public String toString() {
        return "CompanyProcessingAction{" +
               "id=" + getId() +
               ", company=" + (company != null ? company.getId() : null) +
               ", processingAction=" + (processingAction != null ? processingAction.getId() : null) +
               ", enabled=" + enabled +
               ", orderOverride=" + orderOverride +
               '}';
    }
}
