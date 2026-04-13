package com.tiendaenlinea.reactiva.application.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.tiendaenlinea.reactiva.application.dto.order.CreateOrderRequest;
import com.tiendaenlinea.reactiva.application.dto.order.OrderCreatedResponse;
import com.tiendaenlinea.reactiva.application.dto.order.OrderLineRequest;
import com.tiendaenlinea.reactiva.domain.model.Product;
import com.tiendaenlinea.reactiva.domain.port.ProductRepositoryPort;
import com.tiendaenlinea.reactiva.infrastructure.persistence.OrderEntity;
import com.tiendaenlinea.reactiva.infrastructure.persistence.OrderItemEntity;
import com.tiendaenlinea.reactiva.infrastructure.persistence.OrderItemR2dbcRepository;
import com.tiendaenlinea.reactiva.infrastructure.persistence.OrderR2dbcRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrderCheckoutService {

	private final ProductRepositoryPort productRepository;
	private final OrderR2dbcRepository orderRepository;
	private final OrderItemR2dbcRepository orderItemRepository;

	public OrderCheckoutService(
			ProductRepositoryPort productRepository,
			OrderR2dbcRepository orderRepository,
			OrderItemR2dbcRepository orderItemRepository) {
		this.productRepository = productRepository;
		this.orderRepository = orderRepository;
		this.orderItemRepository = orderItemRepository;
	}

	public Mono<OrderCreatedResponse> checkout(UUID userId, CreateOrderRequest req) {
		return Flux.fromIterable(req.items())
				.concatMap(this::cargarProductoCantidad)
				.collectList()
				.flatMap(lineas -> {
					if (lineas.isEmpty()) {
						return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Carrito vacio"));
					}
					BigDecimal total = lineas.stream()
							.map(l -> l.product().getPrice().multiply(BigDecimal.valueOf(l.cantidad())))
							.reduce(BigDecimal.ZERO, BigDecimal::add);
					UUID orderId = UUID.randomUUID();
					OrderEntity order = new OrderEntity();
					order.setId(orderId);
					order.setUserId(userId);
					order.setStatus("PENDING");
					order.setTotalAmount(total);
					order.setCreatedAt(Instant.now());
					order.markNewRow();
					return orderRepository.save(order)
							.flatMap(o -> guardarLineas(o.getId(), lineas).then(actualizarStocks(lineas))
									.thenReturn(new OrderCreatedResponse(o.getId(), total, o.getStatus())));
				});
	}

	private Mono<LineaConProducto> cargarProductoCantidad(OrderLineRequest line) {
		return productRepository.findById(line.productId())
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto no existe: " + line.productId())))
				.flatMap(p -> {
					if (p.getStock() < line.quantity()) {
						return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
								"Stock insuficiente para " + p.getName()));
					}
					return Mono.just(new LineaConProducto(p, line.quantity()));
				});
	}

	private Mono<Void> guardarLineas(UUID orderId, List<LineaConProducto> lineas) {
		return Flux.fromIterable(lineas)
				.concatMap(l -> {
					OrderItemEntity item = new OrderItemEntity();
					item.setId(UUID.randomUUID());
					item.setOrderId(orderId);
					item.setProductId(l.product().getId());
					item.setQuantity(l.cantidad());
					item.setUnitPrice(l.product().getPrice());
					item.markNewRow();
					return orderItemRepository.save(item);
				})
				.then();
	}

	private Mono<Void> actualizarStocks(List<LineaConProducto> lineas) {
		return Flux.fromIterable(lineas)
				.concatMap(l -> {
					Product p = l.product();
					int nuevo = p.getStock() - l.cantidad();
					Product actualizado = p.withStock(nuevo);
					return productRepository.save(actualizado);
				})
				.then();
	}

	private record LineaConProducto(Product product, int cantidad) {
	}
}
