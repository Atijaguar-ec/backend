package com.abelium.inatrace.components.laboratory.api;

import com.abelium.inatrace.components.common.api.ApiDocument;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Laboratory analysis API model
 *
 * @author INATrace Team
 */
public class ApiLaboratoryAnalysis {

    @Schema(description = "Entity id")
    private Long id;

    @Schema(description = "Creation timestamp")
    private Instant creationTimestamp;

    @Schema(description = "Update timestamp")
    private Instant updateTimestamp;

    @Schema(description = "User ID who created this analysis")
    private Long createdById;

    @Schema(description = "User ID who last updated this analysis")
    private Long updatedById;

    @Schema(description = "Stock order ID associated with this analysis")
    private Long stockOrderId;

    @Schema(description = "Type of analysis", allowableValues = {"SENSORIAL", "MICROBIOLOGICAL", "CHEMICAL", "PCR"})
    private String analysisType = "SENSORIAL";

    @Schema(description = "Date when the analysis was performed")
    private Instant analysisDate;

    @Schema(description = "Sensorial analysis - Raw odor")
    private String sensorialRawOdor;

    @Schema(description = "Sensorial analysis - Raw taste")
    private String sensorialRawTaste;

    @Schema(description = "Sensorial analysis - Raw color")
    private String sensorialRawColor;

    @Schema(description = "Sensorial analysis - Cooked odor")
    private String sensorialCookedOdor;

    @Schema(description = "Sensorial analysis - Cooked taste")
    private String sensorialCookedTaste;

    @Schema(description = "Sensorial analysis - Cooked color")
    private String sensorialCookedColor;

    @Schema(description = "Quality observations and notes")
    private String qualityNotes;

    @Schema(description = "Whether metabisulfite level is acceptable (yes/no)")
    private Boolean metabisulfiteLevelAcceptable;

    @Schema(description = "Whether this analysis is approved for purchase (yes/no)")
    private Boolean approvedForPurchase;

    @Schema(description = "Quality document (PDF) attached to the stock order for this analysis")
    private ApiDocument qualityDocument;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(Instant creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public Instant getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(Instant updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public Long getUpdatedById() {
        return updatedById;
    }

    public void setUpdatedById(Long updatedById) {
        this.updatedById = updatedById;
    }

    public Long getStockOrderId() {
        return stockOrderId;
    }

    public void setStockOrderId(Long stockOrderId) {
        this.stockOrderId = stockOrderId;
    }

    public String getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(String analysisType) {
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

    public ApiDocument getQualityDocument() {
        return qualityDocument;
    }

    public void setQualityDocument(ApiDocument qualityDocument) {
        this.qualityDocument = qualityDocument;
    }
}
