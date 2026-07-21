package com.log.ingestion.log_ingestion_service.config;

import jakarta.servlet.FilterChain;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityConfig {

    public SecurityFilterChain requestFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity.authorizeHttpRequests((http)->http.requestMatchers("/*").permitAll().anyRequest().permitAll());

        httpSecurity.cors(AbstractHttpConfigurer::disable);
        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        httpSecurity.oauth2ResourceServer((auth) ->
                auth.jwt((jwt) -> jwt.jwtAuthenticationConverter(null))
        );
        return httpSecurity.build();
    }

}
