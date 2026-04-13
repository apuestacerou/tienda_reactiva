package com.tiendaenlinea.reactiva.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtService {

	private final SecretKey key;
	private final long expirationMs;

	public JwtService(
			@Value("${tienda.jwt.secret:change-this-secret-min-256-bits-for-hs256-algorithm-change-me-now}") String secret,
			@Value("${tienda.jwt.expiration-ms:86400000}") long expirationMs) {
		byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
		if (bytes.length < 32) {
			throw new IllegalArgumentException("tienda.jwt.secret debe tener al menos 32 bytes");
		}
		this.key = Keys.hmacShaKeyFor(bytes);
		this.expirationMs = expirationMs;
	}

	public String createToken(UUID userId, String email, String role) {
		Instant now = Instant.now();
		var builder = Jwts.builder()
				.subject(userId.toString())
				.claim("email", email)
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plusMillis(expirationMs)));
		if (role != null && !role.isBlank()) {
			builder.claim("role", role);
		}
		return builder.signWith(key).compact();
	}

	public Claims parseValid(String token) {
		return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	public UUID extractUserId(String token) {
		return UUID.fromString(parseValid(token).getSubject());
	}
}
