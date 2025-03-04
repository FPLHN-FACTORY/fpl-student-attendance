package udpm.hn.studentattendance.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import udpm.hn.studentattendance.core.authentication.router.AuthenticationSecurityConfig;
import udpm.hn.studentattendance.infrastructure.security.exception.CustomAccessDeniedHandler;
import udpm.hn.studentattendance.infrastructure.security.exception.CustomAuthenticationEntryPoint;

import java.util.Collections;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(
    securedEnabled = true,
    jsr250Enabled = true
)
public class SecurityConfig {

    private final AuthenticationSecurityConfig authenticationSecurityConfig;

    @Value("${allowed.origin}")
    public String ALLOWED_ORIGIN;

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        String[] origins = ALLOWED_ORIGIN.split(",");
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        source.registerCorsConfiguration("/**", config.applyPermitDefaultValues());
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setAllowedOrigins(List.of(origins));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PUT", "OPTIONS", "PATCH", "DELETE"));
        return source;
    }

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(c -> c.configurationSource(corsConfigurationSource()));
        http.csrf(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.ALWAYS));
        http.exceptionHandling(e -> {
            e.accessDeniedHandler(new CustomAccessDeniedHandler());
            e.authenticationEntryPoint(new CustomAuthenticationEntryPoint());
        });

        // Thêm từng config routes vào đây
        authenticationSecurityConfig.configure(http);

        http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
        return http.build();
    }

}
