package com.coreplm;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableJpaAuditing
@SpringBootApplication
public class CoreplmApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoreplmApplication.class, args);
	}

}
