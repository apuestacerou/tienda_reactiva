package com.tiendaenlinea.reactiva.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Flux;

public interface ProductR2dbcRepository extends ReactiveCrudRepository<ProductEntity, UUID> {

    @Query("SELECT * FROM products ORDER BY name ASC")
    Flux<ProductEntity> findAllOrderByName();

    @Query("""
        SELECT *
        FROM products
        WHERE stock <= 0
    """)
    Flux<ProductEntity> findOutOfStockProducts();
}
