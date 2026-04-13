package com.tiendaenlinea.reactiva.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	SecurityWebFilterChain springSecurityFilterChain(
			ServerHttpSecurity http,
			ReactiveAuthenticationManager jwtAuthenticationManager,
			BearerTokenAuthenticationConverter bearerConverter) {

		AuthenticationWebFilter jwtFilter = new AuthenticationWebFilter(jwtAuthenticationManager);
		jwtFilter.setServerAuthenticationConverter(bearerConverter);
		jwtFilter.setSecurityContextRepository(NoOpServerSecurityContextRepository.getInstance());

		return http
				.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
				.formLogin(ServerHttpSecurity.FormLoginSpec::disable)
				.securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
				.authorizeExchange(ex -> ex
						.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.pathMatchers("/api/auth/**").permitAll()
						.pathMatchers(HttpMethod.GET, "/api/products/**").permitAll()
						.pathMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
						.pathMatchers("/api/files/**").permitAll()
						.pathMatchers(HttpMethod.POST, "/api/products").hasRole("ADMINISTRADOR")
						.pathMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMINISTRADOR")
						.pathMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMINISTRADOR")
						.pathMatchers(HttpMethod.POST, "/api/orders").authenticated()
						.anyExchange().permitAll())
				.addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
				.build();
	}

}
