package udpm.hn.studentattendance.core.notification.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.notification.model.request.NotificationAddRequest;
import udpm.hn.studentattendance.core.notification.model.request.NotificationFilterRequest;
import udpm.hn.studentattendance.core.notification.model.request.NotificationModifyRequest;
import udpm.hn.studentattendance.core.notification.model.response.NotificationResponse;
import udpm.hn.studentattendance.core.notification.repositories.NotificationExtendRepository;
import udpm.hn.studentattendance.entities.Notification;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationExtendRepository notificationExtendRepository;

    @Mock
    private SessionHelper sessionHelper;

    @Mock
    private RedisInvalidationHelper redisInvalidationHelper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    @DisplayName("Test getAllList should return paginated notifications")
    void testGetAllList() {
        // Given
        String userId = "user-123";
        NotificationFilterRequest request = new NotificationFilterRequest();
        request.setPage(0);
        request.setSize(10);
        request.setStatus(1); // ACTIVE status

        List<NotificationResponse> notifications = Arrays.asList(
                new NotificationResponse("notif-1", 1, "{\"key\":\"value\"}", EntityStatus.ACTIVE, 1622540800000L),
                new NotificationResponse("notif-2", 2, "{\"key\":\"value2\"}", EntityStatus.ACTIVE, 1622540800000L));
        Page<NotificationResponse> page = new PageImpl<>(notifications);

        when(sessionHelper.getUserId()).thenReturn(userId);
        when(notificationExtendRepository.getAllByFilter(any(Pageable.class), any(NotificationFilterRequest.class)))
                .thenReturn(page);

        // When
        ResponseEntity<?> response = notificationService.getAllList(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy danh sách dữ liệu thành công", apiResponse.getMessage());

        // Verify request was updated with userId
        verify(notificationExtendRepository).getAllByFilter(any(Pageable.class),
                argThat(req -> req.getIdUser().equals(userId) && req.getEntityStatus() == EntityStatus.ACTIVE));
    }

    @Test
    @DisplayName("Test deleteMultiple should delete multiple notifications")
    void testDeleteMultiple() {
        // Given
        String userId = "user-123";
        List<String> notificationIds = Arrays.asList("notif-1", "notif-2");

        NotificationModifyRequest request = new NotificationModifyRequest();
        request.setIds(notificationIds);

        when(sessionHelper.getUserId()).thenReturn(userId);
        when(notificationExtendRepository.deleteMultipleById(any(NotificationModifyRequest.class))).thenReturn(2);

        // When
        ResponseEntity<?> response = notificationService.deleteMultiple(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Xoá thành công 2 thông báo.", apiResponse.getMessage());

        // Verify request was updated with userId
        verify(notificationExtendRepository).deleteMultipleById(
                argThat(req -> req.getIdUser().equals(userId) && req.getIds().equals(notificationIds)));
    }

    @Test
    @DisplayName("Test deleteMultiple should return error when no IDs provided")
    void testDeleteMultipleWithNoIds() {
        // Given
        String userId = "user-123";
        NotificationModifyRequest request = new NotificationModifyRequest();
        request.setIds(new ArrayList<>()); // Empty list

        when(sessionHelper.getUserId()).thenReturn(userId);

        // When
        ResponseEntity<?> response = notificationService.deleteMultiple(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Vui lòng chọn ít nhất 1 mục muốn xoá.", apiResponse.getMessage());

        // Verify repository method was never called
        verify(notificationExtendRepository, never()).deleteMultipleById(any());
    }

    @Test
    @DisplayName("Test deleteMultiple should return error when IDs list is null")
    void testDeleteMultipleWithNullIds() {
        // Given
        String userId = "user-123";
        NotificationModifyRequest request = new NotificationModifyRequest();
        request.setIds(null); // Null list

        when(sessionHelper.getUserId()).thenReturn(userId);

        // When
        ResponseEntity<?> response = notificationService.deleteMultiple(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Vui lòng chọn ít nhất 1 mục muốn xoá.", apiResponse.getMessage());

        // Verify repository method was never called
        verify(notificationExtendRepository, never()).deleteMultipleById(any());
    }

    @Test
    @DisplayName("Test deleteMultiple should return error when no notifications deleted")
    void testDeleteMultipleWithNoDeletions() {
        // Given
        String userId = "user-123";
        List<String> notificationIds = Arrays.asList("notif-1", "notif-2");

        NotificationModifyRequest request = new NotificationModifyRequest();
        request.setIds(notificationIds);

        when(sessionHelper.getUserId()).thenReturn(userId);
        when(notificationExtendRepository.deleteMultipleById(any(NotificationModifyRequest.class))).thenReturn(0);

        // When
        ResponseEntity<?> response = notificationService.deleteMultiple(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Không có thông báo nào cần xoá", apiResponse.getMessage());

        verify(notificationExtendRepository).deleteMultipleById(any(NotificationModifyRequest.class));
    }

    @Test
    @DisplayName("Test deleteAll should delete all notifications")
    void testDeleteAll() {
        // Given
        String userId = "user-123";
        when(sessionHelper.getUserId()).thenReturn(userId);
        when(notificationExtendRepository.deleteAllNotification(userId)).thenReturn(5);

        // When
        ResponseEntity<?> response = notificationService.deleteAll();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Xoá thành công 5 thông báo.", apiResponse.getMessage());

        verify(notificationExtendRepository).deleteAllNotification(userId);
    }

    @Test
    @DisplayName("Test deleteAll should return error when no notifications to delete")
    void testDeleteAllWithNoNotifications() {
        // Given
        String userId = "user-123";
        when(sessionHelper.getUserId()).thenReturn(userId);
        when(notificationExtendRepository.deleteAllNotification(userId)).thenReturn(0);

        // When
        ResponseEntity<?> response = notificationService.deleteAll();

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Không có thông báo nào cần xoá", apiResponse.getMessage());

        verify(notificationExtendRepository).deleteAllNotification(userId);
    }

    @Test
    @DisplayName("Test markRead should mark notifications as read")
    void testMarkRead() {
        // Given
        String userId = "user-123";
        List<String> notificationIds = Arrays.asList("notif-1", "notif-2");

        NotificationModifyRequest request = new NotificationModifyRequest();
        request.setIds(notificationIds);

        when(sessionHelper.getUserId()).thenReturn(userId);
        when(notificationExtendRepository.markReadMultipleById(any(NotificationModifyRequest.class))).thenReturn(2);

        // When
        ResponseEntity<?> response = notificationService.markRead(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Đánh dấu đã đọc thành công 2 thông báo.", apiResponse.getMessage());

        verify(notificationExtendRepository).markReadMultipleById(
                argThat(req -> req.getIdUser().equals(userId) && req.getIds().equals(notificationIds)));
    }

    @Test
    @DisplayName("Test markUnread should mark notifications as unread")
    void testMarkUnread() {
        // Given
        String userId = "user-123";
        List<String> notificationIds = Arrays.asList("notif-1", "notif-2");

        NotificationModifyRequest request = new NotificationModifyRequest();
        request.setIds(notificationIds);

        when(sessionHelper.getUserId()).thenReturn(userId);
        when(notificationExtendRepository.markUnreadMultipleById(any(NotificationModifyRequest.class))).thenReturn(2);

        // When
        ResponseEntity<?> response = notificationService.markUnread(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Đánh dấu chưa đọc thành công 2 thông báo.", apiResponse.getMessage());

        verify(notificationExtendRepository).markUnreadMultipleById(
                argThat(req -> req.getIdUser().equals(userId) && req.getIds().equals(notificationIds)));
    }

    @Test
    @DisplayName("Test markRead should return error when no IDs provided")
    void testMarkReadWithNoIds() {
        // Given
        String userId = "user-123";
        NotificationModifyRequest request = new NotificationModifyRequest();
        request.setIds(new ArrayList<>()); // Empty list

        when(sessionHelper.getUserId()).thenReturn(userId);

        // When
        ResponseEntity<?> response = notificationService.markRead(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Vui lòng chọn ít nhất 1 mục muốn đánh dấu đã đọc.", apiResponse.getMessage());

        // Verify repository method was never called
        verify(notificationExtendRepository, never()).markReadMultipleById(any());
    }

    @Test
    @DisplayName("Test markRead should return error when IDs list is null")
    void testMarkReadWithNullIds() {
        // Given
        String userId = "user-123";
        NotificationModifyRequest request = new NotificationModifyRequest();
        request.setIds(null); // Null list

        when(sessionHelper.getUserId()).thenReturn(userId);

        // When
        ResponseEntity<?> response = notificationService.markRead(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Vui lòng chọn ít nhất 1 mục muốn đánh dấu đã đọc.", apiResponse.getMessage());

        // Verify repository method was never called
        verify(notificationExtendRepository, never()).markReadMultipleById(any());
    }

    @Test
    @DisplayName("Test markRead should return error when no notifications marked")
    void testMarkReadWithNoMarkings() {
        // Given
        String userId = "user-123";
        List<String> notificationIds = Arrays.asList("notif-1", "notif-2");

        NotificationModifyRequest request = new NotificationModifyRequest();
        request.setIds(notificationIds);

        when(sessionHelper.getUserId()).thenReturn(userId);
        when(notificationExtendRepository.markReadMultipleById(any(NotificationModifyRequest.class))).thenReturn(0);

        // When
        ResponseEntity<?> response = notificationService.markRead(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Không có thông báo nào cần đánh dấu", apiResponse.getMessage());

        verify(notificationExtendRepository).markReadMultipleById(any(NotificationModifyRequest.class));
    }

    @Test
    @DisplayName("Test markUnread should return error when no IDs provided")
    void testMarkUnreadWithNoIds() {
        // Given
        String userId = "user-123";
        NotificationModifyRequest request = new NotificationModifyRequest();
        request.setIds(new ArrayList<>()); // Empty list

        when(sessionHelper.getUserId()).thenReturn(userId);

        // When
        ResponseEntity<?> response = notificationService.markUnread(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Vui lòng chọn ít nhất 1 mục muốn đánh dấu chưa đọc.", apiResponse.getMessage());

        // Verify repository method was never called
        verify(notificationExtendRepository, never()).markUnreadMultipleById(any());
    }

    @Test
    @DisplayName("Test markUnread should return error when IDs list is null")
    void testMarkUnreadWithNullIds() {
        // Given
        String userId = "user-123";
        NotificationModifyRequest request = new NotificationModifyRequest();
        request.setIds(null); // Null list

        when(sessionHelper.getUserId()).thenReturn(userId);

        // When
        ResponseEntity<?> response = notificationService.markUnread(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Vui lòng chọn ít nhất 1 mục muốn đánh dấu chưa đọc.", apiResponse.getMessage());

        // Verify repository method was never called
        verify(notificationExtendRepository, never()).markUnreadMultipleById(any());
    }

    @Test
    @DisplayName("Test markUnread should return error when no notifications marked")
    void testMarkUnreadWithNoMarkings() {
        // Given
        String userId = "user-123";
        List<String> notificationIds = Arrays.asList("notif-1", "notif-2");

        NotificationModifyRequest request = new NotificationModifyRequest();
        request.setIds(notificationIds);

        when(sessionHelper.getUserId()).thenReturn(userId);
        when(notificationExtendRepository.markUnreadMultipleById(any(NotificationModifyRequest.class))).thenReturn(0);

        // When
        ResponseEntity<?> response = notificationService.markUnread(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Không có thông báo nào cần đánh dấu", apiResponse.getMessage());

        verify(notificationExtendRepository).markUnreadMultipleById(any(NotificationModifyRequest.class));
    }

    @Test
    @DisplayName("Test markReadAll should mark all notifications as read")
    void testMarkReadAll() {
        // Given
        String userId = "user-123";
        when(sessionHelper.getUserId()).thenReturn(userId);
        when(notificationExtendRepository.markReadAll(userId)).thenReturn(5);

        // When
        ResponseEntity<?> response = notificationService.markReadAll();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Đánh dấu đã đọc thành công 5 thông báo.", apiResponse.getMessage());

        verify(notificationExtendRepository).markReadAll(userId);
    }

    @Test
    @DisplayName("Test markReadAll should return error when no notifications marked")
    void testMarkReadAllWithNoMarkings() {
        // Given
        String userId = "user-123";
        when(sessionHelper.getUserId()).thenReturn(userId);
        when(notificationExtendRepository.markReadAll(userId)).thenReturn(0);

        // When
        ResponseEntity<?> response = notificationService.markReadAll();

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Không có thông báo nào cần đánh dấu", apiResponse.getMessage());

        verify(notificationExtendRepository).markReadAll(userId);
    }

    @Test
    @DisplayName("Test add should create a notification")
    void testAdd() {
        // Given
        String userId = "user-123";
        Integer notificationType = 1;

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("key", "value");

        NotificationAddRequest request = new NotificationAddRequest();
        request.setIdUser(userId);
        request.setType(notificationType);
        request.setData(dataMap);

        Notification savedNotification = new Notification();
        savedNotification.setId("notif-1");
        savedNotification.setIdUser(userId);
        savedNotification.setType(notificationType);
        savedNotification.setData("{\"key\":\"value\"}");

        when(notificationExtendRepository.save(any(Notification.class))).thenReturn(savedNotification);

        // When
        Notification result = notificationService.add(request);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getIdUser());
        assertEquals(notificationType, result.getType());

        verify(notificationExtendRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("Test add should return null when idUser is empty")
    void testAddWithEmptyIdUser() {
        // Given
        NotificationAddRequest request = new NotificationAddRequest();
        request.setIdUser("");
        request.setType(1);
        request.setData(new HashMap<>());

        // When
        Notification result = notificationService.add(request);

        // Then
        assertNull(result);
        verify(notificationExtendRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test add should return null when idUser is null")
    void testAddWithNullIdUser() {
        // Given
        NotificationAddRequest request = new NotificationAddRequest();
        request.setIdUser(null);
        request.setType(1);
        request.setData(new HashMap<>());

        // When
        Notification result = notificationService.add(request);

        // Then
        assertNull(result);
        verify(notificationExtendRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test add should return null when idUser is blank")
    void testAddWithBlankIdUser() {
        // Given
        NotificationAddRequest request = new NotificationAddRequest();
        request.setIdUser("   "); // Blank string
        request.setType(1);
        request.setData(new HashMap<>());

        // When
        Notification result = notificationService.add(request);

        // Then
        assertNull(result);
        verify(notificationExtendRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test add should handle null data")
    void testAddWithNullData() {
        // Given
        String userId = "user-123";
        NotificationAddRequest request = new NotificationAddRequest();
        request.setIdUser(userId);
        request.setType(1);
        request.setData(null);

        Notification savedNotification = new Notification();
        savedNotification.setIdUser(userId);
        savedNotification.setType(1);
        savedNotification.setData(null);

        when(notificationExtendRepository.save(any(Notification.class))).thenReturn(savedNotification);

        // When
        Notification result = notificationService.add(request);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getIdUser());
        assertEquals("test-type", result.getType());
        assertNull(result.getData());

        verify(notificationExtendRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("Test add should handle empty data")
    void testAddWithEmptyData() {
        // Given
        String userId = "user-123";
        NotificationAddRequest request = new NotificationAddRequest();
        request.setIdUser(userId);
        request.setType(1);
        request.setData(new HashMap<>());

        Notification savedNotification = new Notification();
        savedNotification.setIdUser(userId);
        savedNotification.setType(1);
        savedNotification.setData("{}");

        when(notificationExtendRepository.save(any(Notification.class))).thenReturn(savedNotification);

        // When
        Notification result = notificationService.add(request);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getIdUser());
        assertEquals("test-type", result.getType());
        assertEquals("{}", result.getData());

        verify(notificationExtendRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("Test add should handle complex data object")
    void testAddWithComplexData() {
        // Given
        String userId = "user-123";
        Map<String, Object> complexData = new HashMap<>();
        complexData.put("message", "Test notification");
        complexData.put("timestamp", System.currentTimeMillis());
        complexData.put("metadata", Arrays.asList("item1", "item2"));

        NotificationAddRequest request = new NotificationAddRequest();
        request.setIdUser(userId);
        request.setType(1);
        request.setData(complexData);

        Notification savedNotification = new Notification();
        savedNotification.setIdUser(userId);
        savedNotification.setType(1);
        savedNotification.setData(
                "{\"message\":\"Test notification\",\"timestamp\":1234567890,\"metadata\":[\"item1\",\"item2\"]}");

        when(notificationExtendRepository.save(any(Notification.class))).thenReturn(savedNotification);

        // When
        Notification result = notificationService.add(request);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getIdUser());
        assertEquals("test-type", result.getType());
        assertNotNull(result.getData());
        assertTrue(result.getData().contains("Test notification"));

        verify(notificationExtendRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("Test getAllList should handle null status")
    void testGetAllListWithNullStatus() {
        // Given
        String userId = "user-123";
        NotificationFilterRequest request = new NotificationFilterRequest();
        request.setPage(0);
        request.setSize(10);
        request.setStatus(null); // Null status

        List<NotificationResponse> notifications = Arrays.asList(
                new NotificationResponse("notif-1", 1, "{\"key\":\"value\"}", EntityStatus.ACTIVE, 1622540800000L));
        Page<NotificationResponse> page = new PageImpl<>(notifications);

        when(sessionHelper.getUserId()).thenReturn(userId);
        when(notificationExtendRepository.getAllByFilter(any(Pageable.class), any(NotificationFilterRequest.class)))
                .thenReturn(page);

        // When
        ResponseEntity<?> response = notificationService.getAllList(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());

        // Verify request was updated with userId but no EntityStatus
        verify(notificationExtendRepository).getAllByFilter(any(Pageable.class),
                argThat(req -> req.getIdUser().equals(userId) && req.getEntityStatus() == null));
    }

    @Test
    @DisplayName("Test getAllList should handle invalid status value")
    void testGetAllListWithInvalidStatus() {
        // Given
        String userId = "user-123";
        NotificationFilterRequest request = new NotificationFilterRequest();
        request.setPage(0);
        request.setSize(10);
        request.setStatus(999); // Invalid status

        List<NotificationResponse> notifications = Arrays.asList(
                new NotificationResponse("notif-1", 1, "{\"key\":\"value\"}", EntityStatus.ACTIVE, 1622540800000L));
        Page<NotificationResponse> page = new PageImpl<>(notifications);

        when(sessionHelper.getUserId()).thenReturn(userId);
        when(notificationExtendRepository.getAllByFilter(any(Pageable.class), any(NotificationFilterRequest.class)))
                .thenReturn(page);

        // When
        ResponseEntity<?> response = notificationService.getAllList(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());

        // Verify request was updated with userId but EntityStatus.fromValue returns
        // null for invalid value
        verify(notificationExtendRepository).getAllByFilter(any(Pageable.class),
                argThat(req -> req.getIdUser().equals(userId) && req.getEntityStatus() == null));
    }

    @Test
    @DisplayName("Test count should return notification count")
    void testCount() {
        // Given
        String userId = "user-123";
        int count = 5;

        when(sessionHelper.getUserId()).thenReturn(userId);
        when(notificationExtendRepository.count(userId)).thenReturn(count);

        // When
        ResponseEntity<?> response = notificationService.count();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy dữ liệu thông báo thành công", apiResponse.getMessage());
        assertEquals(count, apiResponse.getData());

        verify(notificationExtendRepository).count(userId);
    }
}
