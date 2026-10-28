package com.mental.health.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web 配置：CORS（放行前端 5173）+ /upload/** 静态资源映射
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${storage.upload-dir:./upload}")
    private String uploadDir;

    /** CORS：开发环境放行 http://localhost:5173，方法与请求头全放开（Spring Security http.cors() 复用该 Bean） */
    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        var config = new org.springframework.web.cors.CorsConfiguration();
        config.setAllowedOrigins(java.util.List.of("http://localhost:5173"));
        config.setAllowedMethods(java.util.List.of("*"));
        config.setAllowedHeaders(java.util.List.of("*"));
        config.setMaxAge(3600L);
        var source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /** /upload/** 映射到本地磁盘目录 */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        registry.addResourceHandler("/upload/**")
                .addResourceLocations(dir.toUri().toString());
    }
}
