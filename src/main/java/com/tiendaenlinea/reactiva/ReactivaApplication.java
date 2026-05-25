package com.tiendaenlinea.reactiva;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ReactivaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactivaApplication.class, args);
	}

}
