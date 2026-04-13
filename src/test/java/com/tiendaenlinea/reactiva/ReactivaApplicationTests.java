package com.tiendaenlinea.reactiva;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Carga el contexto completo contra Neon. Se omite si no existe {@code application-local.yml}
 * en la raíz del módulo (útil en CI sin credenciales).
 */
@SpringBootTest
@EnabledIf("com.tiendaenlinea.reactiva.ApplicationTestSupport#applicationLocalYmlExists")
class ReactivaApplicationTests {

	@Test
	void contextLoads() {
	}

}
