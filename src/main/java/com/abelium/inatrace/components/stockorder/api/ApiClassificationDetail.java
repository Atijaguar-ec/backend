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

    @Schema(description = "Classification U value", required = true)
    private BigDecimal classificationU;

    @Schema(description = "Classification # value", required = true)
    private BigDecimal classificationNumber;

    @Schema(description = "Weight per box", required = true)
    private BigDecimal weightPerBox;

    @Schema(description = "Weight format: 'LB' or 'KG'", required = true)
    private String weightFormat;

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

    public BigDecimal getClassificationU() {
        return classificationU;
    }

    public void setClassificationU(BigDecimal classificationU) {
        this.classificationU = classificationU;
    }

    public BigDecimal getClassificationNumber() {
        return classificationNumber;
    }

    public void setClassificationNumber(BigDecimal classificationNumber) {
        this.classificationNumber = classificationNumber;
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
}
