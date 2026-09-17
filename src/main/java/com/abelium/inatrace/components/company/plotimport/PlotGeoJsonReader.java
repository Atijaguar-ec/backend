package com.abelium.inatrace.components.company.plotimport;

import com.abelium.inatrace.api.ApiStatus;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.company.types.PlotGeoJsonImportIssueType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.MultiPolygon;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.turf.TurfMeasurement;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Reads a GeoJSON {@code FeatureCollection} into {@link PlotGeoJsonFeature}s.
 *
 * Problems with the file as a whole (not JSON, not a feature collection, projected
 * coordinates) throw; problems with a single feature are recorded on that feature so the
 * caller can report every one of them at once.
 *
 * Accepted geometries are {@code Point}, {@code Polygon} and single-part
 * {@code MultiPolygon} (GIS tools such as QGIS export every polygon layer as
 * multipolygons, even when each feature has one part).
 */
public final class PlotGeoJsonReader {

	private static final double SQUARE_METRES_PER_HECTARE = 10_000d;

	/** Three distinct vertices plus the repeated closing vertex. */
	private static final int MIN_CLOSED_RING_SIZE = 4;

	private PlotGeoJsonReader() {
	}

	public static List<PlotGeoJsonFeature> read(byte[] content) throws ApiException {

		JsonObject collection = parseCollection(content);
		checkCoordinateReferenceSystem(collection);

		JsonArray features = collection.getAsJsonArray("features");
		List<PlotGeoJsonFeature> result = new ArrayList<>(features.size());
		for (int i = 0; i < features.size(); i++) {
			result.add(readFeature(i + 1, features.get(i)));
		}
		return result;
	}

	private static JsonObject parseCollection(byte[] content) throws ApiException {

		JsonElement root;
		try {
			root = JsonParser.parseString(new String(content, StandardCharsets.UTF_8));
		} catch (JsonParseException e) {
			throw new ApiException(ApiStatus.VALIDATION_ERROR, "The file is not valid JSON");
		}

		if (!root.isJsonObject()
				|| !"FeatureCollection".equals(textMember(root.getAsJsonObject(), "type"))
				|| !root.getAsJsonObject().has("features")
				|| !root.getAsJsonObject().get("features").isJsonArray()) {
			throw new ApiException(ApiStatus.VALIDATION_ERROR, "The file is not a GeoJSON FeatureCollection");
		}
		return root.getAsJsonObject();
	}

	/**
	 * GeoJSON (RFC 7946) is always WGS 84 longitude/latitude, but older exports still
	 * carry a {@code crs} member. A projected one (UTM, for example) would be stored as
	 * nonsense coordinates, so it is refused instead of silently reprojected.
	 */
	private static void checkCoordinateReferenceSystem(JsonObject collection) throws ApiException {

		JsonElement crs = collection.get("crs");
		if (crs == null || crs.isJsonNull()) {
			return;
		}

		String name = null;
		if (crs.isJsonObject() && crs.getAsJsonObject().has("properties")
				&& crs.getAsJsonObject().get("properties").isJsonObject()) {
			name = textMember(crs.getAsJsonObject().getAsJsonObject("properties"), "name");
		}

		String normalized = name == null ? "" : name.toUpperCase(Locale.ROOT);
		if (!normalized.endsWith("CRS84") && !normalized.endsWith("EPSG::4326") && !normalized.endsWith("EPSG:4326")) {
			throw new ApiException(ApiStatus.VALIDATION_ERROR,
					"Unsupported coordinate reference system '" + name + "': export the layer as WGS 84 (EPSG:4326)");
		}
	}

	private static PlotGeoJsonFeature readFeature(int number, JsonElement element) {

		if (!element.isJsonObject()) {
			return invalid(number, new JsonObject(), PlotGeoJsonImportIssueType.UNSUPPORTED_GEOMETRY);
		}

		JsonElement rawProperties = element.getAsJsonObject().get("properties");
		JsonObject properties = rawProperties != null && rawProperties.isJsonObject()
				? rawProperties.getAsJsonObject()
				: new JsonObject();

		Geometry geometry;
		try {
			geometry = Feature.fromJson(element.toString()).geometry();
		} catch (RuntimeException e) {
			// Unknown geometry types and malformed coordinate arrays end up here.
			return invalid(number, properties, PlotGeoJsonImportIssueType.UNSUPPORTED_GEOMETRY);
		}

		if (geometry instanceof Point point) {
			return isValidPosition(point)
					? new PlotGeoJsonFeature(number, properties, List.of(point), null, null)
					: invalid(number, properties, PlotGeoJsonImportIssueType.INVALID_COORDINATES);
		}

		List<List<Point>> rings;
		if (geometry instanceof Polygon polygon) {
			rings = polygon.coordinates();
		} else if (geometry instanceof MultiPolygon multiPolygon) {
			if (multiPolygon.coordinates().size() != 1) {
				return invalid(number, properties, PlotGeoJsonImportIssueType.MULTIPART_GEOMETRY);
			}
			rings = multiPolygon.coordinates().get(0);
		} else {
			return invalid(number, properties, PlotGeoJsonImportIssueType.UNSUPPORTED_GEOMETRY);
		}

		if (rings == null || rings.isEmpty()) {
			return invalid(number, properties, PlotGeoJsonImportIssueType.INVALID_COORDINATES);
		}
		if (rings.size() > 1) {
			return invalid(number, properties, PlotGeoJsonImportIssueType.POLYGON_WITH_HOLES);
		}

		List<Point> ring = closed(rings.get(0));
		if (ring.size() < MIN_CLOSED_RING_SIZE || !ring.stream().allMatch(PlotGeoJsonReader::isValidPosition)) {
			return invalid(number, properties, PlotGeoJsonImportIssueType.INVALID_COORDINATES);
		}

		double areaHectares = TurfMeasurement.area(Polygon.fromLngLats(List.of(ring))) / SQUARE_METRES_PER_HECTARE;
		return new PlotGeoJsonFeature(number, properties, List.copyOf(ring), areaHectares, null);
	}

	/**
	 * RFC 7946 requires closed rings, but some tools write them open. Stored plots keep
	 * the closing vertex, like the ones already in the database.
	 */
	private static List<Point> closed(List<Point> ring) {

		List<Point> result = new ArrayList<>(ring);
		if (!result.isEmpty()) {
			Point first = result.get(0);
			Point last = result.get(result.size() - 1);
			if (first.longitude() != last.longitude() || first.latitude() != last.latitude()) {
				result.add(Point.fromLngLat(first.longitude(), first.latitude()));
			}
		}
		return result;
	}

	private static boolean isValidPosition(Point point) {
		return point != null
				&& Double.isFinite(point.longitude()) && Math.abs(point.longitude()) <= 180
				&& Double.isFinite(point.latitude()) && Math.abs(point.latitude()) <= 90;
	}

	private static PlotGeoJsonFeature invalid(int number, JsonObject properties, PlotGeoJsonImportIssueType issue) {
		return new PlotGeoJsonFeature(number, properties, List.of(), null, issue);
	}

	private static String textMember(JsonObject object, String name) {
		JsonElement element = object.get(name);
		return element != null && element.isJsonPrimitive() ? element.getAsString() : null;
	}

}
