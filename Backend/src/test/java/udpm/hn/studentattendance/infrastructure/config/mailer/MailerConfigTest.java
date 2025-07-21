package udpm.hn.studentattendance.infrastructure.config.mailer;

import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import static org.junit.jupiter.api.Assertions.*;

class MailerConfigTest {
    @Test
    void testMailerConfigExists() {
        MailerConfig config = new MailerConfig();
        assertNotNull(config);
    }

    @Test
    void testJavaMailSenderBean() throws Exception {
        MailerConfig config = new MailerConfig();
        assertNotNull(config.getClass().getMethod("getJavaMailSender"));
    }
}
