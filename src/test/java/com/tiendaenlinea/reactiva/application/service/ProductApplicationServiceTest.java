package com.tiendaenlinea.reactiva.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tiendaenlinea.reactiva.application.dto.CreateProductCommand;
import com.tiendaenlinea.reactiva.application.dto.ProductResponse;
import com.tiendaenlinea.reactiva.application.dto.UpdateProductCommand;
import com.tiendaenlinea.reactiva.domain.model.Category;
import com.tiendaenlinea.reactiva.domain.model.Product;
import com.tiendaenlinea.reactiva.domain.port.CategoryRepositoryPort;
import com.tiendaenlinea.reactiva.domain.port.ProductImageStoragePort;
import com.tiendaenlinea.reactiva.domain.port.ProductRepositoryPort;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ProductApplicationServiceTest {

	private static final String FILES_BASE = "/api/files";

	@Mock
	private ProductRepositoryPort productRepository;

	@Mock
	private CategoryRepositoryPort categoryRepository;

	@Mock
	private ProductImageStoragePort imageStorage;

	private ProductApplicationService service;

	private final UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private final UUID catId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

	private Product producto;

	@BeforeEach
	void setUp() {
		service = new ProductApplicationService(productRepository, categoryRepository, imageStorage, FILES_BASE);
		producto = new Product(id, "Teclado", "Mecánico", new BigDecimal("99.99"), 5, "f1.jpg", null);
	}

	@Test
	@DisplayName("listarTodos: sin categoría — categoryName null")
	void listarTodos_sinCategoria() {
		when(productRepository.findAllOrderByName()).thenReturn(Flux.just(producto));

		StepVerifier.create(service.listarTodos())
				.assertNext(r -> {
					assertThat(r.id()).isEqualTo(id);
					assertThat(r.name()).isEqualTo("Teclado");
					assertThat(r.imageUrl()).isEqualTo(FILES_BASE + "/f1.jpg");
					assertThat(r.categoryId()).isNull();
					assertThat(r.categoryName()).isNull();
				})
				.verifyComplete();

		verify(categoryRepository, never()).findAllById(any());
	}

	@Test
	@DisplayName("listarTodos: con categoría — resuelve nombre")
	void listarTodos_conCategoria() {
		var conCat = new Product(id, "Teclado", "Mecánico", new BigDecimal("99.99"), 5, "f1.jpg", catId);
		when(productRepository.findAllOrderByName()).thenReturn(Flux.just(conCat));
		when(categoryRepository.findAllById(any())).thenReturn(Flux.just(new Category(catId, "Electrónica", "electronica")));

		StepVerifier.create(service.listarTodos())
				.assertNext(r -> {
					assertThat(r.categoryId()).isEqualTo(catId);
					assertThat(r.categoryName()).isEqualTo("Electrónica");
				})
				.verifyComplete();
	}

	@Test
	@DisplayName("obtenerPorId: Mono empty → error NoSuchElementException")
	void obtenerPorId_noExiste() {
		when(productRepository.findById(id)).thenReturn(Mono.empty());

		StepVerifier.create(service.obtenerPorId(id))
				.expectError(NoSuchElementException.class)
				.verify();
	}

	@Test
	@DisplayName("obtenerPorId: map a ProductResponse")
	void obtenerPorId_ok() {
		when(productRepository.findById(id)).thenReturn(Mono.just(producto));

		StepVerifier.create(service.obtenerPorId(id))
				.assertNext(r -> assertThat(r.price()).isEqualByComparingTo("99.99"))
				.verifyComplete();
	}

	@Test
	@DisplayName("crear sin imagen: switchIfEmpty + defer — no llama a imageStorage.save")
	void crear_sinImagen() {
		var cmd = new CreateProductCommand("A", "d", new BigDecimal("10"), 1, null);
		when(productRepository.save(any(Product.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

		StepVerifier.create(service.crear(cmd, Mono.empty(), ""))
				.assertNext((ProductResponse r) -> assertThat(r.name()).isEqualTo("A"))
				.verifyComplete();

		verify(imageStorage, never()).save(any(), any());
	}

	@Test
	@DisplayName("crear con imagen: flatMap encadena save de archivo y persistencia")
	void crear_conImagen() {
		var cmd = new CreateProductCommand("B", "d", new BigDecimal("20"), 2, null);
		when(imageStorage.save(any(), eq("foto.png"))).thenReturn(Mono.just("uuid.png"));
		when(productRepository.save(any(Product.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

		StepVerifier.create(service.crear(cmd, Mono.just(new byte[] { 1, 2, 3 }), "foto.png"))
				.assertNext(r -> assertThat(r.imageUrl()).contains("uuid.png"))
				.verifyComplete();

		verify(imageStorage).save(any(), eq("foto.png"));
	}

	@Test
	@DisplayName("eliminar: flatMap then — borra imagen y repositorio")
	void eliminar_ok() {
		when(productRepository.findById(id)).thenReturn(Mono.just(producto));
		when(imageStorage.deleteIfExists("f1.jpg")).thenReturn(Mono.empty());
		when(productRepository.deleteById(id)).thenReturn(Mono.empty());

		StepVerifier.create(service.eliminar(id))
				.verifyComplete();

		verify(productRepository).deleteById(id);
	}

	@Test
	@DisplayName("actualizar: sin imagen nueva mantiene path y aplica comando")
	void actualizar_sinNuevaImagen() {
		var cmd = new UpdateProductCommand("Nuevo", "desc", new BigDecimal("50"), 1, null);
		when(productRepository.findById(id)).thenReturn(Mono.just(producto));
		when(productRepository.save(any(Product.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

		StepVerifier.create(service.actualizar(id, cmd, Mono.empty(), ""))
				.assertNext(r -> assertThat(r.name()).isEqualTo("Nuevo"))
				.verifyComplete();
	}
}
