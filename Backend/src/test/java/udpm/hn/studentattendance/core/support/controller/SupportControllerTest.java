package udpm.hn.studentattendance.core.support.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import udpm.hn.studentattendance.core.support.service.SupportService;
import udpm.hn.studentattendance.core.support.model.request.SupportEmailRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class SupportControllerTest {

    @Mock
    private SupportService supportService;

    @InjectMocks
    private SupportController supportController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendSupportRequest() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes());

        // Mock the service response
        when(supportService.sendSupportMail(any(SupportEmailRequest.class)))
                .thenReturn(ResponseEntity.ok().build());

        // When
        ResponseEntity<?> response = supportController.sendSupportRequest(
                "Test Support Request",
                "This is a test support message",
                java.util.List.of(file));

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
    }
}