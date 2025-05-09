package com.fplaisant.hrapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.fplaisant")
public class HrapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(HrapiApplication.class, args);
	}

}
