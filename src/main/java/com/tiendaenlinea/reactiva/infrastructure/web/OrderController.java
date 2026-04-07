package com.tiendaenlinea.reactiva.infrastructure.web;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tiendaenlinea.reactiva.application.dto.order.CreateOrderRequest;
import com.tiendaenlinea.reactiva.application.dto.order.OrderCreatedResponse;
import com.tiendaenlinea.reactiva.application.service.OrderCheckoutService;

import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderCheckoutService orderCheckoutService;

	public OrderController(OrderCheckoutService orderCheckoutService) {
		this.orderCheckoutService = orderCheckoutService;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public Mono<OrderCreatedResponse> crearPedido(@Valid @RequestBody CreateOrderRequest request) {
		return ReactiveSecurityContextHolder.getContext()
				.map(SecurityContext::getAuthentication)
				.map(a -> UUID.fromString(a.getName()))
				.flatMap(userId -> orderCheckoutService.checkout(userId, request));
	}
}
