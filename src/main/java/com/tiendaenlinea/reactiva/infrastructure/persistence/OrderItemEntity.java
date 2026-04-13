package com.tiendaenlinea.reactiva.infrastructure.persistence;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("order_items")
public class OrderItemEntity implements Persistable<UUID> {

	@Id
	@Column("id")
	private UUID id;

	@Transient
	private boolean newRow;
	@Column("order_id")
	private UUID orderId;
	@Column("product_id")
	private UUID productId;
	@Column("quantity")
	private int quantity;
	@Column("unit_price")
	private BigDecimal unitPrice;

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

	public UUID getOrderId() {
		return orderId;
	}

	public void setOrderId(UUID orderId) {
		this.orderId = orderId;
	}

	public UUID getProductId() {
		return productId;
	}

	public void setProductId(UUID productId) {
		this.productId = productId;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}
}
