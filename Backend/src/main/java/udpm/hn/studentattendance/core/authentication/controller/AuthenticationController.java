package udpm.hn.studentattendance.core.authentication.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.function.EntityResponse;
import org.springframework.web.servlet.view.RedirectView;
import udpm.hn.studentattendance.core.authentication.router.RouteAuthentication;
import udpm.hn.studentattendance.core.authentication.services.AuthenticationService;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;

import java.io.IOException;


@Controller
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @GetMapping(RouteAuthentication.REDIRECT_LOGIN)
    public RedirectView login(
            @RequestParam(name = "role", required = false) String role,
            @RequestParam(name = "redirect_uri", required = false) String redirectUri,
            @RequestParam(name = "facility_id", required = false) String facilityId
    ) throws IOException {
        return authenticationService.authorSwitch(role, redirectUri, facilityId);
    }


}
