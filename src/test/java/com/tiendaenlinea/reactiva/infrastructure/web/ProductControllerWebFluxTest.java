package com.tiendaenlinea.reactiva.infrastructure.web;

import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.web.reactive.ReactiveWebSecurityAutoConfiguration;
import org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.tiendaenlinea.reactiva.application.dto.ProductResponse;
import com.tiendaenlinea.reactiva.application.service.ProductApplicationService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = ProductController.class, excludeAutoConfiguration = ReactiveWebSecurityAutoConfiguration.class)
@Import(ValidationAutoConfiguration.class)
class ProductControllerWebFluxTest {

	private static final UUID ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

	@Autowired
	private WebTestClient webTestClient;

	@MockitoBean
	private ProductApplicationService productApplicationService;

	@Test
	@DisplayName("GET /api/products — WebFlux devuelve JSON (Flux)")
	void listar_ok() {
		when(productApplicationService.listarTodos()).thenReturn(Flux.just(
				new ProductResponse(ID, "P", "d", new BigDecimal("1"), 1, null)));

		webTestClient.get()
				.uri("/api/products")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$[0].name").isEqualTo("P");
	}

	@Test
	@DisplayName("GET /api/products/{id} — Mono<ProductResponse>")
	void obtener_ok() {
		when(productApplicationService.obtenerPorId(ID)).thenReturn(Mono.just(
				new ProductResponse(ID, "Uno", "", new BigDecimal("9.99"), 3, "/api/files/x.jpg")));

		webTestClient.get()
				.uri("/api/products/" + ID)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.name").isEqualTo("Uno")
				.jsonPath("$.stock").isEqualTo(3);
	}
}
