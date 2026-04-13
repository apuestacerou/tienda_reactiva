package com.tiendaenlinea.reactiva.application.service;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import com.tiendaenlinea.reactiva.application.dto.CreateProductCommand;
import com.tiendaenlinea.reactiva.application.dto.ProductResponse;
import com.tiendaenlinea.reactiva.application.dto.UpdateProductCommand;
import com.tiendaenlinea.reactiva.domain.model.Category;
import com.tiendaenlinea.reactiva.domain.model.Product;
import com.tiendaenlinea.reactiva.domain.port.CategoryRepositoryPort;
import com.tiendaenlinea.reactiva.domain.port.ProductImageStoragePort;
import com.tiendaenlinea.reactiva.domain.port.ProductRepositoryPort;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Validated
public class ProductApplicationService {

	private static final Logger log = LoggerFactory.getLogger(ProductApplicationService.class);

	private final ProductRepositoryPort productRepository;
	private final CategoryRepositoryPort categoryRepository;
	private final ProductImageStoragePort imageStorage;
	private final String publicFilesBasePath;

	public ProductApplicationService(
			ProductRepositoryPort productRepository,
			CategoryRepositoryPort categoryRepository,
			ProductImageStoragePort imageStorage,
			@Value("${tienda.public-files-base:/api/files}") String publicFilesBasePath) {
		this.productRepository = productRepository;
		this.categoryRepository = categoryRepository;
		this.imageStorage = imageStorage;
		this.publicFilesBasePath = publicFilesBasePath.endsWith("/")
				? publicFilesBasePath.substring(0, publicFilesBasePath.length() - 1)
				: publicFilesBasePath;
	}

	public Mono<ProductResponse> crear(
			@Valid CreateProductCommand cmd,
			Mono<byte[]> contenidoImagen,
			String nombreArchivoOriginal) {
		return validateCategory(cmd.categoryId())
				.then(Mono.defer(() -> contenidoImagen
						.filter(b -> b != null && b.length > 0)
						.flatMap(bytes -> imageStorage.save(bytes, nombreSafe(nombreArchivoOriginal))
								.doOnNext(path -> log.debug("Imagen almacenada: {}", path)))
						.flatMap(path -> guardarNuevo(cmd, path))
						.switchIfEmpty(Mono.defer(() -> guardarNuevo(cmd, null)))
						.doOnSuccess(r -> log.debug("Producto creado id={}", r.id()))
						.doOnError(e -> log.warn("Error al crear producto: {}", e.toString()))
						.checkpoint("crear-producto", true)));
	}

	public Mono<ProductResponse> actualizar(
			UUID id,
			@Valid UpdateProductCommand cmd,
			Mono<byte[]> contenidoImagenNueva,
			String nombreArchivoOriginal) {
		return validateCategory(cmd.categoryId())
				.then(Mono.defer(() -> productRepository.findById(id)
						.switchIfEmpty(Mono.error(new NoSuchElementException("Producto no encontrado: " + id)))
						.flatMap(existente -> contenidoImagenNueva
								.filter(b -> b != null && b.length > 0)
								.flatMap(bytes -> reemplazarImagen(existente, bytes, nombreSafe(nombreArchivoOriginal)))
								.switchIfEmpty(Mono.just(existente))
								.map(p -> aplicarDatos(p, cmd))
								.flatMap(productRepository::save)
								.flatMap(this::toResponseWithCategory)
								.doOnNext(r -> log.debug("Producto actualizado id={}", r.id())))
						.checkpoint("actualizar-producto", true)));
	}

	public Flux<ProductResponse> listarTodos() {
		return productRepository.findAllOrderByName()
				.collectList()
				.flatMapMany(list -> {
					if (list.isEmpty()) {
						return Flux.empty();
					}
					var catIds = list.stream()
							.map(Product::getCategoryId)
							.filter(Objects::nonNull)
							.distinct()
							.toList();
					if (catIds.isEmpty()) {
						return Flux.fromIterable(list).map(p -> toResponse(p, null));
					}
					return categoryRepository.findAllById(catIds)
							.collectMap(Category::getId)
							.flatMapMany(map -> Flux.fromIterable(list).map(p -> {
								Category c = p.getCategoryId() != null ? map.get(p.getCategoryId()) : null;
								return toResponse(p, c != null ? c.getName() : null);
							}));
				})
				.name("catalogo-productos")
				.doOnSubscribe(s -> log.debug("Suscripción al listado de productos"))
				.doOnComplete(() -> log.trace("Listado completado"))
				.checkpoint("listar-productos", true);
	}

	public Mono<ProductResponse> obtenerPorId(UUID id) {
		return productRepository.findById(id)
				.switchIfEmpty(Mono.error(new NoSuchElementException("Producto no encontrado: " + id)))
				.flatMap(this::toResponseWithCategory)
				.doOnNext(r -> log.debug("Obtenido producto id={}", r.id()))
				.checkpoint("obtener-por-id", true);
	}

	public Mono<Void> eliminar(UUID id) {
		return productRepository.findById(id)
				.switchIfEmpty(Mono.error(new NoSuchElementException("Producto no encontrado: " + id)))
				.flatMap(p -> {
					Mono<Void> borrarImg = p.getImagePath() != null
							? imageStorage.deleteIfExists(p.getImagePath())
							: Mono.empty();
					return borrarImg.then(productRepository.deleteById(id));
				})
				.doOnSuccess(v -> log.debug("Producto eliminado id={}", id))
				.checkpoint("eliminar-producto", true);
	}

	private Mono<Void> validateCategory(UUID categoryId) {
		if (categoryId == null) {
			return Mono.empty();
		}
		return categoryRepository.findById(categoryId)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria no encontrada")))
				.then();
	}

	private Mono<ProductResponse> toResponseWithCategory(Product p) {
		if (p.getCategoryId() == null) {
			return Mono.just(toResponse(p, null));
		}
		return categoryRepository.findById(p.getCategoryId())
				.map(c -> toResponse(p, c.getName()))
				.switchIfEmpty(Mono.fromCallable(() -> toResponse(p, null)));
	}

	private Mono<Product> reemplazarImagen(Product existente, byte[] bytes, String filename) {
		Mono<Void> borrarAnterior = existente.getImagePath() != null
				? imageStorage.deleteIfExists(existente.getImagePath())
				: Mono.empty();
		return borrarAnterior.then(imageStorage.save(bytes, filename))
				.map(existente::withImagePath);
	}

	private static Product aplicarDatos(Product p, UpdateProductCommand cmd) {
		return new Product(
				p.getId(),
				cmd.name(),
				cmd.description(),
				cmd.price(),
				cmd.stock(),
				p.getImagePath(),
				cmd.categoryId());
	}

	private Mono<ProductResponse> guardarNuevo(CreateProductCommand cmd, String imagePath) {
		Product nuevo = Product.nuevo(cmd.name(), cmd.description(), cmd.price(), cmd.stock(), imagePath, cmd.categoryId());
		return productRepository.save(nuevo).flatMap(this::toResponseWithCategory);
	}

	private ProductResponse toResponse(Product p, String categoryName) {
		String url = null;
		if (p.getImagePath() != null && !p.getImagePath().isBlank()) {
			url = publicFilesBasePath + "/" + p.getImagePath();
		}
		return new ProductResponse(
				p.getId(),
				p.getName(),
				p.getDescription(),
				p.getPrice(),
				p.getStock(),
				url,
				p.getCategoryId(),
				categoryName);
	}

	private static String nombreSafe(String original) {
		return (original == null || original.isBlank()) ? "imagen.bin" : original;
	}
}
