package udpm.hn.studentattendance.core.support.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import udpm.hn.studentattendance.core.support.model.request.SupportEmailRequest;
import udpm.hn.studentattendance.helpers.MailerHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupportServiceImplTest {

    @Mock
    private SessionHelper sessionHelper;

    @Mock
    private MailerHelper mailerHelper;

    @InjectMocks
    private SupportServiceImpl supportService;

    @BeforeEach
    void setUp() {
        // Set up email configuration
        ReflectionTestUtils.setField(supportService, "supportEmailTo", "test@example.com");
    }

    @Test
    void testSendSupportMail_Success() {
        // Given
        SupportEmailRequest request = new SupportEmailRequest();
        request.setTitle("Test Support Request");
        request.setMessage("This is a test message");

        when(sessionHelper.getUserCode()).thenReturn("USER001");
        when(sessionHelper.getUserName()).thenReturn("Test User");
        when(sessionHelper.getUserEmail()).thenReturn("user@test.com");
        when(sessionHelper.getUserRole())
                .thenReturn(java.util.Set.of(udpm.hn.studentattendance.infrastructure.constants.RoleConstant.STUDENT));

        // When
        ResponseEntity<?> response = supportService.sendSupportMail(request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("SUCCESS", apiResponse.getStatus().name());
    }

    @Test
    void testSendSupportMail_WithFiles() {
        // Given
        SupportEmailRequest request = new SupportEmailRequest();
        request.setTitle("Test Support Request");
        request.setMessage("This is a test message with files");

        when(sessionHelper.getUserCode()).thenReturn("USER001");
        when(sessionHelper.getUserName()).thenReturn("Test User");
        when(sessionHelper.getUserEmail()).thenReturn("user@test.com");
        when(sessionHelper.getUserRole())
                .thenReturn(java.util.Set.of(udpm.hn.studentattendance.infrastructure.constants.RoleConstant.STUDENT));

        // When
        ResponseEntity<?> response = supportService.sendSupportMail(request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testSendSupportMail_Exception() {
        // Given
        SupportEmailRequest request = new SupportEmailRequest();
        request.setTitle("Test Support Request");
        request.setMessage("This is a test message");

        when(sessionHelper.getUserCode()).thenThrow(new RuntimeException("Test exception"));

        // When
        ResponseEntity<?> response = supportService.sendSupportMail(request);

        // Then
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("ERROR", apiResponse.getStatus().name());
    }
}