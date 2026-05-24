package com.tiendaenlinea.reactiva.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Flux;

public interface OrderR2dbcRepository extends ReactiveCrudRepository<OrderEntity, UUID> {

    @Query("""
        SELECT * 
        FROM orders 
        WHERE status = 'PENDING'
        AND created_at < :limite
    """)
    Flux<OrderEntity> findPendingOrdersOlderThan(Instant limite);
}

//esto de aqui busca pedidos
//en estado pending
//busca pedidos viejos que cierta fecha