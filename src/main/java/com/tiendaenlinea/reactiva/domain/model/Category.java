package com.tiendaenlinea.reactiva.domain.model;

import java.util.Objects;
import java.util.UUID;

public final class Category {

	private final UUID id;
	private final String name;
	private final String slug;

	public Category(UUID id, String name, String slug) {
		this.id = Objects.requireNonNull(id);
		this.name = Objects.requireNonNull(name, "name");
		this.slug = Objects.requireNonNull(slug, "slug");
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getSlug() {
		return slug;
	}
}
