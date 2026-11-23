package com.abelium.inatrace.components.integration.v1;

import com.abelium.inatrace.components.integration.v1.dto.*;
import com.abelium.inatrace.configuration.OpenAPIConfiguration;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies the OpenAPI 3.0/3.1 contract for /api/integration/v1 against schema collisions,
 * invalid securityScheme combinations, untyped GeoJSON coordinates, missing required/enum metadata,
 * preserved 200 responses alongside 400/401/403/404/429/500 error responses, and Swagger UI primaryName.
 */
class IntegrationV1OpenApiContractTest {

    @SuppressWarnings("rawtypes")
    private OpenAPI buildCustomizedMieruOpenApi() {
        Map<String, Schema> all = new LinkedHashMap<>();
        Class<?>[] rootDtos = {
                ApiCompanyIntegration.class,
                ApiFacilityIntegration.class,
                ApiReferenceDataIntegration.class,
                ApiFarmerIntegration.class,
                ApiDeliveryIntegration.class,
                ApiShipmentIntegration.class,
                ApiCompositionIntegration.class,
                ApiDeletionIntegration.class,
                ApiIntegrationPage.class
        };
        for (Class<?> dtoClass : rootDtos) {
            all.putAll(ModelConverters.getInstance().readAll(dtoClass));
        }
        OpenAPI openApi = new OpenAPIConfiguration().openAPI();
        Components components = openApi.getComponents() != null ? openApi.getComponents() : new Components();
        components.setSchemas(all);
        openApi.setComponents(components);

        Paths paths = new Paths();
        for (String route : List.of(
                "/api/integration/v1/companies",
                "/api/integration/v1/facilities",
                "/api/integration/v1/reference-data",
                "/api/integration/v1/farmers",
                "/api/integration/v1/farmers/{id}",
                "/api/integration/v1/deliveries",
                "/api/integration/v1/deliveries/{id}",
                "/api/integration/v1/shipments",
                "/api/integration/v1/shipments/{id}/composition",
                "/api/integration/v1/deletions"
        )) {
            Operation getOp = new Operation().responses(new ApiResponses().addApiResponse(
                    "200",
                    new ApiResponse().description("OK").content(new Content().addMediaType("application/json", new MediaType()))
            ));
            paths.addPathItem(route, new PathItem().get(getOp));
        }
        openApi.setPaths(paths);
        OpenAPIConfiguration.customizeMieruCacaoOpenApi(openApi);
        return openApi;
    }

    @Test
    @DisplayName("ReferenceUnitDto preserves 'label' while UnitSummaryDto keeps 2 fields without collision")
    @SuppressWarnings("rawtypes")
    void testReferenceUnitDtoAndUnitSummaryDtoDoNotCollide() {
        Map<String, Schema> schemas = buildCustomizedMieruOpenApi().getComponents().getSchemas();

        assertFalse(schemas.containsKey("UnitDto"), "Ambiguous 'UnitDto' schema name must not exist");
        Schema refUnit = schemas.get("ReferenceUnitDto");
        assertNotNull(refUnit, "ReferenceUnitDto must be registered");
        assertEquals(Set.of("code", "label", "kg_factor"), refUnit.getProperties().keySet());
        assertTrue(refUnit.getRequired().containsAll(List.of("code", "label", "kg_factor")));

        Schema unitSummary = schemas.get("UnitSummaryDto");
        assertNotNull(unitSummary, "UnitSummaryDto must be registered");
        assertEquals(Set.of("code", "kg_factor"), unitSummary.getProperties().keySet());
        assertTrue(unitSummary.getRequired().containsAll(List.of("code", "kg_factor")));
    }

    @Test
    @DisplayName("ShipmentLotCodesDto, CompositionLotCodesDto, and Location DTOs do not collide")
    @SuppressWarnings("rawtypes")
    void testNestedSchemasDoNotCollide() {
        Map<String, Schema> schemas = buildCustomizedMieruOpenApi().getComponents().getSchemas();

        assertFalse(schemas.containsKey("LotCodesDto"), "Ambiguous 'LotCodesDto' schema name must not exist");
        assertFalse(schemas.containsKey("LocationDto"), "Ambiguous 'LocationDto' schema name must not exist");

        Schema shipmentLots = schemas.get("ShipmentLotCodesDto");
        assertNotNull(shipmentLots);
        assertEquals(Set.of("internal_lot_number", "order_id", "qr_code_tag", "sac_numbers"),
                shipmentLots.getProperties().keySet());

        Schema compLots = schemas.get("CompositionLotCodesDto");
        assertNotNull(compLots);
        assertEquals(Set.of("internal_lot_number", "qr_code_tag"),
                compLots.getProperties().keySet());

        assertNotNull(schemas.get("FacilityLocationDto"));
        assertNotNull(schemas.get("FarmerLocationDto"));
    }

