package udpm.hn.studentattendance.core.authentication.oauth2;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationUserAdminRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationUserStaffRepository;
import udpm.hn.studentattendance.core.authentication.repositories.AuthenticationUserStudentRepository;
import udpm.hn.studentattendance.core.authentication.utils.JwtUtil;
import udpm.hn.studentattendance.entities.UserAdmin;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    private final SessionHelper sessionHelper;

    private final AuthenticationUserAdminRepository authenticationUserAdminRepository;

    private final AuthenticationUserStaffRepository authenticationUserStaffRepository;

    private final AuthenticationUserStudentRepository authenticationUserStudentRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);

            if (jwtUtil.validateToken(token)) {
                String email = jwtUtil.getEmailFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);
                String facilityID = jwtUtil.getFacilityFromToken(token);

                boolean isAuthentication = true;

                AuthUser authUser = new AuthUser();

                RoleConstant roleCode;
                try {
                    roleCode = RoleConstant.valueOf(role.toUpperCase());

                    switch (roleCode) {

                        case ADMIN:
                            Optional<UserAdmin> userAdmin = authenticationUserAdminRepository.findByEmail(email);
                            if (userAdmin.isEmpty()) {
                                isAuthentication = false;
                            }
                            authUser = sessionHelper.buildAuthUser(userAdmin.get(), roleCode, facilityID);
                            break;

                        case STAFF:
                            Optional<UserStaff> userStaff = authenticationUserStaffRepository.findLoginStaff(email, roleCode, facilityID);
                            if (userStaff.isEmpty()) {
                                isAuthentication = false;
                            }
                            authUser = sessionHelper.buildAuthUser(userStaff.get(), roleCode, facilityID);
                            break;

                        case STUDENT:
                            Optional<UserStudent> userStudent = authenticationUserStudentRepository.findByEmailAndFacility_Id(email, facilityID);
                            if (userStudent.isEmpty()) {
                                isAuthentication = false;
                            }
                            authUser = sessionHelper.buildAuthUser(userStudent.get(), roleCode, facilityID);
                            break;
                    }
                } catch (Exception e) {
                }


                if (isAuthentication) {

                    sessionHelper.setCurrentUser(authUser);

                    List<GrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority(role));

                    Authentication auth = new UsernamePasswordAuthenticationToken(null, token, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }

        chain.doFilter(request, response);
    }

}
