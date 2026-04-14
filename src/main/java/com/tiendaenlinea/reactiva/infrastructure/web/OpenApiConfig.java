package com.tiendaenlinea.reactiva.infrastructure.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

	@Bean
	OpenAPI tiendaOpenAPI() {
		final String schemeName = "bearer-jwt";
		return new OpenAPI()
				.info(new Info()
						.title("Tienda en línea — API")
						.description("REST reactiva: catálogo, auth JWT, pedidos y administración de productos.")
						.version("1.0"))
				.components(new Components()
						.addSecuritySchemes(schemeName, new SecurityScheme()
								.name(schemeName)
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")));
	}
}
