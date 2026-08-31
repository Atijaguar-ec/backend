package com.abelium.inatrace.tools;

import com.abelium.inatrace.db.entities.common.PlotCoordinate;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PlotGeometryToolsTest {

	@Test
	void shouldCloseTheRingWhenThePlotWasStoredOpen() {

		String wkt = PlotGeometryTools.toPolygonWkt(List.of(
				coordinate(-1.5, -79.5),
				coordinate(-1.49, -79.5),
				coordinate(-1.49, -79.49)));

		assertEquals("POLYGON ((-79.5 -1.5, -79.5 -1.49, -79.49 -1.49, -79.5 -1.5))", wkt);
	}

	@Test
	void shouldNotDuplicateTheClosingVertexWhenItIsAlreadyThere() {

		String wkt = PlotGeometryTools.toPolygonWkt(List.of(
				coordinate(-1.5, -79.5),
				coordinate(-1.49, -79.5),
				coordinate(-1.49, -79.49),
				coordinate(-1.5, -79.5)));

		assertEquals("POLYGON ((-79.5 -1.5, -79.5 -1.49, -79.49 -1.49, -79.5 -1.5))", wkt);
	}

	@Test
	void shouldWriteSmallMagnitudesInPlainDecimalNotation() {

		String wkt = PlotGeometryTools.toPolygonWkt(List.of(
				coordinate(0.00001, 0.00002),
				coordinate(0.00003, 0.00002),
				coordinate(0.00003, 0.00004)));

		// Double.toString would have produced 1.0E-5 here, which no WKT parser accepts.
		assertEquals("POLYGON ((0.00002 0.00001, 0.00002 0.00003, 0.00004 0.00003, 0.00002 0.00001))", wkt);
	}

	@Test
	void shouldRejectGeometriesThatAreNotPolygons() {

		assertNull(PlotGeometryTools.toPolygonWkt(null));
		assertNull(PlotGeometryTools.toPolygonWkt(List.of()));
		assertNull(PlotGeometryTools.toPolygonWkt(List.of(coordinate(-1.5, -79.5), coordinate(-1.49, -79.5))));
	}

	@Test
	void shouldRejectAPlotWithAMissingCoordinateValue() {

		PlotCoordinate incomplete = new PlotCoordinate();
		incomplete.setLatitude(-1.49);

		assertNull(PlotGeometryTools.toPolygonWkt(List.of(
				coordinate(-1.5, -79.5),
				coordinate(-1.49, -79.5),
				incomplete)));
	}

	@Test
	void shouldRejectATriangleWhoseFirstVertexIsAlreadyRepeated() {

		// Three points where the last closes the ring leaves only two distinct vertices.
		assertNull(PlotGeometryTools.toPolygonWkt(List.of(
				coordinate(-1.5, -79.5),
				coordinate(-1.49, -79.5),
				coordinate(-1.5, -79.5))));
	}

	private PlotCoordinate coordinate(double latitude, double longitude) {
		PlotCoordinate coordinate = new PlotCoordinate();
		coordinate.setLatitude(latitude);
		coordinate.setLongitude(longitude);
		return coordinate;
	}

}
