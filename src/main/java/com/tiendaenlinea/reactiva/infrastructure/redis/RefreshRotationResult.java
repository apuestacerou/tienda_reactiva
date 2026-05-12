package com.tiendaenlinea.reactiva.infrastructure.redis;

import java.util.UUID;

public record RefreshRotationResult(UUID userId, String email, String role, String newRefreshToken) {
}
