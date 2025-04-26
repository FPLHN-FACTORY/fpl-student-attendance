package udpm.hn.studentattendance.core.authentication.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAuthenticationConstant;
import udpm.hn.studentattendance.core.authentication.utils.JwtUtil;
import udpm.hn.studentattendance.infrastructure.constants.SessionConstant;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    private final HttpSession httpSession;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {

        CustomOAuth2User oauthUser = (CustomOAuth2User) authentication.getPrincipal();

        String role = (String) httpSession.getAttribute(SessionConstant.LOGIN_ROLE);

        String email = oauthUser.getAttribute("email");

        String token = jwtUtil.generateToken(email, oauthUser);

        String redirect_uri = (String) httpSession.getAttribute(SessionConstant.LOGIN_REDIRECT);

        response.sendRedirect(redirect_uri + "?" + RouteAuthenticationConstant.PARAM_ROUTE_ROLE + "=" + role + "&"
                + RouteAuthenticationConstant.PARAM_LOGIN_SUCCESS + "=" + token);
    }

}
