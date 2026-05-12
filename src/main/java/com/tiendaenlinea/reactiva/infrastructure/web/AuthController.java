package com.tiendaenlinea.reactiva.infrastructure.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tiendaenlinea.reactiva.application.dto.auth.AuthResponse;
import com.tiendaenlinea.reactiva.application.dto.auth.LoginRequest;
import com.tiendaenlinea.reactiva.application.dto.auth.LogoutRequest;
import com.tiendaenlinea.reactiva.application.dto.auth.RefreshTokenRequest;
import com.tiendaenlinea.reactiva.application.dto.auth.RegisterRequest;
import com.tiendaenlinea.reactiva.application.service.AuthService;

import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
	public Mono<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
		return authService.register(request);
	}

	@PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
	public Mono<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@PostMapping(value = "/refresh", consumes = MediaType.APPLICATION_JSON_VALUE)
	public Mono<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
		return authService.refresh(request);
	}

	@PostMapping(value = "/logout", consumes = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<Void>> logout(@Valid @RequestBody LogoutRequest request) {
		return authService.logout(request).thenReturn(ResponseEntity.status(HttpStatus.NO_CONTENT).build());
	}
}
