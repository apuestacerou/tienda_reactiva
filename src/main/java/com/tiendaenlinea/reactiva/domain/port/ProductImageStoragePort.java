package com.tiendaenlinea.reactiva.domain.port;

import reactor.core.publisher.Mono;

public interface ProductImageStoragePort {

	Mono<String> save(byte[] content, String originalFilename);

	Mono<Void> deleteIfExists(String storedPath);
}
