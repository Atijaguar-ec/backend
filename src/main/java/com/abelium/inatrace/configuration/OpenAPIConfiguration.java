package com.abelium.inatrace.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfiguration {

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("INATrace Services API")
						.description("INATrace Services API OpenAPI documentation")
						.version("1.0")
						.license(new License().name("AGPL-3.0 license")))
				.schemaRequirement("Access token", new SecurityScheme()
						.type(SecurityScheme.Type.HTTP)
						.in(SecurityScheme.In.COOKIE)
						.name("inatrace-accessToken")
						.bearerFormat("JWT")
						.scheme("Bearer"))
				.schemaRequirement("Bearer Authentication", new SecurityScheme()
						.type(SecurityScheme.Type.HTTP)
						.in(SecurityScheme.In.HEADER)
						.bearerFormat("JWT")
						.scheme("bearer"))
				.specVersion(SpecVersion.V31);
	}

	@Bean
	public GroupedOpenApi erpOpenApi() {
		return GroupedOpenApi.builder()
				.group("erp-interoperabilidad")
				.displayName("1. Interoperabilidad ERP Contable & Báscula (Fortaleza del Valle)")
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
				.displayName("2. Sistema Completo INATrace (Todos los módulos)")
				.pathsToMatch("/**")
				.build();
	}

}
