package udpm.hn.studentattendance.infrastructure.security.router;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;
import udpm.hn.studentattendance.infrastructure.constants.RoutesConstant;

import static udpm.hn.studentattendance.helpers.RouterHelper.appendWildcard;

@Component
public class StaffSecurityConfig {

    public void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers(appendWildcard(RoutesConstant.PREFIX_API_STAFF_MANAGEMENT)).hasAuthority(RoleConstant.STAFF.name());
        });
    }

}
