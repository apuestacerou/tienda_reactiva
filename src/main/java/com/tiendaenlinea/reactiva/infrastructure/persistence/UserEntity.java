package com.tiendaenlinea.reactiva.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("users")
public class UserEntity implements Persistable<UUID> {

	@Id
	@Column("id")
	private UUID id;

	/**
	 * Ids generados en la app: Spring Data R2DBC debe hacer INSERT; sin esto, ve id != null y hace UPDATE a 0 filas.
	 */
	@Transient
	private boolean newRow;
	@Column("email")
	private String email;
	@Column("password_hash")
	private String passwordHash;
	@Column("full_name")
	private String fullName;
	@Column("role")
	private String role;
	@Column("created_at")
	private Instant createdAt;
	@Column("updated_at")
	private Instant updatedAt;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	/** Solo para altas nuevas (no llamar en entidades leídas de la BD). */
	public void markNewRow() {
		this.newRow = true;
	}

	@Override
	public boolean isNew() {
		return newRow;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}
