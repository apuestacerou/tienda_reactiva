package com.tiendaenlinea.reactiva.infrastructure.security;

import java.util.List;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

	private final JwtService jwtService;

	public JwtReactiveAuthenticationManager(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	public Mono<Authentication> authenticate(Authentication authentication) {
		String token = String.valueOf(authentication.getCredentials());
		return Mono.fromCallable(() -> jwtService.parseValid(token))
				.map(claims -> new UsernamePasswordAuthenticationToken(
						claims.getSubject(),
						token,
						List.of(new SimpleGrantedAuthority("ROLE_USER"))));
	}
}
