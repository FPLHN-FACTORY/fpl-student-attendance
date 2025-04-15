package udpm.hn.studentattendance.core.notification.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import udpm.hn.studentattendance.core.notification.model.request.NotificationAddRequest;
import udpm.hn.studentattendance.core.notification.model.request.NotificationFilterRequest;
import udpm.hn.studentattendance.core.notification.model.request.NotificationModifyRequest;
import udpm.hn.studentattendance.core.notification.model.response.NotificationResponse;
import udpm.hn.studentattendance.core.notification.repositories.NotificationExtendRepository;
import udpm.hn.studentattendance.core.notification.service.NotificationService;
import udpm.hn.studentattendance.entities.Notification;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationExtendRepository notificationExtendRepository;

    private final SessionHelper sessionHelper;

    @Override
    public ResponseEntity<?> getAllList(NotificationFilterRequest request) {
        request.setIdUser(sessionHelper.getUserId());
        if (request.getStatus() != null) {
            request.setEntityStatus(EntityStatus.fromValue(request.getStatus()));
        }
        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject<NotificationResponse> data = PageableObject.of(notificationExtendRepository.getAllByFilter(pageable, request));
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    @Override
    public ResponseEntity<?> deleteMultiple(NotificationModifyRequest request) {
        request.setIdUser(sessionHelper.getUserId());
        if (request.getIds() == null || request.getIds().isEmpty()) {
            return RouterHelper.responseError("Vui lòng chọn ít nhất 1 mục muốn xoá.");
        }

        int result = notificationExtendRepository.deleteMultipleById(request);
        if (result > 0) {
            return RouterHelper.responseSuccess("Xoá thành công " + result + " thông báo.");
        }
        return RouterHelper.responseError("Không có thông báo nào cần xoá");
    }

    @Override
    public ResponseEntity<?> deleteAll() {

        int result = notificationExtendRepository.deleteAllNotification(sessionHelper.getUserId());
        if (result > 0) {
            return RouterHelper.responseSuccess("Xoá thành công " + result + " thông báo.");
        }
        return RouterHelper.responseError("Không có thông báo nào cần xoá");
    }

    @Override
    public ResponseEntity<?> markRead(NotificationModifyRequest request) {
        request.setIdUser(sessionHelper.getUserId());
        if (request.getIds() == null || request.getIds().isEmpty()) {
            return RouterHelper.responseError("Vui lòng chọn ít nhất 1 mục muốn đánh dấu đã đọc.");
        }

        int result = notificationExtendRepository.markReadMultipleById(request);
        if (result > 0) {
            return RouterHelper.responseSuccess("Đánh dấu đã đọc thành công " + result + " thông báo.");
        }
        return RouterHelper.responseError("Không có thông báo nào cần đánh dấu");
    }

    @Override
    public ResponseEntity<?> markUnread(NotificationModifyRequest request) {
        request.setIdUser(sessionHelper.getUserId());
        if (request.getIds() == null || request.getIds().isEmpty()) {
            return RouterHelper.responseError("Vui lòng chọn ít nhất 1 mục muốn đánh dấu chưa đọc.");
        }

        int result = notificationExtendRepository.markUnreadMultipleById(request);
        if (result > 0) {
            return RouterHelper.responseSuccess("Đánh dấu chưa đọc thành công " + result + " thông báo.");
        }
        return RouterHelper.responseError("Không có thông báo nào cần đánh dấu");
    }

    @Override
    public ResponseEntity<?> markReadAll() {
        int result = notificationExtendRepository.markReadAll(sessionHelper.getUserId());
        if (result > 0) {
            return RouterHelper.responseSuccess("Đánh dấu đã đọc thành công " + result + " thông báo.");
        }
        return RouterHelper.responseError("Không có thông báo nào cần đánh dấu");
    }

    @Override
    public Notification add(NotificationAddRequest request) {
        if(!StringUtils.hasText(request.getIdUser())) {
            return null;
        }

        ObjectMapper objectMapper = new ObjectMapper();

        Notification notification = new Notification();
        notification.setIdUser(request.getIdUser());
        notification.setType(request.getType());
        try {
            String jsonData = objectMapper.writeValueAsString(request.getData());
            notification.setData(jsonData.equals("null") ? null : jsonData);
        } catch (JsonProcessingException e) {
            return null;
        }
        return notificationExtendRepository.save(notification);
    }

    @Override
    public ResponseEntity<?> count() {
        return RouterHelper.responseSuccess("Lấy dữ liệu thông báo thành công", notificationExtendRepository.count(sessionHelper.getUserId()));
    }

}
