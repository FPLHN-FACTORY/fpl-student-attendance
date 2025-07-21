package udpm.hn.studentattendance.infrastructure.config;

import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestDatabaseConfig {

    @Bean
    @Primary
    public PhysicalNamingStrategy physicalNamingStrategy() {
        return new H2NamingStrategy();
    }
}
