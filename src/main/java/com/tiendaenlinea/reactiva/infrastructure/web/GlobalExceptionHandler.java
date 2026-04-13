package com.tiendaenlinea.reactiva.infrastructure.web;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.r2dbc.BadSqlGrammarException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import io.r2dbc.spi.R2dbcException;

import reactor.core.publisher.Mono;

@ControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(AccessDeniedException.class)
	public Mono<ResponseEntity<ErrorResponse>> accessDenied(AccessDeniedException ex) {
		return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(new ErrorResponse("Se requiere rol de administrador")));
	}

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

	@ExceptionHandler(DuplicateKeyException.class)
	public Mono<ResponseEntity<ErrorResponse>> duplicateKey(DuplicateKeyException ex) {
		return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Email ya registrado")));
	}

	@ExceptionHandler(BadSqlGrammarException.class)
	public Mono<ResponseEntity<ErrorResponse>> badSql(BadSqlGrammarException ex) {
		log.error("Error SQL (revisa tablas en Neon o scripts en /scripts)", ex);
		String hint = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
		return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ErrorResponse("Base de datos no disponible o esquema incompleto: " + hint)));
	}

	@ExceptionHandler(R2dbcException.class)
	public Mono<ResponseEntity<ErrorResponse>> r2dbc(R2dbcException ex) {
		log.error("Error R2DBC", ex);
		return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ErrorResponse(ex.getMessage() != null ? ex.getMessage() : "Error de base de datos")));
	}
}
