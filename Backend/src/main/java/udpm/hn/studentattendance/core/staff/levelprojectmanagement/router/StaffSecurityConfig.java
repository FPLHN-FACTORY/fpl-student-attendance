package udpm.hn.studentattendance.core.staff.levelprojectmanagement.router;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;

import static udpm.hn.studentattendance.helpers.RouterHelper.appendPrefixApi;

@Component
public class StaffSecurityConfig {
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers(appendPrefixApi("/api/v1/staff-management")).permitAll();
                }
        );
    }
}
