package com.tiendaenlinea.reactiva.application.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.tiendaenlinea.reactiva.application.dto.auth.AuthResponse;
import com.tiendaenlinea.reactiva.application.dto.auth.LoginRequest;
import com.tiendaenlinea.reactiva.application.dto.auth.LogoutRequest;
import com.tiendaenlinea.reactiva.application.dto.auth.RefreshTokenRequest;
import com.tiendaenlinea.reactiva.application.dto.auth.RegisterRequest;
import com.tiendaenlinea.reactiva.domain.model.UserRole;
import com.tiendaenlinea.reactiva.infrastructure.persistence.UserEntity;
import com.tiendaenlinea.reactiva.infrastructure.persistence.UserR2dbcRepository;
import com.tiendaenlinea.reactiva.infrastructure.redis.RedisRefreshTokenService;
import com.tiendaenlinea.reactiva.infrastructure.security.JwtService;

import reactor.core.publisher.Mono;

@Service
public class AuthService {

	private final UserR2dbcRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final RedisRefreshTokenService refreshTokenService;

	public AuthService(
			UserR2dbcRepository userRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService,
			RedisRefreshTokenService refreshTokenService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.refreshTokenService = refreshTokenService;
	}

	public Mono<AuthResponse> register(RegisterRequest req) {
		String email = req.email().trim().toLowerCase();
		return userRepository.findByEmail(email)
				.hasElement()
				.flatMap(exists -> {
					if (Boolean.TRUE.equals(exists)) {
						return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "Email ya registrado"));
					}
					Instant now = Instant.now();
					UserEntity e = new UserEntity();
					e.setId(UUID.randomUUID());
					e.setEmail(email);
					e.setPasswordHash(passwordEncoder.encode(req.password()));
					e.setFullName(req.fullName() != null ? req.fullName() : "");
					e.setRole(UserRole.defaultRegistrationRole());
					e.setCreatedAt(now);
					e.setUpdatedAt(now);
					e.markNewRow();
					return userRepository.save(e).flatMap(this::buildAuthResponse);
				});
	}

	public Mono<AuthResponse> login(LoginRequest req) {
		String email = req.email().trim().toLowerCase();
		return userRepository.findByEmail(email)
				.filter(u -> passwordEncoder.matches(req.password(), u.getPasswordHash()))
				.flatMap(this::buildAuthResponse)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas")));
	}

	public Mono<AuthResponse> refresh(RefreshTokenRequest req) {
		return refreshTokenService.rotate(req.refreshToken())
				.map(rot -> new AuthResponse(
						jwtService.createToken(rot.userId(), rot.email(), rot.role()),
						rot.newRefreshToken(),
						rot.userId(),
						rot.email(),
						rot.role()));
	}

	public Mono<Void> logout(LogoutRequest req) {
		return refreshTokenService.revoke(req.refreshToken());
	}

	private Mono<AuthResponse> buildAuthResponse(UserEntity u) {
		String access = jwtService.createToken(u.getId(), u.getEmail(), u.getRole());
		return refreshTokenService.issueForUser(u.getId(), u.getEmail(), u.getRole())
				.map(refresh -> new AuthResponse(access, refresh, u.getId(), u.getEmail(), u.getRole()));
	}
}
