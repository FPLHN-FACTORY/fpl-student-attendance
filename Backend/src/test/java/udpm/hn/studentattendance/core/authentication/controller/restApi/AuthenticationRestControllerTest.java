package udpm.hn.studentattendance.core.authentication.controller.restApi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import udpm.hn.studentattendance.core.authentication.model.request.AuthenticationStudentRegisterRequest;
import udpm.hn.studentattendance.core.authentication.model.request.AuthenticationToken;
import udpm.hn.studentattendance.core.authentication.services.AuthenticationService;
import udpm.hn.studentattendance.infrastructure.constants.SettingKeys;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

class AuthenticationRestControllerTest {
    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationRestController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRefreshToken() {
        AuthenticationToken token = new AuthenticationToken();
        token.setRefreshToken("refresh");
        when(authenticationService.refreshToken("refresh")).thenReturn((ResponseEntity) ResponseEntity.ok("ok"));
        ResponseEntity<?> res = controller.refreshToken(token);
        assertEquals(200, res.getStatusCode().value());
        assertEquals("ok", res.getBody());
    }

    @Test
    void testGetAvatar() {
        Map<String, String> req = new HashMap<>();
        req.put("url_image", "url");
        when(authenticationService.getAvatar("url")).thenReturn((ResponseEntity) ResponseEntity.ok("avatar"));
        ResponseEntity<?> res = controller.getAvatar(req);
        assertEquals(200, res.getStatusCode().value());
        assertEquals("avatar", res.getBody());
    }

    @Test
    void testGetSettings() {
        when(authenticationService.getSettings()).thenReturn((ResponseEntity) ResponseEntity.ok("settings"));
        ResponseEntity<?> res = controller.getSettings();
        assertEquals(200, res.getStatusCode().value());
        assertEquals("settings", res.getBody());
    }

    @Test
    void testSaveSettings() {
        Map<SettingKeys, String> req = new HashMap<>();
        when(authenticationService.saveSettings(req)).thenReturn((ResponseEntity) ResponseEntity.ok("saved"));
        ResponseEntity<?> res = controller.saveSettings(req);
        assertEquals(200, res.getStatusCode().value());
        assertEquals("saved", res.getBody());
    }

    @Test
    void testGetAllFacility() {
        when(authenticationService.getAllFacility()).thenReturn((ResponseEntity) ResponseEntity.ok("facilities"));
        ResponseEntity<?> res = controller.getAllFacility();
        assertEquals(200, res.getStatusCode().value());
        assertEquals("facilities", res.getBody());
    }

    @Test
    void testGetAllSemester() {
        when(authenticationService.getAllSemester()).thenReturn((ResponseEntity) ResponseEntity.ok("semesters"));
        ResponseEntity<?> res = controller.getAllSemester();
        assertEquals(200, res.getStatusCode().value());
        assertEquals("semesters", res.getBody());
    }

    @Test
    void testGetInfoUser() {
        Map<String, String> req = new HashMap<>();
        req.put("role", "admin");
        when(authenticationService.getInfoUser("admin")).thenReturn((ResponseEntity) ResponseEntity.ok("info"));
        ResponseEntity<?> res = controller.getInfoUser(req);
        assertEquals(200, res.getStatusCode().value());
        assertEquals("info", res.getBody());
    }

    @Test
    void testStudentRegister() {
        AuthenticationStudentRegisterRequest req = new AuthenticationStudentRegisterRequest();
        MultipartFile image = mock(MultipartFile.class);
        when(authenticationService.studentRegister(req, image))
                .thenReturn((ResponseEntity) ResponseEntity.ok("registered"));
        ResponseEntity<?> res = controller.studentRegister(req, image);
        assertEquals(200, res.getStatusCode().value());
        assertEquals("registered", res.getBody());
    }

    @Test
    void testStudentUpdateFaceID() {
        MultipartFile image = mock(MultipartFile.class);
        when(authenticationService.studentUpdateFaceID(image))
                .thenReturn((ResponseEntity) ResponseEntity.ok("updated"));
        ResponseEntity<?> res = controller.studentUpdateFaceID(image);
        assertEquals(200, res.getStatusCode().value());
        assertEquals("updated", res.getBody());
    }

    @Test
    void testStudentInfo() {
        when(authenticationService.studentInfo()).thenReturn((ResponseEntity) ResponseEntity.ok("studentInfo"));
        ResponseEntity<?> res = controller.studentInfo();
        assertEquals(200, res.getStatusCode().value());
        assertEquals("studentInfo", res.getBody());
    }
}
