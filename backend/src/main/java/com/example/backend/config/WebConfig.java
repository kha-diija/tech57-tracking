package com.example.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + uploadDir.replace("./", "") + "/";
        // Normalise en chemin absolu si besoin
        java.io.File dir = new java.io.File(uploadDir);
        location = "file:" + dir.getAbsolutePath() + "/";

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}