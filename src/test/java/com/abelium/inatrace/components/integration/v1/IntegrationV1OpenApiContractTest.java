package com.abelium.inatrace.components.integration.v1;

import com.abelium.inatrace.components.integration.v1.dto.*;
import com.abelium.inatrace.configuration.OpenAPIConfiguration;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies the OpenAPI 3.0/3.1 contract for /api/integration/v1 against schema collisions,
 * invalid securityScheme combinations, untyped GeoJSON coordinates, missing required/enum metadata,
 * and Swagger UI primaryName mismatches.
 */
class IntegrationV1OpenApiContractTest {

    @SuppressWarnings("rawtypes")
    private Map<String, Schema> resolveAllIntegrationSchemas() {
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
        OpenAPIConfiguration.customizeMieruCacaoOpenApi(openApi);
        return openApi.getComponents().getSchemas();
    }

    @Test
    @DisplayName("ReferenceUnitDto preserves 'label' while UnitSummaryDto keeps 2 fields without collision")
    @SuppressWarnings("rawtypes")
    void testReferenceUnitDtoAndUnitSummaryDtoDoNotCollide() {
        Map<String, Schema> schemas = resolveAllIntegrationSchemas();

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
    @DisplayName("ShipmentLotCodesDto and CompositionLotCodesDto are distinct schemas without extra fields")
    @SuppressWarnings("rawtypes")
    void testLotCodesSchemasDoNotCollide() {
        Map<String, Schema> schemas = resolveAllIntegrationSchemas();

        assertFalse(schemas.containsKey("LotCodesDto"), "Ambiguous 'LotCodesDto' schema name must not exist");
        Schema shipmentLots = schemas.get("ShipmentLotCodesDto");
        assertNotNull(shipmentLots);
        assertEquals(Set.of("internal_lot_number", "order_id", "qr_code_tag", "sac_numbers"),
                shipmentLots.getProperties().keySet());

        Schema compLots = schemas.get("CompositionLotCodesDto");
        assertNotNull(compLots);
        assertEquals(Set.of("internal_lot_number", "qr_code_tag"),
                compLots.getProperties().keySet());
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
        Map<String, Schema> schemas = resolveAllIntegrationSchemas();
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
    @DisplayName("Controller declares error responses and springdoc urls-primary-name matches displayName")
    void testControllerResponsesAndPrimaryNameMatch() throws Exception {
        io.swagger.v3.oas.annotations.responses.ApiResponses classResponses =
                IntegrationV1Controller.class.getAnnotation(io.swagger.v3.oas.annotations.responses.ApiResponses.class);
        assertNotNull(classResponses, "IntegrationV1Controller must declare class-level @ApiResponses");
        Set<String> codes = new HashSet<>();
        for (var r : classResponses.value()) {
            codes.add(r.responseCode());
        }
        assertEquals(Set.of("400", "401", "403", "429", "500"), codes);

        for (String methodName : List.of("getFarmerById", "getDeliveryById", "getShipmentComposition")) {
            Method m = Arrays.stream(IntegrationV1Controller.class.getDeclaredMethods())
                    .filter(it -> it.getName().equals(methodName))
                    .findFirst()
                    .orElseThrow();
            var r404 = m.getAnnotation(io.swagger.v3.oas.annotations.responses.ApiResponse.class);
            assertNotNull(r404, methodName + " must declare 404 response");
            assertEquals("404", r404.responseCode());
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
