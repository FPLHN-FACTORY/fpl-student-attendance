package udpm.hn.studentattendance.infrastructure.config.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${allowed.origin}")
    public String ALLOWED_ORIGIN;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = ALLOWED_ORIGIN.split(",");
        registry.addMapping("/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "X-Cache-Status")
                .allowCredentials(true);
    }
} 