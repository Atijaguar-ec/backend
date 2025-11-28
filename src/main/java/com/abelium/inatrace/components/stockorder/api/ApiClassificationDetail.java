package com.abelium.inatrace.components.stockorder.api;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

/**
 * API model for classification detail row in ProcessingClassificationBatchDetail.
 * Represents size-based classification details for shrimp processing.
 * 
 * @author INATrace Team
 */
@Validated
public class ApiClassificationDetail {

    @Schema(description = "Brand specific to this detail line (optional)")
    private String brandDetail;

    @Schema(description = "Size classification (e.g., '16/20', '21/25') (optional)")
    private String size;

    @Schema(description = "Number of boxes", required = true)
    private Integer boxes;

    @Schema(description = "Weight per box", required = true)
    private BigDecimal weightPerBox;

    @Schema(description = "Weight format: 'LB' or 'KG'", required = true)
    private String weightFormat;

    // 🦐 Campos para Liquidación de Pesca y Compra
    @Schema(description = "Quality grade: A, B, or C (for purchase settlement)")
    private String qualityGrade;

    @Schema(description = "Presentation type: SHELL_ON_A, SHELL_ON_B, BROKEN_VS, TITI, etc.")
    private String presentationType;

    @Schema(description = "Price per pound in USD (for purchase settlement)")
    private BigDecimal pricePerPound;

    @Schema(description = "Calculated line total: pounds × pricePerPound")
    private BigDecimal lineTotal;

    // Getters and Setters

    public String getBrandDetail() {
        return brandDetail;
    }

    public void setBrandDetail(String brandDetail) {
        this.brandDetail = brandDetail;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Integer getBoxes() {
        return boxes;
    }

    public void setBoxes(Integer boxes) {
        this.boxes = boxes;
    }

    public BigDecimal getWeightPerBox() {
        return weightPerBox;
    }

    public void setWeightPerBox(BigDecimal weightPerBox) {
        this.weightPerBox = weightPerBox;
    }

    public String getWeightFormat() {
        return weightFormat;
    }

    public void setWeightFormat(String weightFormat) {
        this.weightFormat = weightFormat;
    }

    // 🦐 Getters/Setters para Liquidación

    public String getQualityGrade() {
        return qualityGrade;
    }

    public void setQualityGrade(String qualityGrade) {
        this.qualityGrade = qualityGrade;
    }

    public String getPresentationType() {
        return presentationType;
    }

    public void setPresentationType(String presentationType) {
        this.presentationType = presentationType;
    }

    public BigDecimal getPricePerPound() {
        return pricePerPound;
    }

    public void setPricePerPound(BigDecimal pricePerPound) {
        this.pricePerPound = pricePerPound;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }
}
