package com.abelium.inatrace.components.company.plotimport;

import com.abelium.inatrace.components.company.types.PlotGeoJsonImportIssueType;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mapbox.geojson.Point;

import java.util.List;

/**
 * One feature of a GeoJSON file, already reduced to what a plot can store.
 *
 * @param number        1-based position in the file, as the user sees it in a GIS tool
 * @param properties    the feature properties (never {@code null})
 * @param ring          outer ring of a polygon, closed (first vertex repeated), or a
 *                      single point; empty when the geometry is invalid
 * @param areaHectares  geodesic area of the polygon; {@code null} for points and invalid
 *                      geometries
 * @param geometryIssue why the geometry cannot become a plot, or {@code null}
 */
public record PlotGeoJsonFeature(int number,
                                 JsonObject properties,
                                 List<Point> ring,
                                 Double areaHectares,
                                 PlotGeoJsonImportIssueType geometryIssue) {

	public boolean isPolygon() {
		return geometryIssue == null && ring.size() > 1;
	}

	public boolean isPoint() {
		return geometryIssue == null && ring.size() == 1;
	}

	/**
	 * Property as trimmed text; numbers are returned in their JSON form. Blank values
	 * are {@code null}.
	 */
	public String text(String name) {
		JsonElement element = properties.get(name);
		if (element == null || !element.isJsonPrimitive()) {
			return null;
		}
		String value = element.getAsString().trim();
		return value.isEmpty() ? null : value;
	}

	/**
	 * Property as a number, accepting numeric JSON values and numeric text with a dot or
	 * a comma as decimal separator. Anything else is {@code null}.
	 */
	public Double number(String name) {
		String value = text(name);
		if (value == null) {
			return null;
		}
		try {
			double parsed = Double.parseDouble(value.replace(',', '.'));
			return Double.isFinite(parsed) ? parsed : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}

}
