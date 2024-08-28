package com.example.oliveyoung;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
//@EnableFeignClients(basePackages = "com.example.oliveyoung.client")
public class OliveyoungApplication {

	public static void main(String[] args) {
		SpringApplication.run(OliveyoungApplication.class, args);
	}

}
