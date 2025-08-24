package udpm.hn.studentattendance.helpers;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.util.ReflectionTestUtils;
import udpm.hn.studentattendance.infrastructure.config.mailer.model.MailerDefaultRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.MockedConstruction;

@ExtendWith(MockitoExtension.class)
class MailerHelperTest {
    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @Mock
    private MimeMessageHelper mimeMessageHelper;

    private MailerHelper mailerHelper;

    @BeforeEach
    void setUp() {
        mailerHelper = new MailerHelper(mailSender);
        ReflectionTestUtils.setField(mailerHelper, "fromDefault", "test@example.com");
    }

    @Test
    void testMailerHelperExists() {
        assertNotNull(MailerHelper.class);
    }

    @Test
    void testSendWithValidRequest() throws Exception {
        MailerDefaultRequest request = new MailerDefaultRequest();
        request.setTo("test@example.com");
        request.setTitle("Test Subject");
        request.setContent("Test Content");

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        CompletableFuture<Boolean> result = mailerHelper.send(request);
        assertTrue(result.get());
    }

    @Test
    void testSendWithNullRequest() throws Exception {
        // Test với null request - mong đợi trả về false
        CompletableFuture<Boolean> result = mailerHelper.send(null);
        assertFalse(result.get());
    }

    @Test
    void testSendWithNullContent() throws Exception {
        MailerDefaultRequest request = new MailerDefaultRequest();
        request.setTo("test@example.com");
        request.setTitle("Test Subject");
        // No content set - mong đợi trả về false vì content là null

        CompletableFuture<Boolean> result = mailerHelper.send(request);
        assertFalse(result.get());
    }

    @Test
    void testSendWithTemplate() throws Exception {
        MailerDefaultRequest request = new MailerDefaultRequest();
        request.setTo("test@example.com");
        request.setTitle("Test Subject");
        request.setTemplate("default.html");
        request.setContent("Test Content");

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        CompletableFuture<Boolean> result = mailerHelper.send(request);
        assertTrue(result.get());
    }

    @Test
    void testSendWithHeaderAndFooter() throws Exception {
        MailerDefaultRequest request = new MailerDefaultRequest();
        request.setTo("test@example.com");
        request.setTitle("Test Subject");
        request.setContent("Test Content");
        request.setHeader("Custom Header");
        request.setFooter("Custom Footer");

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        CompletableFuture<Boolean> result = mailerHelper.send(request);
        assertTrue(result.get());
    }

    @Test
    void testSendWithBcc() throws Exception {
        MailerDefaultRequest request = new MailerDefaultRequest();
        request.setTo("test@example.com");
        request.setTitle("Test Subject");
        request.setContent("Test Content");
        request.setBcc(new String[] { "bcc1@example.com", "bcc2@example.com" });

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        CompletableFuture<Boolean> result = mailerHelper.send(request);
        assertTrue(result.get());
    }

    @Test
    void testSendWithAttachments() throws Exception {
        MailerDefaultRequest request = new MailerDefaultRequest();
        request.setTo("test@example.com");
        request.setTitle("Test Subject");
        request.setContent("Test Content");

        Map<String, byte[]> attachments = new HashMap<>();
        attachments.put("test.txt", "test content".getBytes());
        request.setAttachments(attachments);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        CompletableFuture<Boolean> result = mailerHelper.send(request);
        assertTrue(result.get());
    }

    @Test
    void testSendWithCustomFrom() throws Exception {
        MailerDefaultRequest request = new MailerDefaultRequest();
        request.setTo("test@example.com");
        request.setTitle("Test Subject");
        request.setContent("Test Content");
        request.setFrom("custom@example.com");

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        CompletableFuture<Boolean> result = mailerHelper.send(request);
        assertTrue(result.get());
    }

    @Test
    void testSendWithMessagingException() throws Exception {
        MailerDefaultRequest request = new MailerDefaultRequest();
        request.setTo("test@example.com");
        request.setTitle("Test Subject");
        request.setContent("Test Content");

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        // Mock MimeMessageHelper constructor to throw MessagingException
        try (MockedConstruction<MimeMessageHelper> mockedConstruction = mockConstruction(MimeMessageHelper.class,
                (mock, context) -> {
                    when(mock.getMimeMessage()).thenReturn(mimeMessage);
                    doThrow(new jakarta.mail.MessagingException("Test exception")).when(mock).setFrom(anyString());
                })) {

            CompletableFuture<Boolean> result = mailerHelper.send(request);
            assertFalse(result.get());
        }
    }

