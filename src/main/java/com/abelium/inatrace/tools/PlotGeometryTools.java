package com.abelium.inatrace.tools;

import com.abelium.inatrace.db.entities.common.PlotCoordinate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Conversion of a plot's stored coordinates into the WKT polygon that the external
 * geospatial services (AgStack asset registry, Whisp) expect.
 *
 * Lives here rather than inside one of the two clients because both need exactly the
 * same string: a difference between them would mean the geo id registered in AgStack and
 * the geometry analysed by Whisp are not the same field.
 */
public class PlotGeometryTools {

	/**
	 * Minimum number of points of a closed linear ring: three distinct vertices plus the
	 * repeated closing vertex.
	 */
	private static final int MIN_CLOSED_RING_SIZE = 4;

	private PlotGeometryTools() {
	}

	/**
	 * Builds a {@code POLYGON ((lon lat, ...))} out of the plot coordinates, closing the
	 * ring when the caller stored it open (the UI does not repeat the first vertex).
	 *
	 * @param plotCoordinates plot coordinates, in the order they were captured
	 * @return the WKT polygon, or {@code null} when the coordinates cannot form a polygon
	 *         (fewer than three vertices, or any missing latitude/longitude)
	 */
	public static String toPolygonWkt(List<PlotCoordinate> plotCoordinates) {

		List<PlotCoordinate> ring = closedRing(plotCoordinates);
		if (ring == null) {
			return null;
		}

		return ring.stream()
				.map(c -> format(c.getLongitude()) + " " + format(c.getLatitude()))
				.collect(Collectors.joining(", ", "POLYGON ((", "))"));
	}

	/**
	 * Returns a defensive copy of the coordinates with the first vertex repeated at the
	 * end, or {@code null} when they do not describe a polygon.
	 */
	public static List<PlotCoordinate> closedRing(List<PlotCoordinate> plotCoordinates) {

		if (plotCoordinates == null || plotCoordinates.size() < 3) {
			return null;
		}

		List<PlotCoordinate> ring = new ArrayList<>(plotCoordinates);
		if (ring.stream().anyMatch(c -> c == null || c.getLatitude() == null || c.getLongitude() == null)) {
			return null;
		}

		PlotCoordinate first = ring.get(0);
		PlotCoordinate last = ring.get(ring.size() - 1);
		if (!first.getLatitude().equals(last.getLatitude()) || !first.getLongitude().equals(last.getLongitude())) {
			PlotCoordinate closing = new PlotCoordinate();
			closing.setLatitude(first.getLatitude());
			closing.setLongitude(first.getLongitude());
			ring.add(closing);
		}

		// A triangle stored with its first vertex already repeated only has three points
		// and is not a valid ring; both APIs reject it with an opaque 400.
		return ring.size() >= MIN_CLOSED_RING_SIZE ? ring : null;
	}

	/**
	 * Plain decimal notation. {@code Double.toString} switches to scientific notation for
	 * small magnitudes ({@code 1.0E-5}), which neither WKT parser accepts; the trailing
	 * zeros that the conversion leaves behind are stripped so the polygon sent to AgStack
	 * and the one sent to Whisp are byte-identical for the same plot.
	 */
	private static String format(Double value) {
		return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
	}

}
