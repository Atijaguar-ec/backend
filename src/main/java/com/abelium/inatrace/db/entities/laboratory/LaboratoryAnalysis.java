package com.abelium.inatrace.db.entities.laboratory;

import com.abelium.inatrace.db.base.TimestampEntity;
import com.abelium.inatrace.db.entities.common.User;
import com.abelium.inatrace.db.entities.stockorder.StockOrder;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Laboratory analysis entity for storing laboratory test results.
 * Supports multiple types of analysis (sensorial, microbiological, chemical, PCR).
 * 
 * @author INATrace Team
 */
@Entity
@Table(name = "LaboratoryAnalysis")
public class LaboratoryAnalysis extends TimestampEntity {

    /**
     * User who created this analysis
     */
    @ManyToOne
    @JoinColumn(name = "createdBy_id", nullable = false)
    private User createdBy;

    /**
     * User who last updated this analysis
     */
    @ManyToOne
    @JoinColumn(name = "updatedBy_id")
    private User updatedBy;

    /**
     * Stock order on which the laboratory analysis was performed (source).
     */
    @ManyToOne
    @JoinColumn(name = "stockOrder_id", nullable = false)
    private StockOrder stockOrder;

    /**
     * Destination stock order where this analysis is used/consumed (optional).
     * When set, the analysis should no longer appear as available for linking
     * to new deliveries.
     */
    @ManyToOne
    @JoinColumn(name = "destinationStockOrder_id")
    private StockOrder destinationStockOrder;

    /**
     * Type of analysis
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnalysisType analysisType;

    /**
     * Date when the analysis was performed
     */
    @Column
    private Instant analysisDate;

    // Sensorial Analysis - Raw State
    @Column
    private String sensorialRawOdor;

    @Column
    private String sensorialRawTaste;

    @Column
    private String sensorialRawColor;

    // Sensorial Analysis - Cooked State
    @Column
    private String sensorialCookedOdor;

    @Column
    private String sensorialCookedTaste;

    @Column
    private String sensorialCookedColor;

    // Quality observations
    @Column(columnDefinition = "LONGTEXT")
    private String qualityNotes;

    @Column
    private Boolean metabisulfiteLevelAcceptable;

    @Column
    private Boolean approvedForPurchase;

    // Getters and Setters

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

    public StockOrder getStockOrder() {
        return stockOrder;
    }

    public void setStockOrder(StockOrder stockOrder) {
        this.stockOrder = stockOrder;
    }

    public StockOrder getDestinationStockOrder() {
        return destinationStockOrder;
    }

    public void setDestinationStockOrder(StockOrder destinationStockOrder) {
        this.destinationStockOrder = destinationStockOrder;
    }

    public AnalysisType getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(AnalysisType analysisType) {
        this.analysisType = analysisType;
    }

    public Instant getAnalysisDate() {
        return analysisDate;
    }

    public void setAnalysisDate(Instant analysisDate) {
        this.analysisDate = analysisDate;
    }

    public String getSensorialRawOdor() {
        return sensorialRawOdor;
    }

    public void setSensorialRawOdor(String sensorialRawOdor) {
        this.sensorialRawOdor = sensorialRawOdor;
    }

    public String getSensorialRawTaste() {
        return sensorialRawTaste;
    }

    public void setSensorialRawTaste(String sensorialRawTaste) {
        this.sensorialRawTaste = sensorialRawTaste;
    }

    public String getSensorialRawColor() {
        return sensorialRawColor;
    }

    public void setSensorialRawColor(String sensorialRawColor) {
        this.sensorialRawColor = sensorialRawColor;
    }

    public String getSensorialCookedOdor() {
        return sensorialCookedOdor;
    }

    public void setSensorialCookedOdor(String sensorialCookedOdor) {
        this.sensorialCookedOdor = sensorialCookedOdor;
    }

    public String getSensorialCookedTaste() {
        return sensorialCookedTaste;
    }

    public void setSensorialCookedTaste(String sensorialCookedTaste) {
        this.sensorialCookedTaste = sensorialCookedTaste;
    }

    public String getSensorialCookedColor() {
        return sensorialCookedColor;
    }

    public void setSensorialCookedColor(String sensorialCookedColor) {
        this.sensorialCookedColor = sensorialCookedColor;
    }

    public String getQualityNotes() {
        return qualityNotes;
    }

    public void setQualityNotes(String qualityNotes) {
        this.qualityNotes = qualityNotes;
    }

    public Boolean getMetabisulfiteLevelAcceptable() {
        return metabisulfiteLevelAcceptable;
    }

    public void setMetabisulfiteLevelAcceptable(Boolean metabisulfiteLevelAcceptable) {
        this.metabisulfiteLevelAcceptable = metabisulfiteLevelAcceptable;
    }

    public Boolean getApprovedForPurchase() {
        return approvedForPurchase;
    }

    public void setApprovedForPurchase(Boolean approvedForPurchase) {
        this.approvedForPurchase = approvedForPurchase;
    }

    /**
     * Analysis type enum
     */
    public enum AnalysisType {
        SENSORIAL,
        MICROBIOLOGICAL,
        CHEMICAL,
        PCR
    }
}
