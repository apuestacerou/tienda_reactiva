package com.tiendaenlinea.reactiva.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface OrderItemR2dbcRepository extends ReactiveCrudRepository<OrderItemEntity, UUID> {
}
