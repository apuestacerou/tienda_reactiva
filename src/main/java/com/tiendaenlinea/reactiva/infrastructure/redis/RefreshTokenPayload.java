package com.tiendaenlinea.reactiva.infrastructure.redis;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RefreshTokenPayload(UUID userId, String email, String role) {
}
