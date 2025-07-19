package udpm.hn.studentattendance.helpers;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import udpm.hn.studentattendance.infrastructure.config.mailer.model.MailerDefaultRequest;
import udpm.hn.studentattendance.infrastructure.constants.ExecutorConstants;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class MailerHelper {

    private static final Logger logger = LoggerFactory.getLogger(MailerHelper.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromDefault;

    private final static String FOLDER_TEMPLATE = "templates/mailer";

    public final static String TEMPLATE_DEFAULT = "default.html";

    public final static String TEMPLATE_CHANGE_STATUS_FACILITY = "change-status-facility.html";

    public final static String TEMPLATE_CHANGE_STATUS_ADMIN = "change-status-admin.html";

    public final static String TEMPLATE_STATISTICS_STAFF = "statistics-staff.html";

    public final static String TEMPLATE_STATISTICS_TEACHER = "statistics-teacher.html";

    public final static String TEMPLATE_UPCOMING_SCHEDULE_PLAN_DATE = "upcoming-schedule-plandate.html";

    public final static String TEMPLATE_STATISTICS_DAILY = "statistics-staff.html";

    public final static String HEADER_DEFAULT = "";

    public final static String FOOTER_DEFAULT = """
                <ul class="list-unstyled">
                    <li>Lưu ý : Đây là email tự động vui lòng không phải hồi email này.</li>
                    <li>Mọi thắc mắc xin liên hệ xưởng dự án của Bộ môn Phát Triển Phần Mềm.</li>
                </ul>
            """;

    @Async(ExecutorConstants.TASK_EXECUTOR)
    public CompletableFuture<Boolean> send(MailerDefaultRequest request) {
        if (request == null) {
            return CompletableFuture.completedFuture(false);
        }

        String content = request.getTemplate() != null ? loadTemplate(request.getTemplate()) : request.getContent();

        if (Objects.isNull(content)) {
            return CompletableFuture.completedFuture(false);
        }

        if (!Objects.isNull(request.getHeader())) {
            content = content.replace("{{ HEADER }}", request.getHeader());
        }

        if (!Objects.isNull(request.getFooter())) {
            content = content.replace("{{ FOOTER }}", request.getFooter());
        }

        if (request.getTemplate() != null && !Objects.isNull(request.getContent())) {
            content = content.replace("{{ CONTENT }}", request.getContent());
        }

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        if (mimeMessage == null) {
            return CompletableFuture.completedFuture(false);
        }

        MimeMessageHelper mimeMessageHelper = null;
        try {

            String from = request.getFrom() != null ? request.getFrom() : fromDefault;

            mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.toString());
            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(request.getTo());
            mimeMessageHelper.setReplyTo(from);

            if (request.getBcc() != null && request.getBcc().length > 0) {
                mimeMessageHelper.setBcc(request.getBcc());
            }

            if (request.getAttachments() != null) {
                for (Map.Entry<String, byte[]> entry : request.getAttachments().entrySet()) {
                    String filename = entry.getKey();
                    byte[] fileData = entry.getValue();
                    ByteArrayResource resource = new ByteArrayResource(fileData);
                    mimeMessageHelper.addAttachment(filename, resource);
                }
            }

            mimeMessageHelper.setText(content, true);
            mimeMessageHelper.setSubject(request.getTitle());

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            logger.warn(e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
        logger.info("Gửi email thành công: " + request.getTitle());
        return CompletableFuture.completedFuture(true);
    }

    public static String loadTemplate(String template_name) {
        try {
            ClassPathResource resource = new ClassPathResource(buildPathTemplate(template_name));
            try (InputStream inputStream = resource.getInputStream()) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            logger.error("Không thể tải template: " + template_name);
            return "";
        }
    }

    public static String loadTemplate(String template_name, Map<String, Object> data) {
        if (data == null) {
            return loadTemplate(template_name);
        }

        String content = loadTemplate(template_name);
        for (String key : data.keySet()) {
            Object value = data.get(key);
            content = content.replace("{{ " + key + " }}", value != null ? value.toString() : "");
        }
        return content;
    }

    private static String buildPathTemplate(String template_name) {
        return FOLDER_TEMPLATE + "/" + template_name;
    }

}
