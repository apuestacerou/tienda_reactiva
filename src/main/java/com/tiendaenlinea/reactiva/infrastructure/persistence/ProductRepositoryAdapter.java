package com.tiendaenlinea.reactiva.infrastructure.persistence;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.tiendaenlinea.reactiva.domain.model.Product;
import com.tiendaenlinea.reactiva.domain.port.ProductRepositoryPort;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ProductRepositoryAdapter implements ProductRepositoryPort {

	private final ProductR2dbcRepository r2dbc;

	public ProductRepositoryAdapter(ProductR2dbcRepository r2dbc) {
		this.r2dbc = r2dbc;
	}

	@Override
	public Mono<Product> save(Product product) {
		return r2dbc.save(toEntity(product)).map(this::toDomain);
	}

	@Override
	public Mono<Product> findById(UUID id) {
		return r2dbc.findById(id).map(this::toDomain);
	}

	@Override
	public Flux<Product> findAllOrderByName() {
		return r2dbc.findAllOrderByName().map(this::toDomain);
	}

	@Override
	public Mono<Void> deleteById(UUID id) {
		return r2dbc.deleteById(id).then();
	}

	private ProductEntity toEntity(Product p) {
		ProductEntity e = new ProductEntity();
		e.setId(p.getId());
		e.setName(p.getName());
		e.setDescription(p.getDescription());
		e.setPrice(p.getPrice());
		e.setStock(p.getStock());
		e.setImagePath(p.getImagePath());
		return e;
	}

	private Product toDomain(ProductEntity e) {
		return new Product(
				e.getId(),
				e.getName(),
				e.getDescription(),
				e.getPrice(),
				e.getStock(),
				e.getImagePath());
	}
}
