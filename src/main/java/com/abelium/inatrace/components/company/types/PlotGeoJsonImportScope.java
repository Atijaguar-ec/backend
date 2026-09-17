package com.abelium.inatrace.components.company.types;

/**
 * Which existing plots a bulk GeoJSON import deletes before creating the new ones.
 *
 * Farmers are always looked up in the selected company and its connected companies
 * (companies that share a product with it); the scope only decides what is replaced.
 */
public enum PlotGeoJsonImportScope {

	/** Every plot of the selected company and of all its connected companies. */
	COMPANY_AND_CONNECTED,

	/** Only the plots of the companies that receive at least one plot from the file. */
	MATCHED_COMPANIES
}