    @Test
    @DisplayName("Mieru Cacao OpenAPI exposes only valid Bearer Authentication and global Access token uses APIKEY")
    void testSecuritySchemesValidAndScoped() {
        OpenAPIConfiguration cfg = new OpenAPIConfiguration();
        OpenAPI globalApi = cfg.openAPI();
        SecurityScheme cookieScheme = globalApi.getComponents().getSecuritySchemes().get("Access token");
        assertNotNull(cookieScheme);
        assertEquals(SecurityScheme.Type.APIKEY, cookieScheme.getType(),
                "Cookie securityScheme must use type=apiKey per OpenAPI 3.0 specification");
        assertEquals(SecurityScheme.In.COOKIE, cookieScheme.getIn());

        OpenAPIConfiguration.customizeMieruCacaoOpenApi(globalApi);
        assertEquals(Set.of("Bearer Authentication"), globalApi.getComponents().getSecuritySchemes().keySet(),
                "Mieru Cacao OpenAPI group must expose only Bearer Authentication");
    }

    @Test
    @DisplayName("GeoJsonGeometryDto.coordinates uses oneOf typed coordinate arrays instead of generic object")
    @SuppressWarnings("rawtypes")
    void testGeoJsonCoordinatesTypedAsPointOrPolygonArrays() {
        Map<String, Schema> schemas = buildCustomizedMieruOpenApi().getComponents().getSchemas();
        Schema geo = schemas.get("GeoJsonGeometryDto");
        assertNotNull(geo);
        assertTrue(geo.getRequired().containsAll(List.of("type", "coordinates")));

        Schema typeProp = (Schema) geo.getProperties().get("type");
        assertEquals(List.of("Point", "Polygon"), typeProp.getEnum());

        Schema coordsProp = (Schema) geo.getProperties().get("coordinates");
        assertNotNull(coordsProp.getOneOf(), "coordinates must define oneOf [Point, Polygon]");
        assertEquals(2, coordsProp.getOneOf().size());
    }

    @Test
    @DisplayName("Customizer preserves 200 OK while attaching 400/401/403/404/429/500 responses and matching primaryName")
    void testErrorResponsesPreserving200AndPrimaryNameMatch() throws Exception {
        assertNull(IntegrationV1Controller.class.getAnnotation(io.swagger.v3.oas.annotations.responses.ApiResponses.class),
                "Class-level @ApiResponses without 200 suppresses Springdoc return-type introspection and must not be used");

        OpenAPI openApi = buildCustomizedMieruOpenApi();
        assertTrue(openApi.getComponents().getSchemas().containsKey("ApiDefaultResponse"),
                "ApiDefaultResponse schema must be registered for $ref resolution");

        ApiResponses collectionResponses = openApi.getPaths().get("/api/integration/v1/shipments").getGet().getResponses();
        assertEquals(Set.of("200", "400", "401", "403", "429", "500"), collectionResponses.keySet());
        assertNotNull(collectionResponses.get("429").getHeaders().get("Retry-After"));

        for (String idRoute : List.of(
                "/api/integration/v1/farmers/{id}",
                "/api/integration/v1/deliveries/{id}",
                "/api/integration/v1/shipments/{id}/composition"
        )) {
            ApiResponses itemResponses = openApi.getPaths().get(idRoute).getGet().getResponses();
            assertEquals(Set.of("200", "400", "401", "403", "404", "429", "500"), itemResponses.keySet(),
                    "Route " + idRoute + " must retain 200 and include 404");
        }

        for (String relPath : List.of("src/main/resources/application.properties", "src/main/resources/application.properties.template")) {
            Properties props = new Properties();
            try (InputStream in = java.nio.file.Files.newInputStream(java.nio.file.Path.of(relPath))) {
                props.load(in);
            }
            assertEquals(OpenAPIConfiguration.MIERU_GROUP_DISPLAY_NAME,
                    props.getProperty("springdoc.swagger-ui.urls-primary-name"),
                    "Mismatch in " + relPath);
        }
    }
}
