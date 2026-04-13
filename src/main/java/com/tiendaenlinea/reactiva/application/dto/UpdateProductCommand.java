package com.tiendaenlinea.reactiva.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateProductCommand(
		@NotBlank @Size(max = 255) String name,
		@Size(max = 4000) String description,
		@NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal price,
		@NotNull @Min(0) Integer stock,
		UUID categoryId
) {
}
