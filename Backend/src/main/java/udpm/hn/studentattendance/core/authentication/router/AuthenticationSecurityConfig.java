package udpm.hn.studentattendance.core.authentication.router;

import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import udpm.hn.studentattendance.core.authentication.oauth2.CustomOAuth2FailureHandler;
import udpm.hn.studentattendance.core.authentication.oauth2.CustomOAuth2SuccessHandler;
import udpm.hn.studentattendance.core.authentication.oauth2.CustomOAuth2UserService;
import udpm.hn.studentattendance.core.authentication.oauth2.JwtAuthenticationFilter;

import static udpm.hn.studentattendance.helpers.RouterHelper.appendPrefixApi;

@Component
@RequiredArgsConstructor
public class AuthenticationSecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

    private final CustomOAuth2FailureHandler customOAuth2FailureHandler;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public void configure(HttpSecurity http) throws Exception {
        http.rememberMe(AbstractHttpConfigurer::disable);

        http.oauth2Login(oauth2 -> {
            oauth2.loginPage(RouteAuthentication.REDIRECT_LOGIN);
            oauth2.userInfoEndpoint(userInfoEndpointConfig -> {
                userInfoEndpointConfig.userService(customOAuth2UserService);
            });
            oauth2.successHandler(customOAuth2SuccessHandler);
            oauth2.failureHandler(customOAuth2FailureHandler);
        });

        http.authorizeHttpRequests(authorization -> {
            authorization.requestMatchers(appendPrefixApi(RouteAuthentication.API_GET_ALL_FACILITY)).permitAll();
            authorization.requestMatchers(appendPrefixApi(RouteAuthentication.API_GET_INFO_USER, "/*")).permitAll();
            authorization.requestMatchers(RouteAuthentication.REDIRECT_LOGIN).permitAll();
        });

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

}
