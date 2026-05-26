package com.sportsapi.sports_ecommerce.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Configura detalhadamente o CORS permitindo cabeçalhos personalizados de controle
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("X-API-Key", "X-Idempotency-Key", "X-API-Version", "Content-Type", "Authorization")
                .exposedHeaders("Retry-After", "X-RateLimit-Limit", "X-RateLimit-Remaining", "X-Cache-Lookup")
                .allowCredentials(true)
                .maxAge(3600);
    }
}