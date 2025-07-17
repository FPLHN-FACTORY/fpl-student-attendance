package udpm.hn.studentattendance.infrastructure.config.application;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import static org.junit.jupiter.api.Assertions.*;

class WebConfigTest {
    @Test
    void testWebConfigExists() {
        WebConfig config = new WebConfig();
        assertNotNull(config);
    }

    @Test
    void testCorsFilter() {
        WebConfig config = new WebConfig();
        org.springframework.web.filter.CorsFilter corsFilter = config.corsFilter();
        assertNotNull(corsFilter);
    }

    @Test
    void testAddCorsMappings() {
        WebConfig config = new WebConfig();
        org.springframework.web.servlet.config.annotation.CorsRegistry registry = new org.springframework.web.servlet.config.annotation.CorsRegistry();
        config.addCorsMappings(registry);
        // Test that the method executes without exception
        assertNotNull(config);
    }
}