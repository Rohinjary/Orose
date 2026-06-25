package com.example.orose;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OroseApplication {

	public static void main(String[] args) {
		SpringApplication.run(OroseApplication.class, args);
	}
}