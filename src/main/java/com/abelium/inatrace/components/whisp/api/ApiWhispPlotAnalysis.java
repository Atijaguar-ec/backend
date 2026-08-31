package com.abelium.inatrace.components.whisp.api;

import com.abelium.inatrace.api.ApiBaseEntity;
import com.abelium.inatrace.types.DeforestationAnalysisStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import java.util.Date;
import java.util.Map;

@Validated
@Schema(description = "Deforestation risk analysis of a plot, as returned by Whisp.")
public class ApiWhispPlotAnalysis extends ApiBaseEntity {

	@Schema(description = "ID of the analysed plot")
	private Long plotId;

	@Schema(description = "Name of the analysed plot")
	private String plotName;

	@Schema(description = "State of the analysis job")
	private DeforestationAnalysisStatus status;

	@Schema(description = "Whisp job token")
	private String whispToken;

	@Schema(description = "How the geometry was submitted: GEO_ID or WKT")
	private String submissionMode;

	@Schema(description = "AgStack geo id the analysis was run against, when submitted by geo id")
	private String geoId;

	@Schema(description = "When the analysis was submitted to Whisp")
	private Date requestedAt;

	@Schema(description = "When the analysis finished")
	private Date completedAt;

	@Schema(description = "Completion percentage reported by Whisp while the job runs")
	private Integer progressPercent;

	@Schema(description = "Overall deforestation risk for perennial crops (risk_pcrop)")
	private String riskPcrop;

	@Schema(description = "Whisp indicator 1 - tree cover")
	private String indicatorTreeCover;

	@Schema(description = "Whisp indicator 2 - agricultural commodities")
	private String indicatorCommodities;

	@Schema(description = "Whisp indicator 3 - disturbance before the 2020 cut-off")
	private String indicatorDisturbanceBefore2020;

	@Schema(description = "Whisp indicator 4 - disturbance after the 2020 cut-off")
	private String indicatorDisturbanceAfter2020;

	@Schema(description = "Plot area measured by Whisp from the geometry")
	private Double area;

	@Schema(description = "Unit of the measured area")
	private String areaUnit;

	@Schema(description = "Country Whisp located the plot in")
	private String country;

	@Schema(description = "Reason the analysis failed, when it did")
	private String errorMessage;

	@Schema(description = "Every field Whisp reported for this plot, including the datasets "
			+ "not promoted to a dedicated column. The keys depend on the analysis schema "
			+ "active in Whisp at the time of the analysis.")
	private Map<String, Object> results;

	public Long getPlotId() {
		return plotId;
	}

	public void setPlotId(Long plotId) {
		this.plotId = plotId;
	}

	public String getPlotName() {
		return plotName;
	}

	public void setPlotName(String plotName) {
		this.plotName = plotName;
	}

	public DeforestationAnalysisStatus getStatus() {
		return status;
	}

	public void setStatus(DeforestationAnalysisStatus status) {
		this.status = status;
	}

	public String getWhispToken() {
		return whispToken;
	}

	public void setWhispToken(String whispToken) {
		this.whispToken = whispToken;
	}

	public String getSubmissionMode() {
		return submissionMode;
	}

	public void setSubmissionMode(String submissionMode) {
		this.submissionMode = submissionMode;
	}

	public String getGeoId() {
		return geoId;
	}

	public void setGeoId(String geoId) {
		this.geoId = geoId;
	}

	public Date getRequestedAt() {
		return requestedAt;
	}

	public void setRequestedAt(Date requestedAt) {
		this.requestedAt = requestedAt;
	}

	public Date getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(Date completedAt) {
		this.completedAt = completedAt;
	}

	public Integer getProgressPercent() {
		return progressPercent;
	}

	public void setProgressPercent(Integer progressPercent) {
		this.progressPercent = progressPercent;
	}

	public String getRiskPcrop() {
		return riskPcrop;
	}

	public void setRiskPcrop(String riskPcrop) {
		this.riskPcrop = riskPcrop;
	}

	public String getIndicatorTreeCover() {
		return indicatorTreeCover;
	}

	public void setIndicatorTreeCover(String indicatorTreeCover) {
		this.indicatorTreeCover = indicatorTreeCover;
	}

	public String getIndicatorCommodities() {
		return indicatorCommodities;
	}

	public void setIndicatorCommodities(String indicatorCommodities) {
		this.indicatorCommodities = indicatorCommodities;
	}

	public String getIndicatorDisturbanceBefore2020() {
		return indicatorDisturbanceBefore2020;
	}

	public void setIndicatorDisturbanceBefore2020(String indicatorDisturbanceBefore2020) {
		this.indicatorDisturbanceBefore2020 = indicatorDisturbanceBefore2020;
	}

	public String getIndicatorDisturbanceAfter2020() {
		return indicatorDisturbanceAfter2020;
	}

	public void setIndicatorDisturbanceAfter2020(String indicatorDisturbanceAfter2020) {
		this.indicatorDisturbanceAfter2020 = indicatorDisturbanceAfter2020;
	}

	public Double getArea() {
		return area;
	}

	public void setArea(Double area) {
		this.area = area;
	}

	public String getAreaUnit() {
		return areaUnit;
	}

	public void setAreaUnit(String areaUnit) {
		this.areaUnit = areaUnit;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public Map<String, Object> getResults() {
		return results;
	}

	public void setResults(Map<String, Object> results) {
		this.results = results;
	}

}
