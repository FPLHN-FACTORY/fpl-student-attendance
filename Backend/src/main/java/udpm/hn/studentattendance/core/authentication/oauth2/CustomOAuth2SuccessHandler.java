package udpm.hn.studentattendance.core.authentication.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import udpm.hn.studentattendance.core.authentication.model.request.AuthenticationToken;
import udpm.hn.studentattendance.helpers.ValidateHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAuthenticationConstant;
import udpm.hn.studentattendance.core.authentication.utils.JwtUtil;
import udpm.hn.studentattendance.infrastructure.constants.SessionConstant;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

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

        String accessToken = jwtUtil.generateToken(email, oauthUser);
        String refreshToken = jwtUtil.generateRefreshToken(accessToken);

        AuthenticationToken authenticationToken = new AuthenticationToken(accessToken, refreshToken);

        ObjectMapper mapper = new ObjectMapper();
        String jsonToken = mapper.writeValueAsString(authenticationToken);
        String redirect_uri = (String) httpSession.getAttribute(SessionConstant.LOGIN_REDIRECT);

        String data = Base64.getEncoder().encodeToString(jsonToken.getBytes(StandardCharsets.UTF_8));
        response.sendRedirect(redirect_uri + "?" + RouteAuthenticationConstant.PARAM_ROUTE_ROLE + "=" + role + "&"
                + RouteAuthenticationConstant.PARAM_LOGIN_SUCCESS + "=" + data);
    }

}
