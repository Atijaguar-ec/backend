package com.abelium.inatrace.db.entities.common;

import com.abelium.inatrace.api.types.Lengths;
import com.abelium.inatrace.db.base.BaseEntity;
import com.abelium.inatrace.types.DeforestationAnalysisStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Date;

/**
 * Result of a Whisp (Open Foris) deforestation risk analysis for a farmer plot.
 *
 * Rows are kept as history, not overwritten: an EUDR due diligence file has to be able
 * to show what the analysis said on the date the lot was bought, and Whisp's own storage
 * is ephemeral, so the raw response is archived here in {@code resultJson}.
 *
 * Every column is nullable on purpose (see agent-context.md section 12): the table is
 * created by hbm2ddl at startup and a NOT NULL column would fail silently on any
 * installation that already has rows.
 */
@Entity
@Table(name = "plotdeforestationanalysis")
public class PlotDeforestationAnalysis extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "plot_id")
	private Plot plot;

	@Enumerated(EnumType.STRING)
	@Column(length = Lengths.ENUM)
	private DeforestationAnalysisStatus status;

	/**
	 * The Whisp job token. One token is one job, which may cover several plots when the
	 * analysis was submitted as a batch.
	 */
	@Column(length = Lengths.DEFAULT)
	private String whispToken;

	/**
	 * How the geometry reached Whisp: {@code GEO_ID} when the plot's AgStack geo id was
	 * used, {@code WKT} when the polygon was sent directly.
	 */
	@Column(length = Lengths.ENUM)
	private String submissionMode;

	/**
	 * The geo id the analysis was run against, when it was submitted by geo id. Stored
	 * next to the result because the plot's geo id can be regenerated later.
	 */
	@Column(length = Lengths.DEFAULT)
	private String geoId;

	@Column
	private Date requestedAt;

	@Column
	private Date completedAt;

	@Column
	private Integer progressPercent;

	/**
	 * Whisp's overall risk verdict for perennial crops ({@code risk_pcrop}): typically
	 * {@code low}, {@code high} or {@code more_info_needed}. Kept as text because the
	 * vocabulary belongs to Whisp and changes with its dataset versions.
	 */
	@Column(length = Lengths.DEFAULT)
	private String riskPcrop;

	/**
	 * Whisp indicator 1 - tree cover on the plot before the cut-off date.
	 */
	@Column(length = Lengths.DEFAULT)
	private String indicatorTreeCover;

	/**
	 * Whisp indicator 2 - presence of agricultural commodities.
	 */
	@Column(length = Lengths.DEFAULT)
	private String indicatorCommodities;

	/**
	 * Whisp indicator 3 - forest disturbance before the 2020 EUDR cut-off.
	 */
	@Column(length = Lengths.DEFAULT)
	private String indicatorDisturbanceBefore2020;

	/**
	 * Whisp indicator 4 - forest disturbance after the 2020 EUDR cut-off. This is the
	 * one that makes a plot non-compliant.
	 */
	@Column(length = Lengths.DEFAULT)
	private String indicatorDisturbanceAfter2020;

	/**
	 * Plot area as measured by Whisp from the geometry, in {@code areaUnit}. Useful as a
	 * cross-check against the area the farmer declared.
	 */
	@Column
	private Double area;

	@Column(length = Lengths.ENUM)
	private String areaUnit;

	@Column(length = Lengths.DEFAULT)
	private String country;

	/**
	 * The complete Whisp feature properties, verbatim. The set of indicators depends on
	 * which datasets Whisp had enabled at analysis time, so anything not promoted to a
	 * column above is still recoverable from here.
	 */
	@Column(columnDefinition = "TEXT")
	private String resultJson;

	@Column(columnDefinition = "TEXT")
	private String errorMessage;

	public Plot getPlot() {
		return plot;
	}

	public void setPlot(Plot plot) {
		this.plot = plot;
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

	public String getResultJson() {
		return resultJson;
	}

	public void setResultJson(String resultJson) {
		this.resultJson = resultJson;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
