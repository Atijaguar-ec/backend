package com.abelium.inatrace.components.whisp;

/**
 * The bit of a plot that Whisp needs, read inside the transaction and carried outside it
 * so the HTTP call to Whisp does not run with a database transaction held open.
 */
public class PlotAnalysisTarget {

	private final Long plotId;

	private final String plotName;

	private final String geoId;

	private final String wkt;

	public PlotAnalysisTarget(Long plotId, String plotName, String geoId, String wkt) {
		this.plotId = plotId;
		this.plotName = plotName;
		this.geoId = geoId;
		this.wkt = wkt;
	}

	public Long getPlotId() {
		return plotId;
	}

	public String getPlotName() {
		return plotName;
	}

	public String getGeoId() {
		return geoId;
	}

	public String getWkt() {
		return wkt;
	}

}
