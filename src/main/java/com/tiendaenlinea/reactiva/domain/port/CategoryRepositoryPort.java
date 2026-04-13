package com.tiendaenlinea.reactiva.domain.port;

import java.util.Collection;
import java.util.UUID;

import com.tiendaenlinea.reactiva.domain.model.Category;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CategoryRepositoryPort {

	Flux<Category> findAllOrderByName();

	Mono<Category> findById(UUID id);

	Flux<Category> findAllById(Collection<UUID> ids);
}
