package udpm.hn.studentattendance.helpers;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import udpm.hn.studentattendance.infrastructure.config.mailer.model.MailerDefaultRequest;
import udpm.hn.studentattendance.infrastructure.constants.ExecutorConstants;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class MailerHelper {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromDefault;

    private final static String FOLDER_TEMPLATE = "templates/mailer";

    public final static String TEMPLATE_DEFAULT = "default.html";

    public final static String TEMPLATE_CHANGE_STATUS_FACILITY = "change-status-facility.html";

    public final static String TEMPLATE_CHANGE_STATUS_ADMIN = "change-status-admin.html";
    public final static String HEADER_DEFAULT = "";

    public final static String FOOTER_DEFAULT = """
                <ul class="list-unstyled">
                    <li>Lưu ý : Đây là email tự động vui lòng không phải hồi email này.</li>
                    <li>Mọi thắc mắc xin liên hệ xưởng dự án của Bộ môn Phát Triển Phần Mềm.</li>
                </ul>
            """;

    @Async(ExecutorConstants.TASK_EXECUTOR)
    public CompletableFuture<Boolean> send(MailerDefaultRequest request) {
        String content = request.getTemplate() != null ? loadTemplate(request.getTemplate()) : request.getContent();

        if (Objects.isNull(content)) {
            LogHelper.error("Gửi mail thất bại!");
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
        MimeMessageHelper mimeMessageHelper = null;
        try {

            String from = request.getFrom() != null ? request.getFrom() : fromDefault;

            mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.toString());
            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(request.getTo());

            if (request.getBcc() != null && request.getBcc().length > 0) {
                mimeMessageHelper.setBcc(request.getBcc());
            }

            mimeMessageHelper.setText(content, true);
            mimeMessageHelper.setSubject(request.getTitle());

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            LogHelper.error("Không thể gửi mail", e);
            return CompletableFuture.completedFuture(false);
        }
        return CompletableFuture.completedFuture(true);
    }


    public static String loadTemplate(String template_name) {
        try {
            return Files.readString(new ClassPathResource(buildPathTemplate(template_name)).getFile().toPath());
        } catch (IOException e) {
            LogHelper.error("Không thể tải template mailer: ", e);
            return "";
        }
    }

    public static String loadTemplate(String template_name, Map<String, Object> data) {
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
