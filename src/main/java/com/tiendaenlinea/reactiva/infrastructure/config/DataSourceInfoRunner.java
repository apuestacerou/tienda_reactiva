package com.tiendaenlinea.reactiva.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Confirma en consola que la app está configurada contra PostgreSQL (Neon).
 */
@Component
public class DataSourceInfoRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(DataSourceInfoRunner.class);

	private final Environment env;

	public DataSourceInfoRunner(Environment env) {
		this.env = env;
	}

	@Override
	public void run(ApplicationArguments args) {
		String url = env.getProperty("spring.r2dbc.url", "");
		if (url.contains("postgresql")) {
			log.info(">>> R2DBC: PostgreSQL (Neon). URL (inicio): {}…",
					url.length() > 72 ? url.substring(0, 72) : url);
		} else {
			log.warn(
					">>> spring.r2dbc.url no esta definida o no es PostgreSQL. Crea application-local.yml junto al pom.xml (copia application-local.yml.example) con tu Neon.");
		}
	}
}
