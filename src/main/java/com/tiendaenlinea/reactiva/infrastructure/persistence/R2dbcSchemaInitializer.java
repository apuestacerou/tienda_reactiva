package com.tiendaenlinea.reactiva.infrastructure.persistence;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

import io.r2dbc.spi.ConnectionFactory;

/**
 * {@code spring.sql.init} solo aplica scripts a JDBC; con solo R2DBC el {@code schema.sql}
 * no se ejecutaba y faltaban tablas (p. ej. {@code users}), provocando 500 al registrar.
 */
@Configuration(proxyBeanMethods = false)
public class R2dbcSchemaInitializer {

	@Bean
	public ConnectionFactoryInitializer r2dbcInitializer(ConnectionFactory connectionFactory) {
		ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
		initializer.setConnectionFactory(connectionFactory);
		ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
		populator.addScript(new ClassPathResource("schema.sql"));
		initializer.setDatabasePopulator(populator);
		return initializer;
	}
}
