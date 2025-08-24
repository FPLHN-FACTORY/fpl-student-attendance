package udpm.hn.studentattendance.core.authentication.oauth2;

import org.junit.jupiter.api.Test;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AuthUserTest {
    @Test
    void testAllArgsConstructorAndGettersSetters() {
        AuthUser user = new AuthUser("id1", "Test Name", Set.of(RoleConstant.ADMIN), "facility1", "code1", "pic1", "email1", "emailFe1", "emailFpt1");
        assertEquals("id1", user.getId());
        assertEquals("Test Name", user.getName());
        assertEquals(Set.of(RoleConstant.ADMIN), user.getRole());
        assertEquals("facility1", user.getIdFacility());
        assertEquals("code1", user.getCode());
        assertEquals("pic1", user.getPicture());
        assertEquals("email1", user.getEmail());
        assertEquals("emailFe1", user.getEmailFe());
        assertEquals("emailFpt1", user.getEmailFpt());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        AuthUser user = new AuthUser();
        user.setId("id2");
        user.setName("Name2");
        user.setRole(Set.of(RoleConstant.STAFF));
        user.setIdFacility("facility2");
        user.setCode("code2");
        user.setPicture("pic2");
        user.setEmail("email2");
        user.setEmailFe("emailFe2");
        user.setEmailFpt("emailFpt2");
        assertEquals("id2", user.getId());
        assertEquals("Name2", user.getName());
        assertEquals(Set.of(RoleConstant.STAFF), user.getRole());
        assertEquals("facility2", user.getIdFacility());
        assertEquals("code2", user.getCode());
        assertEquals("pic2", user.getPicture());
        assertEquals("email2", user.getEmail());
        assertEquals("emailFe2", user.getEmailFe());
        assertEquals("emailFpt2", user.getEmailFpt());
    }

    @Test
    void testToString() {
        AuthUser user = new AuthUser("id3", "Name3", Set.of(RoleConstant.STUDENT), "facility3", "code3", "pic3", "email3", "emailFe3", "emailFpt3");
        String str = user.toString();
        assertTrue(str.contains("id3"));
        assertTrue(str.contains("Name3"));
        assertTrue(str.contains("STUDENT"));
        assertTrue(str.contains("facility3"));
        assertTrue(str.contains("code3"));
        assertTrue(str.contains("pic3"));
        assertTrue(str.contains("email3"));
        assertTrue(str.contains("emailFe3"));
        assertTrue(str.contains("emailFpt3"));
    }
}
