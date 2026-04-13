package com.tiendaenlinea.reactiva.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Flux;

public interface CategoryR2dbcRepository extends ReactiveCrudRepository<CategoryEntity, UUID> {

	@Query("SELECT * FROM categories ORDER BY name ASC")
	Flux<CategoryEntity> findAllOrderByName();
}
