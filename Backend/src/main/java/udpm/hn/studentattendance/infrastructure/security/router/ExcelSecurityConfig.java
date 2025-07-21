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
            auth.requestMatchers(appendWildcard(RouteExcelConstant.URL_API_STAFF)).hasAuthority(RoleConstant.ADMIN.name());
            auth.requestMatchers(appendWildcard(RouteExcelConstant.URL_API_FACTORY)).hasAuthority(RoleConstant.STAFF.name());
            auth.requestMatchers(appendWildcard(RouteExcelConstant.URL_API_STUDENT)).hasAuthority(RoleConstant.STAFF.name());
            auth.requestMatchers(appendWildcard(RouteExcelConstant.URL_API_STUDENT_FACTORY)).hasAuthority(RoleConstant.STAFF.name());
            auth.requestMatchers(appendWildcard(RouteExcelConstant.URL_API_PROJECT)).hasAuthority(RoleConstant.STAFF.name());
            auth.requestMatchers(appendWildcard(RouteExcelConstant.URL_API_ATTENDANCE_RECOVERY)).hasAuthority(RoleConstant.STAFF.name());
            auth.requestMatchers(appendWildcard(RouteExcelConstant.URL_API_SCHEDULED_STUDENT)).hasAuthority(RoleConstant.STUDENT.name());
        });
    }

}
