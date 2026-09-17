package com.abelium.inatrace.components.company.types;

/**
 * Why a feature of a bulk GeoJSON plot import cannot become a plot.
 *
 * Every issue blocks the import unless the caller explicitly asks to skip the features
 * that have one.
 */
public enum PlotGeoJsonImportIssueType {

	/** The feature has no farmer internal id property. */
	MISSING_FARMER_ID,

	/** No farmer in the import scope has that internal id. */
	FARMER_NOT_FOUND,

	/** More than one farmer in the import scope has that internal id. */
	FARMER_AMBIGUOUS,

	/** The feature has no plot code property. */
	MISSING_PLOT_NAME,

	/** The same farmer and plot code appear in more than one feature. */
	DUPLICATE_PLOT_NAME,

	/** The geometry is missing or is not a polygon. */
	UNSUPPORTED_GEOMETRY,

	/** A multipolygon with more than one part: a plot stores a single ring. */
	MULTIPART_GEOMETRY,

	/** A polygon with inner rings: a plot cannot store holes. */
	POLYGON_WITH_HOLES,

	/** Fewer than three vertices, or coordinates outside longitude/latitude ranges. */
	INVALID_COORDINATES,

	/** The crop does not match a product type and the farmer has none assigned. */
	CROP_NOT_FOUND
}
