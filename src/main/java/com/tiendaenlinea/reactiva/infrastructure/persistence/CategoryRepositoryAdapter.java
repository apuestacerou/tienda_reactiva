package com.tiendaenlinea.reactiva.infrastructure.persistence;

import java.util.Collection;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.tiendaenlinea.reactiva.domain.model.Category;
import com.tiendaenlinea.reactiva.domain.port.CategoryRepositoryPort;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class CategoryRepositoryAdapter implements CategoryRepositoryPort {

	private final CategoryR2dbcRepository r2dbc;

	public CategoryRepositoryAdapter(CategoryR2dbcRepository r2dbc) {
		this.r2dbc = r2dbc;
	}

	@Override
	public Flux<Category> findAllOrderByName() {
		return r2dbc.findAllOrderByName().map(this::toDomain);
	}

	@Override
	public Mono<Category> findById(UUID id) {
		return r2dbc.findById(id).map(this::toDomain);
	}

	@Override
	public Flux<Category> findAllById(Collection<UUID> ids) {
		if (ids == null || ids.isEmpty()) {
			return Flux.empty();
		}
		return r2dbc.findAllById(ids).map(this::toDomain);
	}

	private Category toDomain(CategoryEntity e) {
		return new Category(e.getId(), e.getName(), e.getSlug());
	}
}
