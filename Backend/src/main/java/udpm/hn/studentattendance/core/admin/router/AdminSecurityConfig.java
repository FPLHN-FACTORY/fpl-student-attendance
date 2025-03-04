package udpm.hn.studentattendance.core.admin.router;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;

import static udpm.hn.studentattendance.helpers.RouterHelper.appendPrefixApi;

@Component
public class AdminSecurityConfig {
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers(appendPrefixApi("/api/v1/admin-management")).permitAll();
                }
        );
    }
}
