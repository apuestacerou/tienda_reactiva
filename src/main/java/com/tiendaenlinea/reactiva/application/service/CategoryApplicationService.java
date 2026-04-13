package com.tiendaenlinea.reactiva.application.service;

import org.springframework.stereotype.Service;

import com.tiendaenlinea.reactiva.application.dto.CategoryResponse;
import com.tiendaenlinea.reactiva.domain.port.CategoryRepositoryPort;

import reactor.core.publisher.Flux;

@Service
public class CategoryApplicationService {

	private final CategoryRepositoryPort categoryRepository;

	public CategoryApplicationService(CategoryRepositoryPort categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	public Flux<CategoryResponse> listarTodos() {
		return categoryRepository.findAllOrderByName()
				.map(c -> new CategoryResponse(c.getId(), c.getName(), c.getSlug()));
	}
}
