package com.tiendaenlinea.reactiva.infrastructure.web;

import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class WebConfig implements WebFluxConfigurer {

	@Bean
	CorsWebFilter corsWebFilter() {
		CorsConfiguration config = new CorsConfiguration();
		config.addAllowedOriginPattern("*");
		config.addAllowedMethod("*");
		config.addAllowedHeader("*");
		config.addExposedHeader("Authorization");
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return new CorsWebFilter(source);
	}

	@Value("${tienda.uploads.dir}")
	private String uploadDir;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		String location = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString();
		if (!location.endsWith("/")) {
			location = location + "/";
		}
		registry.addResourceHandler("/api/files/**")
				.addResourceLocations(location);
	}

}
