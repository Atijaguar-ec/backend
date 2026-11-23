package com.abelium.inatrace.configuration;

import com.abelium.inatrace.api.ApiDefaultResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
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
	 * 3. Attaches standard error responses (400, 401, 403, 404 on {id} routes, 429 with Retry-After, 500) without
	 *    suppressing Springdoc's introspected 200 return-type schemas.
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static void customizeMieruCacaoOpenApi(OpenAPI openApi) {
		if (openApi == null) {
			return;
		}
		Components components = openApi.getComponents();
		if (components == null) {
			components = new Components();
			openApi.setComponents(components);
		}
		if (components.getSecuritySchemes() != null) {
			components.getSecuritySchemes().remove("Access token");
		}
		Map<String, Schema> schemas = components.getSchemas();
		if (schemas == null) {
			schemas = new LinkedHashMap<>();
			components.setSchemas(schemas);
		}

		// Ensure ApiDefaultResponse error envelope is registered in components.schemas
		Map<String, Schema> errorSchemas = ModelConverters.getInstance().readAll(ApiDefaultResponse.class);
		if (errorSchemas != null) {
			for (Map.Entry<String, Schema> entry : errorSchemas.entrySet()) {
				schemas.putIfAbsent(entry.getKey(), entry.getValue());
			}
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

		// Attach error responses onto every operation while preserving 200 OK
		if (openApi.getPaths() != null) {
			for (Map.Entry<String, PathItem> pathEntry : openApi.getPaths().entrySet()) {
				String path = pathEntry.getKey();
				PathItem item = pathEntry.getValue();
				if (item == null || item.readOperations() == null) {
					continue;
				}
				boolean hasIdParam = path != null && path.contains("{id}");
				for (Operation op : item.readOperations()) {
					ApiResponses responses = op.getResponses();
					if (responses == null) {
						responses = new ApiResponses();
						op.setResponses(responses);
					}
					responses.putIfAbsent("400", jsonErrorResponse(
							"Invalid or missing query parameter (INVALID_REQUEST / REQUEST_BODY_ERROR)"));
					responses.putIfAbsent("401", new ApiResponse()
							.description("Missing, expired, or invalid OAuth 2.0 Bearer JWT token"));
					responses.putIfAbsent("403", jsonErrorResponse(
							"Unauthorized organization or restricted M2M scope (FORBIDDEN_TENANT_ACCESS)"));
					if (hasIdParam) {
						responses.putIfAbsent("404", jsonErrorResponse(
								"Requested resource not found with the specified ID"));
					}
					ApiResponse rateLimitResp = jsonErrorResponse("Request rate limit exceeded");
					rateLimitResp.addHeaderObject("Retry-After", new Header()
							.description("Seconds (or HTTP-date) to wait before retrying")
							.schema(new StringSchema()));
					responses.putIfAbsent("429", rateLimitResp);
					responses.putIfAbsent("500", jsonErrorResponse("Unexpected server error"));
				}
			}
		}
	}

	private static ApiResponse jsonErrorResponse(String description) {
		Schema<?> ref = new Schema<>().$ref("#/components/schemas/ApiDefaultResponse");
		Content content = new Content().addMediaType("application/json", new MediaType().schema(ref));
		return new ApiResponse().description(description).content(content);
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
