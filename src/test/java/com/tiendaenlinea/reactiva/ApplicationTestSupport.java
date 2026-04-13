package com.tiendaenlinea.reactiva;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Condiciones compartidas para tests que necesitan {@code application-local.yml}.
 */
public final class ApplicationTestSupport {

	private ApplicationTestSupport() {
	}

	public static boolean applicationLocalYmlExists() {
		return Files.exists(Path.of("application-local.yml"));
	}
}
