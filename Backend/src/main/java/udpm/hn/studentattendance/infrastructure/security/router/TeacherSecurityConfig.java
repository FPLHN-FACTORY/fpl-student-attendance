package udpm.hn.studentattendance.infrastructure.security.router;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;
import udpm.hn.studentattendance.infrastructure.constants.RoutesConstant;

import static udpm.hn.studentattendance.helpers.RouterHelper.appendWildcard;

@Component
public class TeacherSecurityConfig {

    public void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers(appendWildcard(RoutesConstant.PREFIX_API_TEACHER_MANAGEMENT)).hasAuthority(RoleConstant.TEACHER.name());
        });
    }

}
