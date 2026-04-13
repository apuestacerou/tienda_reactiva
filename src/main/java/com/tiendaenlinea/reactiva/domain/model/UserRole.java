package com.tiendaenlinea.reactiva.domain.model;

/** Valores de la columna {@code users.role} en Neon. */
public enum UserRole {

	CLIENTE,
	ADMINISTRADOR;

	public static String defaultRegistrationRole() {
		return CLIENTE.name();
	}
}
