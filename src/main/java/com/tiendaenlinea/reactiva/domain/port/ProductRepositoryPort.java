package com.tiendaenlinea.reactiva.domain.port;

import java.util.UUID;

import com.tiendaenlinea.reactiva.domain.model.Product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepositoryPort {

	Mono<Product> save(Product product);

	Mono<Product> findById(UUID id);

	Flux<Product> findAllOrderByName();

	Mono<Void> deleteById(UUID id);
}
