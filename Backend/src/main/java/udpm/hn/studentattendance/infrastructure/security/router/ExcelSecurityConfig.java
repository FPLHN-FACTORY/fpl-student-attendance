package udpm.hn.studentattendance.infrastructure.security.router;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;
import udpm.hn.studentattendance.infrastructure.constants.RoutesConstant;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteExcelConstant;

import static udpm.hn.studentattendance.helpers.RouterHelper.appendWildcard;

@Component
public class ExcelSecurityConfig {

    public void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers(appendWildcard(RouteExcelConstant.URL_API_PLAN_DATE)).hasAuthority(RoleConstant.STAFF.name());
        });
    }

}
