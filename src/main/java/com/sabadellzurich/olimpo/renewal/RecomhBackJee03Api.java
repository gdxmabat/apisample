package com.sabadellzurich.olimpo.renewal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients
public class RecomhBackJee03Api {

	public static void main(String[] args) {
		SpringApplication.run(RecomhBackJee03Api.class, args);
	}

}
