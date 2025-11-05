package com.example.hanoiGo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan("com.example.hanoiGo.model")
@EnableJpaRepositories("com.example.hanoiGo.repository")
@EnableCaching
@EnableScheduling
public class HanoiGoApplication {

	public static void main(String[] args) {
		SpringApplication.run(HanoiGoApplication.class, args);
	}

}
