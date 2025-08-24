package udpm.hn.studentattendance.core.notification.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.notification.service.NotificationService;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NotificationRestControllerTest {
    @Mock
    private NotificationService notificationService;
    
    @InjectMocks
    private NotificationRestController notificationRestController;

    @Test
    void testNotificationRestControllerExists() {
        assertNotNull(notificationRestController);
    }

    @Test
    void testGetAllList() {
        udpm.hn.studentattendance.core.notification.model.request.NotificationFilterRequest request = 
            new udpm.hn.studentattendance.core.notification.model.request.NotificationFilterRequest();
        when(notificationService.getAllList(request)).thenReturn(ResponseEntity.ok().build());
        
        ResponseEntity<?> response = notificationRestController.getAllList(request);
        
        assertNotNull(response);
        verify(notificationService).getAllList(request);
    }

    @Test
    void testCount() {
        when(notificationService.count()).thenReturn(ResponseEntity.ok().build());
        
        ResponseEntity<?> response = notificationRestController.count();
        
        assertNotNull(response);
        verify(notificationService).count();
    }

    @Test
    void testDeleteMultiple() {
        udpm.hn.studentattendance.core.notification.model.request.NotificationModifyRequest request = 
            new udpm.hn.studentattendance.core.notification.model.request.NotificationModifyRequest();
        when(notificationService.deleteMultiple(request)).thenReturn(ResponseEntity.ok().build());
        
        ResponseEntity<?> response = notificationRestController.deleteMultiple(request);
        
        assertNotNull(response);
        verify(notificationService).deleteMultiple(request);
    }
} 
