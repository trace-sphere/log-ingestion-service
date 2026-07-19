package com.log.ingestion.log_ingestion_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RequiredBeanConfig {

    @Bean
    RestTemplate getRestTemplateBean() {
        return new RestTemplate();
    }
}
