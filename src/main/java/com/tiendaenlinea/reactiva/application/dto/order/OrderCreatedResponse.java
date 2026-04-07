package com.tiendaenlinea.reactiva.application.dto.order;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderCreatedResponse(UUID orderId, BigDecimal totalAmount, String status) {
}
