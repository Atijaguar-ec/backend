package com.abelium.inatrace.components.company.plotimport;

import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.company.types.PlotGeoJsonImportIssueType;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlotGeoJsonReaderTest {

	/** About 1.1 ha near Naranjal: 0.001° x 0.001° at latitude -2.2. */
	private static final String SQUARE = "[[-79.555,-2.214],[-79.554,-2.214],[-79.554,-2.213],[-79.555,-2.213],[-79.555,-2.214]]";

	@Test
	void shouldReadASinglePartMultiPolygonAsAPolygon() throws ApiException {

		// Layout of the QGIS export delivered by UNOCACE, crs member included.
		List<PlotGeoJsonFeature> features = read("""
				{"type":"FeatureCollection","name":"POLIGONOS",
				 "crs":{"type":"name","properties":{"name":"urn:ogc:def:crs:OGC:1.3:CRS84"}},
				 "features":[{"type":"Feature",
				   "properties":{"ID_INTERNO":"MY0000000001","COD_LOTE":"LOTE-1","HECTAREA":1.0047,"CULTIVOPRI":"CACAO CCN51"},
				   "geometry":{"type":"MultiPolygon","coordinates":[[%s]]}}]}
				""".formatted(SQUARE));

		assertEquals(1, features.size());
		PlotGeoJsonFeature feature = features.get(0);
		assertTrue(feature.isPolygon());
		assertNull(feature.geometryIssue());
		assertEquals(5, feature.ring().size());
		assertEquals(-79.555, feature.ring().get(0).longitude());
		assertEquals(-2.214, feature.ring().get(0).latitude());
		assertEquals("MY0000000001", feature.text("ID_INTERNO"));
		assertEquals(1.0047, feature.number("HECTAREA"));
	}

	@Test
	void shouldComputeTheAreaInHectaresNotInThousandsOfSquareMetres() throws ApiException {

		PlotGeoJsonFeature feature = read(collection("{\"type\":\"Polygon\",\"coordinates\":[" + SQUARE + "]}")).get(0);

		// 111.3 m x 110.6 m ≈ 1.23 ha. The old upload divided by 1000 and stored 12.3.
		assertEquals(1.23, feature.areaHectares(), 0.01);
	}

	@Test
	void shouldCloseAnOpenRing() throws ApiException {

		PlotGeoJsonFeature feature = read(collection(
				"{\"type\":\"Polygon\",\"coordinates\":[[[-79.555,-2.214],[-79.554,-2.214],[-79.554,-2.213]]]}")).get(0);

		assertTrue(feature.isPolygon());
		assertEquals(4, feature.ring().size());
		assertEquals(feature.ring().get(0), feature.ring().get(3));
	}

	@Test
	void shouldReadPoints() throws ApiException {

		PlotGeoJsonFeature feature = read(collection("{\"type\":\"Point\",\"coordinates\":[-79.5,-2.2]}")).get(0);

		assertTrue(feature.isPoint());
		assertNull(feature.areaHectares());
	}

	@Test
	void shouldFlagEveryGeometryAPlotCannotStore() throws ApiException {

		List<PlotGeoJsonFeature> features = read("""
				{"type":"FeatureCollection","features":[
				 {"type":"Feature","properties":{},"geometry":{"type":"MultiPolygon","coordinates":[[%1$s],[%1$s]]}},
				 {"type":"Feature","properties":{},"geometry":{"type":"Polygon","coordinates":[%1$s,%1$s]}},
				 {"type":"Feature","properties":{},"geometry":{"type":"LineString","coordinates":[[-79.5,-2.2],[-79.4,-2.2]]}},
				 {"type":"Feature","properties":{},"geometry":null},
				 {"type":"Feature","properties":{},"geometry":{"type":"Polygon","coordinates":[[[-79.5,-2.2],[-79.4,-2.2],[-79.5,-2.2]]]}},
				 {"type":"Feature","properties":{},"geometry":{"type":"Polygon","coordinates":[[[500000,9750000],[500100,9750000],[500100,9750100],[500000,9750000]]]}}
				]}
				""".formatted(SQUARE));

		assertEquals(List.of(
				PlotGeoJsonImportIssueType.MULTIPART_GEOMETRY,
				PlotGeoJsonImportIssueType.POLYGON_WITH_HOLES,
				PlotGeoJsonImportIssueType.UNSUPPORTED_GEOMETRY,
				PlotGeoJsonImportIssueType.UNSUPPORTED_GEOMETRY,
				PlotGeoJsonImportIssueType.INVALID_COORDINATES,
				PlotGeoJsonImportIssueType.INVALID_COORDINATES),
				features.stream().map(PlotGeoJsonFeature::geometryIssue).toList());
		assertEquals(List.of(1, 2, 3, 4, 5, 6), features.stream().map(PlotGeoJsonFeature::number).toList());
	}

	@Test
	void shouldRejectProjectedCoordinateSystems() {

		ApiException error = assertThrows(ApiException.class, () -> read("""
				{"type":"FeatureCollection",
				 "crs":{"type":"name","properties":{"name":"urn:ogc:def:crs:EPSG::32717"}},
				 "features":[]}
				"""));

		assertTrue(error.getMessage().contains("EPSG::32717"));
	}

	@Test
	void shouldRejectFilesThatAreNotFeatureCollections() {

		assertThrows(ApiException.class, () -> read("not json"));
		assertThrows(ApiException.class, () -> read("{\"type\":\"Feature\",\"geometry\":null}"));
		assertThrows(ApiException.class, () -> read("[]"));
	}

	@Test
	void shouldReadNumbersWrittenAsTextWithADecimalComma() throws ApiException {

		PlotGeoJsonFeature feature = read("""
				{"type":"FeatureCollection","features":[{"type":"Feature",
				 "properties":{"HECTAREA":"1,5","ID_INTERNO":"  ","OTHER":{"nested":true}},
				 "geometry":{"type":"Point","coordinates":[-79.5,-2.2]}}]}
				""").get(0);

		assertEquals(1.5, feature.number("HECTAREA"));
		assertNull(feature.text("ID_INTERNO"));
		assertNull(feature.text("OTHER"));
		assertNull(feature.text("MISSING"));
	}

	private static String collection(String geometry) {
		return "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":{},\"geometry\":"
				+ geometry + "}]}";
	}

	private static List<PlotGeoJsonFeature> read(String json) throws ApiException {
		return PlotGeoJsonReader.read(json.getBytes(StandardCharsets.UTF_8));
	}

}
