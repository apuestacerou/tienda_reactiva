package com.tiendaenlinea.reactiva.infrastructure.web;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResponseStatusException.class)
	public Mono<ResponseEntity<ErrorResponse>> responseStatus(ResponseStatusException ex) {
		String reason = ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString();
		return Mono.just(ResponseEntity.status(ex.getStatusCode()).body(new ErrorResponse(reason)));
	}

	@ExceptionHandler(NoSuchElementException.class)
	public Mono<ResponseEntity<ErrorResponse>> notFound(NoSuchElementException ex) {
		return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage())));
	}

	@ExceptionHandler(WebExchangeBindException.class)
	public Mono<ResponseEntity<ErrorResponse>> validation(WebExchangeBindException ex) {
		String msg = ex.getFieldErrors().stream()
				.map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
				.collect(Collectors.joining("; "));
		return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(msg)));
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public Mono<ResponseEntity<ErrorResponse>> staticNotFound(NoResourceFoundException ex, ServerWebExchange exchange) {
		return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Recurso no encontrado")));
	}
}
