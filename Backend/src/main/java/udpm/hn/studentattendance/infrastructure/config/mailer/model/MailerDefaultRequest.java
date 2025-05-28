package udpm.hn.studentattendance.infrastructure.config.mailer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.helpers.MailerHelper;

import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MailerDefaultRequest {

    private String to;

    private String from;

    private String[] bcc;

    private String title;

    private String content;

    private Map<String, byte[]> attachments;

    private String template = MailerHelper.TEMPLATE_DEFAULT;

    private String header = MailerHelper.HEADER_DEFAULT;

    private String footer = MailerHelper.FOOTER_DEFAULT;

}
