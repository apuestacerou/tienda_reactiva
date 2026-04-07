package com.tiendaenlinea.reactiva.infrastructure.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tiendaenlinea.reactiva.domain.port.ProductImageStoragePort;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class LocalProductImageStorageAdapter implements ProductImageStoragePort {

	private final Path directorioBase;

	public LocalProductImageStorageAdapter(@Value("${tienda.uploads.dir}") String dir) throws IOException {
		this.directorioBase = Path.of(dir).toAbsolutePath().normalize();
		Files.createDirectories(this.directorioBase);
	}

	@Override
	public Mono<String> save(byte[] content, String originalFilename) {
		return Mono.fromCallable(() -> {
			String ext = extensionSegura(originalFilename);
			String nombre = UUID.randomUUID() + ext;
			Path destino = directorioBase.resolve(nombre);
			Files.write(destino, content);
			return nombre;
		}).subscribeOn(Schedulers.boundedElastic());
	}

	@Override
	public Mono<Void> deleteIfExists(String storedPath) {
		if (storedPath == null || storedPath.isBlank()) {
			return Mono.empty();
		}
		return Mono.fromRunnable(() -> {
			Path p = directorioBase.resolve(storedPath).normalize();
			if (!p.startsWith(directorioBase)) {
				throw new SecurityException("Ruta invalida");
			}
			try {
				Files.deleteIfExists(p);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}).subscribeOn(Schedulers.boundedElastic()).then();
	}

	private static String extensionSegura(String originalFilename) {
		if (originalFilename == null || !originalFilename.contains(".")) {
			return ".bin";
		}
		String ext = originalFilename.substring(originalFilename.lastIndexOf('.'));
		if (ext.length() > 8 || ext.chars().anyMatch(c -> !Character.isLetterOrDigit(c) && c != '.')) {
			return ".bin";
		}
		return ext.toLowerCase();
	}
}
