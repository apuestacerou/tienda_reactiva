package com.tiendaenlinea.reactiva.infrastructure.web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tiendaenlinea.reactiva.application.dto.CategoryResponse;
import com.tiendaenlinea.reactiva.application.service.CategoryApplicationService;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping(value = "/api/categories", produces = MediaType.APPLICATION_JSON_VALUE)
public class CategoryController {

	private final CategoryApplicationService categoryService;

	public CategoryController(CategoryApplicationService categoryService) {
		this.categoryService = categoryService;
	}

	@GetMapping
	public Flux<CategoryResponse> listar() {
		return categoryService.listarTodos();
	}
}
