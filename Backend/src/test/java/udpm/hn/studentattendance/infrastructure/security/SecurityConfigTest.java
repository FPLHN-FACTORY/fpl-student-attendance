package udpm.hn.studentattendance.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import udpm.hn.studentattendance.infrastructure.security.router.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {
    @Mock
    private AuthenticationSecurityConfig authenticationSecurityConfig;

    @Mock
    private StaffSecurityConfig staffSecurityConfig;

    @Mock
    private AdminSecurityConfig adminSecurityConfig;

    @Mock
    private StudentSecurityConfig studentSecurityConfig;

    @Mock
    private TeacherSecurityConfig teacherSecurityConfig;

    @Mock
    private ExcelSecurityConfig excelSecurityConfig;

    @Mock
    private TestRedisSecurityConfig testSecurityConfig;

    @Test
    void testSecurityConfigConstructor() {
        SecurityConfig config = new SecurityConfig(
                authenticationSecurityConfig,
                staffSecurityConfig,
                adminSecurityConfig,
                studentSecurityConfig,
                teacherSecurityConfig,
                excelSecurityConfig,
                testSecurityConfig);
        assertNotNull(config);
    }

    @Test
    void testFilterChainMethodExists() throws Exception {
        SecurityConfig config = new SecurityConfig(
                authenticationSecurityConfig,
                staffSecurityConfig,
                adminSecurityConfig,
                studentSecurityConfig,
                teacherSecurityConfig,
                excelSecurityConfig,
                testSecurityConfig);
        assertNotNull(config.getClass().getMethod("filterChain", HttpSecurity.class));
    }

    @Test
    void testCorsConfigurationSourceMethodExists() throws Exception {
        SecurityConfig config = new SecurityConfig(
                authenticationSecurityConfig,
                staffSecurityConfig,
                adminSecurityConfig,
                studentSecurityConfig,
                teacherSecurityConfig,
                excelSecurityConfig,
                testSecurityConfig);
        assertNotNull(config.getClass().getMethod("corsConfigurationSource"));
    }
}