package com.tiendaenlinea.reactiva.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderR2dbcRepository extends ReactiveCrudRepository<OrderEntity, UUID> {

    @Query("""
                SELECT *
                FROM orders
                WHERE status = 'PENDING'
                AND created_at < :limite
            """)

    Flux<OrderEntity> findPendingOrdersOlderThan(Instant limite);

    Mono<Long> countByStatus(String status);

    @Query("""
                SELECT COALESCE(SUM(total_amount),0)
                FROM orders
            """)
    Mono<BigDecimal> totalVentas();
}

// esto de aqui busca pedidos
// en estado pending
// busca pedidos viejos que cierta fecha