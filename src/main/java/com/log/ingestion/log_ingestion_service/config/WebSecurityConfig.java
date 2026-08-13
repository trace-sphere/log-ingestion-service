package com.log.ingestion.log_ingestion_service.config;

import jakarta.servlet.FilterChain;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain requestFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity.authorizeHttpRequests((http)->http
                .requestMatchers("/logTrace/test/saveAll").permitAll()
                .requestMatchers("/logTrace/test/request").permitAll()
                .requestMatchers("/logTrace/getBySearch").permitAll()
                .requestMatchers("/dashboard/getAll").permitAll()
                .anyRequest().authenticated());

        httpSecurity.cors(AbstractHttpConfigurer::disable);
        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        httpSecurity.oauth2ResourceServer((auth) ->
                auth.jwt((jwt) -> jwt.jwtAuthenticationConverter(new JwtAuthenticationConverter()))
        );
        return httpSecurity.build();
    }

}
