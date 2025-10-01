package com.example.hanoiGo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.example.hanoiGo.model")
@EnableJpaRepositories("com.example.hanoiGo.repository")
public class HanoiGoApplication {

	public static void main(String[] args) {
		SpringApplication.run(HanoiGoApplication.class, args);
	}

}
