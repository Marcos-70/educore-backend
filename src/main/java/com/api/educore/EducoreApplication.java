package com.api.educore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EducoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(EducoreApplication.class, args);
	}

}
