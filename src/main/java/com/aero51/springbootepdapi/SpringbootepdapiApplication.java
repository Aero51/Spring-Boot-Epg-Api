package com.aero51.springbootepdapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SpringbootepdapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootepdapiApplication.class, args);
		System.out.println("SpringbootepdapiApplication");
	}

}
