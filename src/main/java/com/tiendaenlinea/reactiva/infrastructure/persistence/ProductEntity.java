package com.tiendaenlinea.reactiva.infrastructure.persistence;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("products")
public class ProductEntity implements Persistable<UUID> {

	@Id
	@Column("id")
	private UUID id;

	@Transient
	private boolean newRow;
	@Column("name")
	private String name;
	@Column("description")
	private String description;
	@Column("price")
	private BigDecimal price;
	@Column("stock")
	private int stock;
	@Column("image_path")
	private String imagePath;
	@Column("category_id")
	private UUID categoryId;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public void markNewRow() {
		this.newRow = true;
	}

	@Override
	public boolean isNew() {
		return newRow;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public int getStock() {
		return stock;
	}

	public void setStock(int stock) {
		this.stock = stock;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public UUID getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(UUID categoryId) {
		this.categoryId = categoryId;
	}
}
