package com.tiendaenlinea.reactiva.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Mono;

public interface UserR2dbcRepository extends ReactiveCrudRepository<UserEntity, UUID> {

	@Query("SELECT * FROM users WHERE email = :email LIMIT 1")
	Mono<UserEntity> findByEmail(String email);
}
