package udpm.hn.studentattendance.core.test.router;

import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;
import udpm.hn.studentattendance.core.authentication.router.RouteAuthentication;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;

import static udpm.hn.studentattendance.helpers.RouterHelper.appendPrefixApi;

@Component
public class TestSecurityConfig {
    public void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(auth ->{
            auth.requestMatchers(appendPrefixApi("/api/v1/admin")).permitAll();
        });
    }
}
