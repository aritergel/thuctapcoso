package com.shoeshop.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Cấu hình Web: CORS, Static Resources, Upload Directory
 * Tương đương với middleware CORS và express.static() trong server.js
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Thư mục lưu ảnh upload (tương đương public/uploads/ trong Node.js)
    public static final String UPLOAD_DIR = "uploads/";

    @PostConstruct
    public void init() {
        // Tạo thư mục uploads nếu chưa có
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Tương đương cors({ credentials: true, origin: true }) trong Node.js
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded files từ thư mục ngoài (tương đương express.static)
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + UPLOAD_DIR);
    }
}
