package udpm.hn.studentattendance.core.authentication.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import udpm.hn.studentattendance.core.authentication.model.request.AuthenticationUserRequest;
import udpm.hn.studentattendance.core.authentication.router.RouteAuthentication;
import udpm.hn.studentattendance.core.authentication.utils.JwtUtil;
import udpm.hn.studentattendance.infrastructure.constants.SessionConstant;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    private final HttpSession httpSession;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        CustomOAuth2User oauthUser = (CustomOAuth2User) authentication.getPrincipal();

        String email = oauthUser.getAttribute("email");

        Map<String, Object> dataUser = new HashMap<>();
        dataUser.put("id", oauthUser.getId());
        dataUser.put("role", oauthUser.getRole());
        dataUser.put("facilityID", oauthUser.getIdFacility());
        dataUser.put("name", oauthUser.getName());
        dataUser.put("code", oauthUser.getCode());
        dataUser.put("picture", oauthUser.getPicture());

        String token = jwtUtil.generateToken(email, dataUser);

        String redirect_uri = (String) httpSession.getAttribute(SessionConstant.LOGIN_REDIRECT);

        response.sendRedirect(redirect_uri + "?" + RouteAuthentication.PARAM_LOGIN_SUCCESS + "=" + token);
    }

}
