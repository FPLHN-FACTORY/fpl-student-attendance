package udpm.hn.studentattendance.core.authentication.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAuthenticationConstant;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.constants.SessionConstant;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class CustomOAuth2FailureHandler implements AuthenticationFailureHandler {

    private final HttpSession httpSession;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(ApiResponse.error(exception.getMessage()));
        String data = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        String redirect_uri = (String) httpSession.getAttribute(SessionConstant.LOGIN_REDIRECT);

        response.sendRedirect(redirect_uri + "?" + RouteAuthenticationConstant.PARAM_LOGIN_FAILURE + "=" + data);
    }

}
