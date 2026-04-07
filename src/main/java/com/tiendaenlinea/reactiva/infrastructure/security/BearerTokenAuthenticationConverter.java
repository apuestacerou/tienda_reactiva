package com.tiendaenlinea.reactiva.infrastructure.security;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class BearerTokenAuthenticationConverter implements ServerAuthenticationConverter {

	@Override
	public Mono<Authentication> convert(ServerWebExchange exchange) {
		String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		if (auth == null || !auth.regionMatches(true, 0, "Bearer ", 0, 7)) {
			return Mono.empty();
		}
		String token = auth.substring(7).trim();
		if (token.isEmpty()) {
			return Mono.empty();
		}
		return Mono.just(new UsernamePasswordAuthenticationToken(null, token));
	}
}
