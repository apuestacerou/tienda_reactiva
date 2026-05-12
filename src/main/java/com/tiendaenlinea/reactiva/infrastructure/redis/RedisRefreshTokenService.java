package com.tiendaenlinea.reactiva.infrastructure.redis;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

/**
 * Refresh tokens opacos en Redis. Claves: {@code refresh:<token>} y {@code user_token:<userId>}.
 */
@Service
public class RedisRefreshTokenService {

	public static final String REFRESH_PREFIX = "refresh:";
	public static final String USER_TOKEN_PREFIX = "user_token:";

	private final ReactiveStringRedisTemplate redis;
	private final ObjectMapper objectMapper;
	private final Duration refreshTtl;

	public RedisRefreshTokenService(
			ReactiveStringRedisTemplate redis,
			ObjectMapper objectMapper,
			@Value("${tienda.jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs) {
		this.redis = redis;
		this.objectMapper = objectMapper;
		this.refreshTtl = Duration.ofMillis(refreshExpirationMs);
	}

	/**
	 * Emite un refresh nuevo; revoca el anterior del mismo usuario si existía.
	 */
	public Mono<String> issueForUser(UUID userId, String email, String role) {
		String userKey = USER_TOKEN_PREFIX + userId;
		return revokePreviousRefreshForUser(userKey)
				.then(Mono.defer(() -> {
					String newToken = UUID.randomUUID().toString();
					String refreshKey = REFRESH_PREFIX + newToken;
					String json = writeJson(new RefreshTokenPayload(userId, email, role));
					return redis.opsForValue().set(refreshKey, json, refreshTtl)
							.flatMap(ok -> Boolean.TRUE.equals(ok)
									? redis.opsForValue().set(userKey, newToken, refreshTtl).thenReturn(newToken)
									: Mono.error(new IllegalStateException("Redis SET refresh fallo")));
				}));
	}

	private Mono<Void> revokePreviousRefreshForUser(String userKey) {
		return redis.opsForValue().get(userKey)
				.flatMap(oldRt -> redis.delete(REFRESH_PREFIX + oldRt).then())
				.then();
	}

	/**
	 * Valida el refresh, comprueba que siga siendo el activo del usuario, rota y devuelve el nuevo valor opaco.
	 */
	public Mono<RefreshRotationResult> rotate(String refreshToken) {
		if (refreshToken == null || refreshToken.isBlank()) {
			return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh requerido"));
		}
		String refreshKey = REFRESH_PREFIX + refreshToken;
		return redis.opsForValue().get(refreshKey)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh invalido")))
				.flatMap(json -> {
					RefreshTokenPayload payload = readJson(json);
					String userKey = USER_TOKEN_PREFIX + payload.userId();
					return redis.opsForValue().get(userKey)
							.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesion invalida")))
							.flatMap(current -> {
								if (!refreshToken.equals(current)) {
									return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh revocado"));
								}
								String newToken = UUID.randomUUID().toString();
								String newRefreshKey = REFRESH_PREFIX + newToken;
								return redis.delete(refreshKey)
										.then(redis.opsForValue().set(newRefreshKey, json, refreshTtl)
												.flatMap(ok -> Boolean.TRUE.equals(ok)
														? redis.opsForValue().set(userKey, newToken, refreshTtl)
																.thenReturn(new RefreshRotationResult(
																		payload.userId(),
																		payload.email(),
																		payload.role(),
																		newToken))
														: Mono.error(new IllegalStateException("Redis SET refresh fallo"))));
							});
				});
	}

	/**
	 * Invalida el par refresh / user_token si el token coincide con el puntero activo.
	 */
	public Mono<Void> revoke(String refreshToken) {
		if (refreshToken == null || refreshToken.isBlank()) {
			return Mono.empty();
		}
		String refreshKey = REFRESH_PREFIX + refreshToken;
		return redis.opsForValue().get(refreshKey)
				.flatMap(json -> {
					RefreshTokenPayload payload = readJson(json);
					String userKey = USER_TOKEN_PREFIX + payload.userId();
					return redis.opsForValue().get(userKey)
							.flatMap(current -> refreshToken.equals(current)
									? redis.delete(refreshKey).then(redis.delete(userKey))
									: redis.delete(refreshKey))
							.switchIfEmpty(redis.delete(refreshKey));
				})
				.then();
	}

	private String writeJson(RefreshTokenPayload p) {
		try {
			return objectMapper.writeValueAsString(p);
		}
		catch (JsonProcessingException e) {
			throw new IllegalStateException(e);
		}
	}

	private RefreshTokenPayload readJson(String json) {
		try {
			return objectMapper.readValue(json, RefreshTokenPayload.class);
		}
		catch (JsonProcessingException e) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh corrupto");
		}
	}
}
