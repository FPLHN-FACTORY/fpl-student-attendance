package udpm.hn.studentattendance.infrastructure.config.dbgenerator;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class MockDBGenerator {

    @Bean
    @Primary
    public DBGenerator dbGenerator() {
        return new NoOpDBGenerator();
    }


    private static class NoOpDBGenerator extends DBGenerator {
        public NoOpDBGenerator() {
            super(null, null, null, null, null, null);
        }

        @Override
        public void init() {
        }
    }
}
