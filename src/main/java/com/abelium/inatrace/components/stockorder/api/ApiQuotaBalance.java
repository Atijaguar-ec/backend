package com.abelium.inatrace.components.stockorder.api;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@Validated
@Schema(description = "Farmer and plot delivery quota balance")
public class ApiQuotaBalance {

    @Schema(description = "Initial quota defined in quintales (qq)")
    private BigDecimal initialQuota;

    @Schema(description = "Initial quota converted to delivery measurement unit (e.g. Libra, kg)")
    private BigDecimal initialQuotaInUnit;

    @Schema(description = "Total delivered quantity so far in the delivery measurement unit")
    private BigDecimal totalDelivered;

    @Schema(description = "Remaining quota balance in the delivery measurement unit")
    private BigDecimal remainingBalance;

    @Schema(description = "Remaining quota balance in quintales (qq)")
    private BigDecimal remainingBalanceInQq;

    @Schema(description = "Measurement unit label (e.g. Libra, kg, qq)")
    private String unit;

    @Schema(description = "Whether the remaining quota is zero or exceeded")
    private Boolean isExceeded;

    @Schema(description = "Whether the remaining quota is at or below 10% of initial quota")
    private Boolean isNearLimit;

    @Schema(description = "Plot name if quota was resolved for a specific plot")
    private String plotName;

    @Schema(description = "Farmer total delivered quantity across all plots in the current cycle")
    private BigDecimal farmerTotalDelivered;

    @Schema(description = "Farmer total initial quota across all plots in delivery measurement unit")
    private BigDecimal farmerTotalQuotaInUnit;

    @Schema(description = "Farmer total remaining quota balance across all plots in delivery measurement unit")
    private BigDecimal farmerTotalRemainingBalance;

    public BigDecimal getInitialQuota() {
        return initialQuota;
    }

    public void setInitialQuota(BigDecimal initialQuota) {
        this.initialQuota = initialQuota;
    }

    public BigDecimal getInitialQuotaInUnit() {
        return initialQuotaInUnit;
    }

    public void setInitialQuotaInUnit(BigDecimal initialQuotaInUnit) {
        this.initialQuotaInUnit = initialQuotaInUnit;
    }

    public BigDecimal getTotalDelivered() {
        return totalDelivered;
    }

    public void setTotalDelivered(BigDecimal totalDelivered) {
        this.totalDelivered = totalDelivered;
    }

    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(BigDecimal remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public BigDecimal getRemainingBalanceInQq() {
        return remainingBalanceInQq;
    }

    public void setRemainingBalanceInQq(BigDecimal remainingBalanceInQq) {
        this.remainingBalanceInQq = remainingBalanceInQq;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Boolean getIsExceeded() {
        return isExceeded;
    }

    public void setIsExceeded(Boolean isExceeded) {
        this.isExceeded = isExceeded;
    }

    public Boolean getIsNearLimit() {
        return isNearLimit;
    }

    public void setIsNearLimit(Boolean isNearLimit) {
        this.isNearLimit = isNearLimit;
    }

    public String getPlotName() {
        return plotName;
    }

    public void setPlotName(String plotName) {
        this.plotName = plotName;
    }

    public BigDecimal getFarmerTotalDelivered() {
        return farmerTotalDelivered;
    }

    public void setFarmerTotalDelivered(BigDecimal farmerTotalDelivered) {
        this.farmerTotalDelivered = farmerTotalDelivered;
    }

    public BigDecimal getFarmerTotalQuotaInUnit() {
        return farmerTotalQuotaInUnit;
    }

    public void setFarmerTotalQuotaInUnit(BigDecimal farmerTotalQuotaInUnit) {
        this.farmerTotalQuotaInUnit = farmerTotalQuotaInUnit;
    }

    public BigDecimal getFarmerTotalRemainingBalance() {
        return farmerTotalRemainingBalance;
    }

    public void setFarmerTotalRemainingBalance(BigDecimal farmerTotalRemainingBalance) {
        this.farmerTotalRemainingBalance = farmerTotalRemainingBalance;
    }
}
