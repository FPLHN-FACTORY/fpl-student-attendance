package udpm.hn.studentattendance.core.support.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.support.model.request.SupportEmailRequest;

public interface SupportService {

    ResponseEntity<?> sendSupportMail(SupportEmailRequest supportEmailRequest);
}
