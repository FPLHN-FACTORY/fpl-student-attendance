package udpm.hn.studentattendance.core.support.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import udpm.hn.studentattendance.core.support.service.SupportService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SupportController.class)
class SupportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SupportService supportService;

    @Test
    void testSendSupportRequest() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes());

        // When & Then
        mockMvc.perform(multipart("/api/v1/support/send-mail")
                .file(file)
                .param("title", "Test Support Request")
                .param("message", "This is a test support message"))
                .andExpect(status().isOk());
    }
}