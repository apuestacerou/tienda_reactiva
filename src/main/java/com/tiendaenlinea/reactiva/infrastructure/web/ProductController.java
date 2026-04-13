package com.tiendaenlinea.reactiva.infrastructure.web;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.tiendaenlinea.reactiva.application.dto.CreateProductCommand;
import com.tiendaenlinea.reactiva.application.dto.ProductResponse;
import com.tiendaenlinea.reactiva.application.dto.UpdateProductCommand;
import com.tiendaenlinea.reactiva.application.service.ProductApplicationService;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/products")
public class ProductController {

	private final ProductApplicationService productService;
	private final Validator validator;

	public ProductController(ProductApplicationService productService, Validator validator) {
		this.productService = productService;
		this.validator = validator;
	}

	@GetMapping
	public Flux<ProductResponse> listar() {
		return productService.listarTodos();
	}

	@GetMapping("/{id}")
	public Mono<ProductResponse> obtener(@PathVariable UUID id) {
		return productService.obtenerPorId(id);
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Mono<ProductResponse> crear(
			@RequestPart("name") String name,
			@RequestPart(value = "description", required = false) String description,
			@RequestPart("price") String priceStr,
			@RequestPart("stock") String stockStr,
			@RequestPart(value = "categoryId", required = false) String categoryIdStr,
			@RequestPart(value = "image", required = false) FilePart image) {
		CreateProductCommand cmd = new CreateProductCommand(
				name,
				description != null ? description : "",
				new BigDecimal(priceStr.trim()),
				Integer.parseInt(stockStr.trim()),
				parseCategoryId(categoryIdStr));
		validar(cmd);
		Mono<byte[]> imagen = image != null ? toBytes(image) : Mono.empty();
		String nombreArchivo = image != null ? image.filename() : "";
		return productService.crear(cmd, imagen, nombreArchivo);
	}

	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Mono<ProductResponse> actualizar(
			@PathVariable UUID id,
			@RequestPart("name") String name,
			@RequestPart(value = "description", required = false) String description,
			@RequestPart("price") String priceStr,
			@RequestPart("stock") String stockStr,
			@RequestPart(value = "categoryId", required = false) String categoryIdStr,
			@RequestPart(value = "image", required = false) FilePart image) {
		UpdateProductCommand cmd = new UpdateProductCommand(
				name,
				description != null ? description : "",
				new BigDecimal(priceStr.trim()),
				Integer.parseInt(stockStr.trim()),
				parseCategoryId(categoryIdStr));
		validar(cmd);
		Mono<byte[]> imagen = image != null ? toBytes(image) : Mono.empty();
		String nombreArchivo = image != null ? image.filename() : "";
		return productService.actualizar(id, cmd, imagen, nombreArchivo);
	}

	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> eliminar(@PathVariable UUID id) {
		return productService.eliminar(id).then(Mono.just(ResponseEntity.noContent().build()));
	}

	private static UUID parseCategoryId(String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		try {
			return UUID.fromString(raw.trim());
		} catch (IllegalArgumentException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "categoryId invalido");
		}
	}

	private void validar(Object cmd) {
		Set<ConstraintViolation<Object>> violations = validator.validate(cmd);
		if (!violations.isEmpty()) {
			String msg = violations.stream().map(ConstraintViolation::getMessage).reduce((a, b) -> a + "; " + b)
					.orElse("Validacion fallida");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
		}
	}

	private static Mono<byte[]> toBytes(FilePart part) {
		return DataBufferUtils.join(part.content())
				.map(dataBuffer -> {
					try {
						byte[] bytes = new byte[dataBuffer.readableByteCount()];
						dataBuffer.read(bytes);
						return bytes;
					} finally {
						DataBufferUtils.release(dataBuffer);
					}
				});
	}
}
