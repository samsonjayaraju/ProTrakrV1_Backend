package com.klup.protrackr.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;
    private final Path uploadDir;
    private final String publicBaseUrl;

    public WebConfig(
            @Value("${app.cors.allowed-origins}") String allowedOrigins,
            @Value("${app.upload.dir}") String uploadDir,
            @Value("${app.upload.public-base-url}") String publicBaseUrl
    ) {
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toArray(String[]::new);
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.publicBaseUrl = publicBaseUrl.startsWith("/") ? publicBaseUrl : "/" + publicBaseUrl;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String handler = publicBaseUrl.endsWith("/") ? publicBaseUrl + "**" : publicBaseUrl + "/**";
        registry.addResourceHandler(handler)
                .addResourceLocations(uploadDir.toUri().toString());
    }
}

