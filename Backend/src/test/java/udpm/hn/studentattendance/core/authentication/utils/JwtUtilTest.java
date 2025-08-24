package udpm.hn.studentattendance.core.authentication.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import udpm.hn.studentattendance.core.authentication.oauth2.AuthUser;
import udpm.hn.studentattendance.helpers.SettingHelper;

import javax.crypto.SecretKey;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtUtilTest {

    @Mock
    private SettingHelper settingHelper;

    @InjectMocks
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(jwtUtil, "SECRET_KEY", "test-secret-key-test-secret-key-test-secret-key");

        // Mock SettingHelper to return a valid expiration time (30 minutes)
        when(settingHelper.getSetting(any(), eq(Integer.class))).thenReturn(30);
    }

    @Test
    @DisplayName("Test getSecretKey returns non-null key")
    void testGetSecretKey() {
        SecretKey key = jwtUtil.getSecretKey();
        assertNotNull(key);
    }

    @Test
    @DisplayName("Test generateToken and validateToken")
    void testGenerateAndValidateToken() {
        AuthUser user = new AuthUser();
        user.setId("user-1");
        user.setEmail("test@fpt.edu.vn");
        user.setRole(Set.of());
        user.setName("Test User");
        user.setCode("CODE1");
        user.setPicture("pic.png");
        user.setIdFacility("facility-1");

        String token = jwtUtil.generateToken(user.getEmail(), user);
        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
        assertEquals(user.getEmail(), jwtUtil.getEmailFromToken(token));
        assertEquals("facility-1", jwtUtil.getFacilityFromToken(token));
    }

    @Test
    @DisplayName("Test generateRefreshToken returns new token")
    void testGenerateRefreshToken() {
        AuthUser user = new AuthUser();
        user.setId("user-2");
        user.setEmail("refresh@fpt.edu.vn");
        user.setRole(Set.of());
        user.setName("Refresh User");
        user.setCode("CODE2");
        user.setPicture("pic2.png");
        user.setIdFacility("facility-2");

        String token = jwtUtil.generateToken(user.getEmail(), user);
        String refreshToken = jwtUtil.generateRefreshToken(token);
        assertNotNull(refreshToken);
        assertTrue(jwtUtil.validateToken(refreshToken));
    }

    @Test
    @DisplayName("Test getRoleFromToken returns correct roles")
    void testGetRoleFromToken() {
        AuthUser user = new AuthUser();
        user.setId("user-3");
        user.setEmail("role@fpt.edu.vn");
        user.setRole(Set.of());
        user.setName("Role User");
        user.setCode("CODE3");
        user.setPicture("pic3.png");
        user.setIdFacility("facility-3");

        String token = jwtUtil.generateToken(user.getEmail(), user);
        assertNotNull(token);
        assertNotNull(jwtUtil.getRoleFromToken(token));
    }

    @Test
    @DisplayName("Test validateToken returns false for invalid token")
    void testValidateTokenInvalid() {
        assertFalse(jwtUtil.validateToken("invalid.token.value"));
    }

    @Test
    @DisplayName("Test validateToken returns false for null token")
    void testValidateTokenNull() {
        assertFalse(jwtUtil.validateToken(null));
    }

    @Test
    @DisplayName("Test getAuthorization returns token without Bearer prefix")
    void testGetAuthorization() {
        String token = "test.jwt.token";
        String authorizationHeader = "Bearer " + token;

        // Mock HttpServletRequest
        org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest();
        request.addHeader("Authorization", authorizationHeader);

        String result = JwtUtil.getAuthorization(request);
        assertEquals(token, result);
    }

    @Test
    @DisplayName("Test getAuthorization returns null when no Authorization header")
    void testGetAuthorizationNoHeader() {
        org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest();

        String result = JwtUtil.getAuthorization(request);
        assertNull(result);
    }

    @Test
    @DisplayName("Test getAuthorization returns null when header doesn't start with Bearer")
    void testGetAuthorizationNoBearer() {
        org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest();
        request.addHeader("Authorization", "Basic dGVzdDp0ZXN0");

        String result = JwtUtil.getAuthorization(request);
        assertNull(result);
    }

    @Test
    @DisplayName("Test generateToken with existing token")
    void testGenerateTokenFromExistingToken() {
        AuthUser user = new AuthUser();
        user.setId("user-1");
        user.setEmail("test@fpt.edu.vn");
        user.setRole(Set.of());
        user.setName("Test User");
        user.setCode("CODE1");
        user.setPicture("pic.png");
        user.setIdFacility("facility-1");

        String originalToken = jwtUtil.generateToken(user.getEmail(), user);
        String newToken = jwtUtil.generateToken(originalToken);

        assertNotNull(newToken);
        assertNotEquals(originalToken, newToken);
        assertTrue(jwtUtil.validateToken(newToken));
    }

    @Test
    @DisplayName("Test getClaimsFromToken returns valid claims")
    void testGetClaimsFromToken() {
        AuthUser user = new AuthUser();
        user.setId("user-1");
        user.setEmail("test@fpt.edu.vn");
        user.setRole(Set.of());
        user.setName("Test User");
        user.setCode("CODE1");
        user.setPicture("pic.png");
        user.setIdFacility("facility-1");

        String token = jwtUtil.generateToken(user.getEmail(), user);

        var claims = jwtUtil.getClaimsFromToken(token);
        assertNotNull(claims);
        assertEquals(user.getEmail(), claims.getBody().getSubject());
    }
}
