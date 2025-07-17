package udpm.hn.studentattendance.infrastructure.config.application;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JacksonConfigTest {
    @Test
    void testJacksonConfigExists() {
        JacksonConfig config = new JacksonConfig();
        assertNotNull(config);
    }

    @Test
    void testObjectMapperBean() throws Exception {
        JacksonConfig config = new JacksonConfig();
        // Test that the method exists
        assertNotNull(config.getClass().getMethod("objectMapper",
                org.springframework.http.converter.json.Jackson2ObjectMapperBuilder.class));
    }

    @Test
    void testMappingJackson2HttpMessageConverterBean() throws Exception {
        JacksonConfig config = new JacksonConfig();
        // Test that the method exists
        assertNotNull(config.getClass().getMethod("mappingJackson2HttpMessageConverter",
                com.fasterxml.jackson.databind.ObjectMapper.class));
    }
}