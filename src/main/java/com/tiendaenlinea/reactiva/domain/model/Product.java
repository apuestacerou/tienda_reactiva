package com.tiendaenlinea.reactiva.domain.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public final class Product {

	private final UUID id;
	private final String name;
	private final String description;
	private final BigDecimal price;
	private final int stock;
	private final String imagePath;

	public Product(UUID id, String name, String description, BigDecimal price, int stock, String imagePath) {
		this.id = Objects.requireNonNull(id);
		this.name = Objects.requireNonNull(name, "name");
		this.description = description != null ? description : "";
		this.price = Objects.requireNonNull(price, "price");
		if (price.signum() < 0) {
			throw new IllegalArgumentException("price must be >= 0");
		}
		if (stock < 0) {
			throw new IllegalArgumentException("stock must be >= 0");
		}
		this.stock = stock;
		this.imagePath = imagePath;
	}

	public static Product nuevo(String name, String description, BigDecimal price, int stock, String imagePath) {
		return new Product(UUID.randomUUID(), name, description, price, stock, imagePath);
	}

	public Product withStock(int newStock) {
		return new Product(id, name, description, price, newStock, imagePath);
	}

	public Product withImagePath(String newImagePath) {
		return new Product(id, name, description, price, stock, newImagePath);
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public int getStock() {
		return stock;
	}

	public String getImagePath() {
		return imagePath;
	}
}
