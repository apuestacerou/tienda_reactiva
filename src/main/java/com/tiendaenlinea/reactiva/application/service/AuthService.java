package com.tiendaenlinea.reactiva.application.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.tiendaenlinea.reactiva.application.dto.auth.AuthResponse;
import com.tiendaenlinea.reactiva.application.dto.auth.LoginRequest;
import com.tiendaenlinea.reactiva.application.dto.auth.RegisterRequest;
import com.tiendaenlinea.reactiva.infrastructure.persistence.UserEntity;
import com.tiendaenlinea.reactiva.infrastructure.persistence.UserR2dbcRepository;
import com.tiendaenlinea.reactiva.infrastructure.security.JwtService;

import reactor.core.publisher.Mono;

@Service
public class AuthService {

	private final UserR2dbcRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthService(UserR2dbcRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	public Mono<AuthResponse> register(RegisterRequest req) {
		String email = req.email().trim().toLowerCase();
		return userRepository.findByEmail(email)
				.flatMap(u -> Mono.<AuthResponse>error(
						new ResponseStatusException(HttpStatus.CONFLICT, "Email ya registrado")))
				.switchIfEmpty(Mono.defer(() -> {
					UserEntity e = new UserEntity();
					e.setId(UUID.randomUUID());
					e.setEmail(email);
					e.setPasswordHash(passwordEncoder.encode(req.password()));
					e.setFullName(req.fullName() != null ? req.fullName() : "");
					e.setRole("CUSTOMER");
					return userRepository.save(e)
							.map(u -> new AuthResponse(
									jwtService.createToken(u.getId(), u.getEmail()),
									u.getId(),
									u.getEmail()));
				}));
	}

	public Mono<AuthResponse> login(LoginRequest req) {
		String email = req.email().trim().toLowerCase();
		return userRepository.findByEmail(email)
				.filter(u -> passwordEncoder.matches(req.password(), u.getPasswordHash()))
				.map(u -> new AuthResponse(jwtService.createToken(u.getId(), u.getEmail()), u.getId(), u.getEmail()))
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas")));
	}
}
