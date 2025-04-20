package udpm.hn.studentattendance.core.notification.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.notification.model.request.NotificationAddRequest;
import udpm.hn.studentattendance.core.notification.model.request.NotificationModifyRequest;
import udpm.hn.studentattendance.core.notification.model.request.NotificationFilterRequest;
import udpm.hn.studentattendance.entities.Notification;

import java.util.Map;

public interface NotificationService {

    ResponseEntity<?> getAllList(NotificationFilterRequest request);

    ResponseEntity<?> deleteMultiple(NotificationModifyRequest request);

    ResponseEntity<?> deleteAll();

    ResponseEntity<?> markRead(NotificationModifyRequest request);

    ResponseEntity<?> markUnread(NotificationModifyRequest request);

    ResponseEntity<?> markReadAll();

    Notification add(NotificationAddRequest request);

    ResponseEntity<?> count();

}
