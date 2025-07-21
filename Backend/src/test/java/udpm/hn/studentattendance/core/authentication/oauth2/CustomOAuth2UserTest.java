package udpm.hn.studentattendance.core.authentication.oauth2;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomOAuth2UserTest {
    @Test
    void testGettersAndAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", "Test User");
        attributes.put("email", "test@fpt.edu.vn");
        attributes.put("picture", "pic.png");
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttributes()).thenReturn(attributes);
        when(oAuth2User.getAttribute("name")).thenReturn("Test User");
        when(oAuth2User.getAttribute("email")).thenReturn("test@fpt.edu.vn");
        when(oAuth2User.getAttribute("picture")).thenReturn("pic.png");

        CustomOAuth2User user = new CustomOAuth2User(oAuth2User);
        user.setRole(Set.of(RoleConstant.ADMIN, RoleConstant.STAFF));

        assertEquals(attributes, user.getAttributes());
        assertEquals("Test User", user.getName());
        assertEquals("test@fpt.edu.vn", user.getEmail());
        assertEquals("pic.png", user.getPicture());
        assertEquals("test", user.getCode());
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ADMIN")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("STAFF")));
    }

    @Test
    void testGetCodeWithInvalidEmail() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("email")).thenReturn("invalidemail");
        CustomOAuth2User user = new CustomOAuth2User(oAuth2User);
        user.setRole(Set.of());
        assertEquals("invalidemail", user.getCode());
    }
}
