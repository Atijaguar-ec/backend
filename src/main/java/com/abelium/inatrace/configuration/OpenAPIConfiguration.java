package com.abelium.inatrace.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class OpenAPIConfiguration {

	public static final String MIERU_GROUP_DISPLAY_NAME = "1. Interoperabilidad Mieru Cacao (Japón / JICA - UNOCACE)";

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("INATrace Services API")
						.description("INATrace Services API OpenAPI documentation")
						.version("1.0")
						.license(new License().name("AGPL-3.0 license")))
				.schemaRequirement("Access token", new SecurityScheme()
						.type(SecurityScheme.Type.APIKEY)
						.in(SecurityScheme.In.COOKIE)
						.name("inatrace-accessToken"))
				.schemaRequirement("Bearer Authentication", new SecurityScheme()
						.type(SecurityScheme.Type.HTTP)
						.in(SecurityScheme.In.HEADER)
						.bearerFormat("JWT")
						.scheme("bearer"))
				.specVersion(SpecVersion.V31);
	}

	@Bean
	public GroupedOpenApi mieruCacaoOpenApi() {
		return GroupedOpenApi.builder()
				.group("mieru-cacao")
				.displayName(MIERU_GROUP_DISPLAY_NAME)
				.pathsToMatch("/**/integration/v1/**")
				.addOpenApiCustomizer(OpenAPIConfiguration::customizeMieruCacaoOpenApi)
				.build();
	}

	/**
	 * Keeps the Mieru Cacao OpenAPI specification strictly valid and unambiguous for code generators:
	 * 1. Exposes only OAuth2 Bearer JWT ("Bearer Authentication") in securitySchemes.
	 * 2. Replaces the generic Java Object type on GeoJsonGeometryDto.coordinates with RFC 7946 Point / Polygon arrays.
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static void customizeMieruCacaoOpenApi(OpenAPI openApi) {
		if (openApi == null || openApi.getComponents() == null) {
			return;
		}
		if (openApi.getComponents().getSecuritySchemes() != null) {
			openApi.getComponents().getSecuritySchemes().remove("Access token");
		}
		Map<String, Schema> schemas = openApi.getComponents().getSchemas();
		if (schemas == null) {
			return;
		}
		Schema geoSchema = schemas.get("GeoJsonGeometryDto");
		if (geoSchema != null && geoSchema.getProperties() != null) {
			Schema lonLatPair = new ArraySchema()
					.minItems(2)
					.maxItems(2)
					.items(new NumberSchema().format("double"))
					.description("[longitude, latitude] in WGS84 (EPSG:4326)");

			Schema polygonRing = new ArraySchema()
					.minItems(4)
					.items(lonLatPair)
					.description("Closed counter-clockwise GeoJSON ring");

			Schema polygonCoordinates = new ArraySchema()
					.minItems(1)
					.items(polygonRing)
					.description("Polygon coordinates: [[[lon, lat], ...]]");

			Schema coordinatesSchema = new Schema<>()
					.description("WGS84 coordinates: [lon, lat] for Point or [[[lon, lat], ...]] closed CCW ring for Polygon")
					.oneOf(List.of(lonLatPair, polygonCoordinates));

			geoSchema.getProperties().put("coordinates", coordinatesSchema);
		}
	}

	@Bean
	public GroupedOpenApi erpOpenApi() {
		return GroupedOpenApi.builder()
				.group("erp-interoperabilidad")
				.displayName("2. Interoperabilidad ERP Contable & Báscula (Fortaleza del Valle)")
				.pathsToMatch(
						"/**/company/userCustomers/**/plots",
						"/**/company/userCustomers/**/FARMER",
						"/**/company/userCustomers/add/**",
						"/**/chain/stock-order",
						"/**/chain/stock-order/quota-balance",
						"/**/chain/stock-order/**/cancel",
						"/**/chain/stock-order/list/company/**",
						"/**/public/delivery-receipt/**"
				)
				.build();
	}

	@Bean
	public GroupedOpenApi allOpenApi() {
		return GroupedOpenApi.builder()
				.group("sistema-completo")
				.displayName("3. Sistema Completo INATrace (Todos los módulos)")
				.pathsToMatch("/**")
				.build();
	}

}
