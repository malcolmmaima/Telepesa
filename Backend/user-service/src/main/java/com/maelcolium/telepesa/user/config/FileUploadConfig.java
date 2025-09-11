package com.maelcolium.telepesa.user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for serving uploaded files
 */
@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded files from the uploads directory
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:" + uploadDir + "/")
            .setCachePeriod(3600); // Cache for 1 hour
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Allow CORS for static file access from frontend
        registry.addMapping("/uploads/**")
            .allowedOrigins("http://localhost:3000", "http://localhost:5173", "http://localhost:5174") // React dev servers
            .allowedMethods("GET", "HEAD")
            .allowedHeaders("*")
            .allowCredentials(false)
            .maxAge(3600);
    }
}
