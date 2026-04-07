package com.tiendaenlinea.reactiva.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface OrderR2dbcRepository extends ReactiveCrudRepository<OrderEntity, UUID> {
}
