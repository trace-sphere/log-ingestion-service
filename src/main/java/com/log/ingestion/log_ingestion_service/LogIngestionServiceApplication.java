package com.log.ingestion.log_ingestion_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableCaching
@EnableFeignClients
@EnableAsync
@SpringBootApplication
public class LogIngestionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LogIngestionServiceApplication.class, args);
	}

}
