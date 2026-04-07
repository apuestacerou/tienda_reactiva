package com.tiendaenlinea.reactiva.application.dto.order;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderLineRequest(
		@NotNull UUID productId,
		@NotNull @Min(1) Integer quantity
) {
}