    @Test
    void testLoadTemplateWithValidTemplate() {
        String result = MailerHelper.loadTemplate("default.html");
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testLoadTemplateWithInvalidTemplate() {
        String result = MailerHelper.loadTemplate("invalid-template.html");
        assertEquals("", result);
    }

    @Test
    void testLoadTemplateWithData() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "John");
        data.put("age", 25);

        String result = MailerHelper.loadTemplate("default.html", data);
        assertNotNull(result);
    }

    @Test
    void testLoadTemplateWithNullData() {
        // Test với null data - mong đợi trả về template gốc không thay đổi
        String result = MailerHelper.loadTemplate("default.html", null);
        assertNotNull(result);
        // Không nên có NullPointerException
    }

    @Test
    void testLoadTemplateWithEmptyData() {
        Map<String, Object> data = new HashMap<>();
        String result = MailerHelper.loadTemplate("default.html", data);
        assertNotNull(result);
    }

    @Test
    void testLoadTemplateWithNullValues() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", null);
        data.put("age", 25);

        String result = MailerHelper.loadTemplate("default.html", data);
        assertNotNull(result);
    }

    @Test
    void testConstants() {
        assertEquals("default.html", MailerHelper.TEMPLATE_DEFAULT);
        assertEquals("change-status-facility.html", MailerHelper.TEMPLATE_CHANGE_STATUS_FACILITY);
        assertEquals("change-status-admin.html", MailerHelper.TEMPLATE_CHANGE_STATUS_ADMIN);
        assertEquals("statistics-staff.html", MailerHelper.TEMPLATE_STATISTICS_STAFF);
        assertEquals("statistics-teacher.html", MailerHelper.TEMPLATE_STATISTICS_TEACHER);
        assertEquals("upcoming-schedule-plandate.html", MailerHelper.TEMPLATE_UPCOMING_SCHEDULE_PLAN_DATE);
        assertEquals("statistics-staff.html", MailerHelper.TEMPLATE_STATISTICS_DAILY);
        assertEquals("", MailerHelper.HEADER_DEFAULT);
        assertNotNull(MailerHelper.FOOTER_DEFAULT);
        assertTrue(MailerHelper.FOOTER_DEFAULT.contains("Lưu ý"));
    }

    @Test
    void testSendWithEmptyRequest() throws Exception {
        MailerDefaultRequest request = new MailerDefaultRequest();
        // No fields set - mong đợi trả về false vì thiếu các trường bắt buộc

        CompletableFuture<Boolean> result = mailerHelper.send(request);
        assertFalse(result.get());
    }

    @Test
    void testSendWithTemplateAndNoContent() throws Exception {
        MailerDefaultRequest request = new MailerDefaultRequest();
        request.setTo("test@example.com");
        request.setTitle("Test Subject");
        request.setTemplate("default.html");
        // No content set - mong đợi trả về true vì có template

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        CompletableFuture<Boolean> result = mailerHelper.send(request);
        assertTrue(result.get());
    }

    @Test
    void testSendWithNullBcc() throws Exception {
        MailerDefaultRequest request = new MailerDefaultRequest();
        request.setTo("test@example.com");
        request.setTitle("Test Subject");
        request.setContent("Test Content");
        request.setBcc(null);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        CompletableFuture<Boolean> result = mailerHelper.send(request);
        assertTrue(result.get());
    }

    @Test
    void testSendWithEmptyBcc() throws Exception {
        MailerDefaultRequest request = new MailerDefaultRequest();
        request.setTo("test@example.com");
        request.setTitle("Test Subject");
        request.setContent("Test Content");
        request.setBcc(new String[0]);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        CompletableFuture<Boolean> result = mailerHelper.send(request);
        assertTrue(result.get());
    }

    @Test
    void testSendWithNullAttachments() throws Exception {
        MailerDefaultRequest request = new MailerDefaultRequest();
        request.setTo("test@example.com");
        request.setTitle("Test Subject");
        request.setContent("Test Content");
        request.setAttachments(null);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        CompletableFuture<Boolean> result = mailerHelper.send(request);
        assertTrue(result.get());
    }

    @Test
    void testLoadTemplateWithInvalidTemplateAndData() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "John");

        String result = MailerHelper.loadTemplate("invalid-template.html", data);
        assertEquals("", result);
    }
}
