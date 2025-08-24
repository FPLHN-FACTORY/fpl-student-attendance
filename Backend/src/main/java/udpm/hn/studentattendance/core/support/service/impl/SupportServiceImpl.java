package udpm.hn.studentattendance.core.support.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import udpm.hn.studentattendance.core.support.model.request.SupportEmailRequest;
import udpm.hn.studentattendance.core.support.service.SupportService;
import udpm.hn.studentattendance.helpers.MailerHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.config.mailer.model.MailerDefaultRequest;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SupportServiceImpl implements SupportService {

    private final SessionHelper sessionHelper;
    private final MailerHelper mailerHelper;

    @Value("${spring.mail.username}")
    private String supportEmailTo;

    @Override
    public ResponseEntity<?> sendSupportMail(SupportEmailRequest supportEmailRequest) {
        try {
            String userCode = sessionHelper.getUserCode();
            String userEmail = sessionHelper.getUserEmail();
            Set<RoleConstant> userRoles = sessionHelper.getUserRole();
            String userRole = userRoles != null && !userRoles.isEmpty() ? userRoles.iterator().next().name()
                    : "Không xác định";
            String userRoleName = userRole.equals("STAFF") ? "Phụ Trách Xưởng"
                    : userRole.equals("TEACHER") ? "Giảng Viên"
                            : userRole.equals("STUDENT") ? "Sinh Viên" : "Admin";


            Map<String, Object> adminTemplateData = new HashMap<>();
            adminTemplateData.put("TITLE", supportEmailRequest.getTitle());
            adminTemplateData.put("MESSAGE", supportEmailRequest.getMessage());
            adminTemplateData.put("SENDER_EMAIL", userEmail != null ? userEmail : "Không xác định");
            adminTemplateData.put("USER_CODE", userCode != null ? userCode : "Không xác định");
            adminTemplateData.put("USER_ROLE", userRoleName != null ? userRoleName : "Không xác định");
            adminTemplateData.put("TIMESTAMP",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

            Map<String, Object> userTemplateData = new HashMap<>();
            userTemplateData.put("TITLE", supportEmailRequest.getTitle());
            userTemplateData.put("MESSAGE", supportEmailRequest.getMessage());
            userTemplateData.put("TIMESTAMP",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

            Map<String, byte[]> attachments = new HashMap<>();
            if (supportEmailRequest.getFiles() != null && supportEmailRequest.getFiles().length > 0) {
                StringBuilder attachmentsHtml = new StringBuilder();
                attachmentsHtml.append("<div class=\"info-section\"><h3>Tệp đính kèm</h3><div class=\"attachments\">");

                for (MultipartFile file : supportEmailRequest.getFiles()) {
                    if (!file.isEmpty()) {
                        try {
                            attachments.put(file.getOriginalFilename(), file.getBytes());
                            attachmentsHtml.append("<div class=\"attachment-item\">📎 ")
                                    .append(file.getOriginalFilename())
                                    .append(" (").append(formatFileSize(file.getSize())).append(")</div>");
                        } catch (IOException e) {
                            // Log error but continue
                        }
                    }
                }
                attachmentsHtml.append("</div></div>");
                adminTemplateData.put("ATTACHMENTS_SECTION", attachmentsHtml.toString());
                userTemplateData.put("ATTACHMENTS_SECTION", attachmentsHtml.toString());
            } else {
                adminTemplateData.put("ATTACHMENTS_SECTION", "");
                userTemplateData.put("ATTACHMENTS_SECTION", "");
            }

            String adminEmailContent = MailerHelper.loadTemplate(MailerHelper.TEMPLATE_SUPPORT, adminTemplateData);
            MailerDefaultRequest adminMailRequest = new MailerDefaultRequest();
            adminMailRequest.setTitle("Yêu cầu hỗ trợ: " + supportEmailRequest.getTitle());
            adminMailRequest.setContent(adminEmailContent);
            adminMailRequest.setTo(supportEmailTo);
            adminMailRequest.setAttachments(attachments);
            mailerHelper.send(adminMailRequest);

            String userEmailContent = MailerHelper.loadTemplate(MailerHelper.TEMPLATE_SUPPORT_CONFIRMATION,
                    userTemplateData);
            MailerDefaultRequest userMailRequest = new MailerDefaultRequest();
            userMailRequest.setTitle("Xác nhận yêu cầu hỗ trợ: " + supportEmailRequest.getTitle());
            userMailRequest.setContent(userEmailContent);
            userMailRequest.setTo(userEmail);
            mailerHelper.send(userMailRequest);

            return RouterHelper.responseSuccess(
                    ("Yêu cầu hỗ trợ đã được gửi thành công! Email xác nhận đã được gửi đến " + userEmail));

        } catch (Exception e) {
            return RouterHelper.responseError(
                    ("Có lỗi xảy ra khi gửi yêu cầu hỗ trợ: " + e.getMessage()));
        }
    }

    private String formatFileSize(long size) {
        if (size < 1024)
            return size + " B";
        if (size < 1024 * 1024)
            return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024)
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
    }
}
