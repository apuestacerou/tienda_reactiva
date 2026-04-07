package com.tiendaenlinea.reactiva.infrastructure.web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tiendaenlinea.reactiva.application.dto.auth.AuthResponse;
import com.tiendaenlinea.reactiva.application.dto.auth.LoginRequest;
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
}
