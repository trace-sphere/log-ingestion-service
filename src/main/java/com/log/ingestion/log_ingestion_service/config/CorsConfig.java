package com.log.ingestion.log_ingestion_service.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.time.Instant;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsConfig implements Filter {

    @Value("${cors.allowed.http.headers}")
    private String allowedHeaders;

    @Value("${cors.allowed.http.methods}")
    private String allowedMethods;

    @Value("${cors.allowed.client.origins}")
    private String allowedOrigins;

    @Value("${cors.allowed.http.exposed.headers}")
    private String exposedHeaders;

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;

        response.setHeader("Access-Control-Allow-Origin", allowedOrigins);
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", allowedMethods);
        response.setHeader("Access-Control-Allow-Headers", allowedHeaders);
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Current-Fetched-Time-Stamp", Instant.now().toString());
        response.setHeader("Access-Control-Expose-Headers", exposedHeaders);
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            chain.doFilter(req, res);
        }
    }

    public void init(FilterConfig filterConfig) {
    }

}