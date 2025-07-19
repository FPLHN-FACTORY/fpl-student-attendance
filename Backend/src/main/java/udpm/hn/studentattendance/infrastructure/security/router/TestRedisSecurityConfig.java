package udpm.hn.studentattendance.infrastructure.security.router;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;
import udpm.hn.studentattendance.infrastructure.constants.RoutesConstant;

@Component
public class TestRedisSecurityConfig {

    public void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(RoutesConstant.API_PREFIX + "/cache/**").permitAll());
    }
}