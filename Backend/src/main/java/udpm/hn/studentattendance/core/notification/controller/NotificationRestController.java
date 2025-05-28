package udpm.hn.studentattendance.core.notification.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import udpm.hn.studentattendance.core.notification.model.request.NotificationModifyRequest;
import udpm.hn.studentattendance.core.notification.model.request.NotificationFilterRequest;
import udpm.hn.studentattendance.core.notification.service.NotificationService;
import udpm.hn.studentattendance.infrastructure.constants.RoutesConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RoutesConstant.PREFIX_API_NOTIFICATION)
public class NotificationRestController {

    private final NotificationService notificationService;

    @GetMapping("/list")
    public ResponseEntity<?> getAllList(@Valid NotificationFilterRequest request) {
        return notificationService.getAllList(request);
    }

    @GetMapping("/count")
    public ResponseEntity<?> count() {
        return notificationService.count();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteMultiple(@RequestBody NotificationModifyRequest request) {
        return notificationService.deleteMultiple(request);
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAll() {
        return notificationService.deleteAll();
    }

    @PutMapping("/mark-read-all")
    public ResponseEntity<?> markReadAll() {
        return notificationService.markReadAll();
    }

    @PutMapping("/mark-read")
    public ResponseEntity<?> markRead(@RequestBody NotificationModifyRequest request) {
        return notificationService.markRead(request);
    }

    @PutMapping("/mark-unread")
    public ResponseEntity<?> markUnread(@RequestBody NotificationModifyRequest request) {
        return notificationService.markUnread(request);
    }

}
