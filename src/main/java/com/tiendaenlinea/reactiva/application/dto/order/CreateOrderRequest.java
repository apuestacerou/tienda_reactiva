package com.tiendaenlinea.reactiva.application.dto.order;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record CreateOrderRequest(
		@NotEmpty @Valid List<OrderLineRequest> items
) {
}
