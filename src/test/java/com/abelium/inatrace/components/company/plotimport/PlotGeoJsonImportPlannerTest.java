package com.abelium.inatrace.components.company.plotimport;

import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.company.plotimport.PlotGeoJsonImportPlanner.Company;
import com.abelium.inatrace.components.company.plotimport.PlotGeoJsonImportPlanner.Farmer;
import com.abelium.inatrace.components.company.plotimport.PlotGeoJsonImportPlanner.Issue;
import com.abelium.inatrace.components.company.plotimport.PlotGeoJsonImportPlanner.Plan;
import com.abelium.inatrace.components.company.plotimport.PlotGeoJsonImportPlanner.PlannedPlot;
import com.abelium.inatrace.components.company.plotimport.PlotGeoJsonImportPlanner.ProductType;
import com.abelium.inatrace.components.company.types.PlotGeoJsonImportIssueType;
import com.abelium.inatrace.components.company.types.PlotGeoJsonImportScope;
import com.abelium.inatrace.types.CocoaVariety;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PlotGeoJsonImportPlannerTest {

	private static final String SQUARE = "[[[-79.555,-2.214],[-79.554,-2.214],[-79.554,-2.213],[-79.555,-2.214]]]";

	/** Umbrella organisation (1) with two associations (2 and 3), like UNOCACE. */
	private static final List<Company> COMPANIES = List.of(
			new Company(1L, "UNOCACE", 0),
			new Company(2L, "2 de Mayo", 66),
			new Company(3L, "El Paraíso", 5));

	private static final List<ProductType> PRODUCT_TYPES = List.of(
			new ProductType(1L, "Cacao"),
			new ProductType(2L, "CACAO CCN51"),
			new ProductType(3L, "CACAO NACIONAL"),
			new ProductType(9L, "cacao ccn51"));

	private static final Farmer ANA = new Farmer(10L, 2L, "MY0000000001", "Ana", 1L);
	private static final Farmer BETO = new Farmer(11L, 2L, "MY0000000002", "Beto", 1L);
	private static final Farmer CARLA = new Farmer(12L, 2L, "MY0000000003", "Carla", null);
	private static final Farmer DUPLICATE_A = new Farmer(13L, 2L, "MY0000000004", "Duplicada A", 1L);
	private static final Farmer DUPLICATE_B = new Farmer(14L, 3L, "my0000000004 ", "Duplicada B", 1L);
	private static final Farmer PARAISO = new Farmer(15L, 3L, "P0000000005", "Paraíso", 1L);

	private static final List<Farmer> FARMERS = List.of(ANA, BETO, CARLA, DUPLICATE_A, DUPLICATE_B, PARAISO);

	@Test
	void shouldBuildOnePlotPerFeatureWithTheSurveyedAttributes() throws ApiException {

		Plan plan = plan(PlotGeoJsonImportScope.COMPANY_AND_CONNECTED,
				feature(" my0000000001 ", "LOTE-1", "1.0047", "CACAO CCN51"),
				feature("MY0000000001", "LOTE-2", null, "Cacao Nacional"),
				feature("MY0000000002", "LOTE-1", "0", "SOMETHING ELSE"));

		assertEquals(List.of(), plan.issues());
		assertEquals(3, plan.plots().size());

		PlannedPlot first = plan.plots().get(0);
		assertEquals(ANA.id(), first.farmerId());
		assertEquals(2L, first.companyId());
		assertEquals("LOTE-1", first.plotName());
		assertEquals(1.0047, first.size());
		assertEquals(2L, first.cropId(), "same-name product types resolve to the lowest id");
		assertEquals(CocoaVariety.CCN51, first.cocoaVariety());
		assertEquals(4, first.coordinates().size());

		PlannedPlot second = plan.plots().get(1);
		assertEquals(3L, second.cropId());
		assertEquals(CocoaVariety.ORGANICO, second.cocoaVariety(), "Nacional is variety 1, stored as ORGANICO");
		assertEquals(0.6, second.size(), 0.05, "without HECTAREA the geodesic area is used");

		PlannedPlot third = plan.plots().get(2);
		assertEquals(BETO.id(), third.farmerId());
		assertEquals(1L, third.cropId(), "an unknown crop falls back to the farmer's product type");
		assertNull(third.cocoaVariety());
		assertEquals(0.6, third.size(), 0.05, "a zero HECTAREA is not a size");
	}

	@Test
	void shouldReportEveryFeatureThatCannotBeImported() throws ApiException {

		Plan plan = plan(PlotGeoJsonImportScope.COMPANY_AND_CONNECTED,
				feature(null, "LOTE-1", "1", "CACAO CCN51"),
				feature("MY0000000001", null, "1", "CACAO CCN51"),
				feature("MY9999999999", "LOTE-1", "1", "CACAO CCN51"),
				feature("MY0000000004", "LOTE-1", "1", "CACAO CCN51"),
				feature("MY0000000003", "LOTE-1", "1", "OTRO"),
				feature("MY0000000002", "Lote-1", "1", "CACAO CCN51"),
				feature("MY0000000002", " LOTE-1", "1", "CACAO CCN51"),
				"{\"type\":\"Feature\",\"properties\":{\"ID_INTERNO\":\"MY0000000001\",\"COD_LOTE\":\"LOTE-9\"},"
						+ "\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[-79.5,-2.2],[-79.4,-2.2]]}}");

		assertEquals(List.of(
						new Issue(1, null, "LOTE-1", PlotGeoJsonImportIssueType.MISSING_FARMER_ID),
						new Issue(2, "MY0000000001", null, PlotGeoJsonImportIssueType.MISSING_PLOT_NAME),
						new Issue(3, "MY9999999999", "LOTE-1", PlotGeoJsonImportIssueType.FARMER_NOT_FOUND),
						new Issue(4, "MY0000000004", "LOTE-1", PlotGeoJsonImportIssueType.FARMER_AMBIGUOUS),
						new Issue(5, "MY0000000003", "LOTE-1", PlotGeoJsonImportIssueType.CROP_NOT_FOUND),
						new Issue(6, "MY0000000002", "Lote-1", PlotGeoJsonImportIssueType.DUPLICATE_PLOT_NAME),
						new Issue(7, "MY0000000002", "LOTE-1", PlotGeoJsonImportIssueType.DUPLICATE_PLOT_NAME),
						new Issue(8, "MY0000000001", "LOTE-9", PlotGeoJsonImportIssueType.UNSUPPORTED_GEOMETRY)),
				plan.issues());
		assertEquals(List.of(), plan.plots());
		assertEquals(8, plan.featuresRead());
	}

	@Test
	void shouldReplaceTheWholeOrganisationWhenAskedTo() throws ApiException {

		Plan plan = plan(PlotGeoJsonImportScope.COMPANY_AND_CONNECTED,
				feature("MY0000000001", "LOTE-1", "1", "CACAO CCN51"));

		assertEquals(Set.of(1L, 2L, 3L), plan.companiesToReplace());
		assertEquals(71, plan.plotsToDelete());
	}

	@Test
	void shouldReplaceOnlyTheCompaniesThatReceivePlots() throws ApiException {

		Plan plan = plan(PlotGeoJsonImportScope.MATCHED_COMPANIES,
				feature("MY0000000001", "LOTE-1", "1", "CACAO CCN51"),
				// Not importable: must not put its company in the replacement.
				feature("P9999999999", "LOTE-1", "1", "CACAO CCN51"));

		assertEquals(Set.of(2L), plan.companiesToReplace());
		assertEquals(66, plan.plotsToDelete());
	}

	@Test
	void shouldListTheFarmersOfReceivingCompaniesThatEndUpWithoutPlots() throws ApiException {

		Plan plan = plan(PlotGeoJsonImportScope.COMPANY_AND_CONNECTED,
				feature("MY0000000001", "LOTE-1", "1", "CACAO CCN51"));

		// Company 3 receives nothing, so its farmers are not listed even though its
		// plots are deleted: the per-company numbers already say so.
		assertEquals(List.of(BETO.id(), CARLA.id(), DUPLICATE_A.id()),
				plan.farmersWithoutPlots().stream().map(Farmer::id).toList());
	}

	@Test
	void shouldRecogniseTheVarietyNamesUsedInTheSurveys() {

		assertEquals(CocoaVariety.CCN51, PlotGeoJsonImportPlanner.cocoaVariety("CACAO CCN51"));
		assertEquals(CocoaVariety.CCN51, PlotGeoJsonImportPlanner.cocoaVariety("Cacao CCN-51"));
		assertEquals(CocoaVariety.CCN51, PlotGeoJsonImportPlanner.cocoaVariety("CACAO CC51"));
		assertEquals(CocoaVariety.ORGANICO, PlotGeoJsonImportPlanner.cocoaVariety("CACAO NACIONAL"));
		assertEquals(CocoaVariety.ORGANICO, PlotGeoJsonImportPlanner.cocoaVariety("cacao nacional fino de aroma"));
		assertNull(PlotGeoJsonImportPlanner.cocoaVariety("CACAO"));
		assertNull(PlotGeoJsonImportPlanner.cocoaVariety("CACAO SUPER ÁRBOL"));
		assertNull(PlotGeoJsonImportPlanner.cocoaVariety(null));
	}

	private static Plan plan(PlotGeoJsonImportScope scope, String... features) throws ApiException {
		String json = "{\"type\":\"FeatureCollection\",\"features\":[" + String.join(",", features) + "]}";
		List<PlotGeoJsonFeature> read = PlotGeoJsonReader.read(json.getBytes(StandardCharsets.UTF_8));
		return PlotGeoJsonImportPlanner.plan(read, COMPANIES, FARMERS, PRODUCT_TYPES, scope);
	}

	private static String feature(String farmerId, String plotName, String hectares, String crop) {
		List<String> properties = new ArrayList<>();
		if (farmerId != null) {
			properties.add("\"ID_INTERNO\":\"" + farmerId + "\"");
		}
		if (plotName != null) {
			properties.add("\"COD_LOTE\":\"" + plotName + "\"");
		}
		if (hectares != null) {
			properties.add("\"HECTAREA\":" + hectares);
		}
		if (crop != null) {
			properties.add("\"CULTIVOPRI\":\"" + crop + "\"");
		}
		return "{\"type\":\"Feature\",\"properties\":{" + properties.stream().collect(Collectors.joining(","))
				+ "},\"geometry\":{\"type\":\"MultiPolygon\",\"coordinates\":[" + SQUARE + "]}}";
	}

}
