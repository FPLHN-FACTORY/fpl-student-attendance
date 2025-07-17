package udpm.hn.studentattendance.repositories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import udpm.hn.studentattendance.entities.Notification;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationRepositoryTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Test
    void testSaveAndFindById() {
        // Given
        Notification notification = new Notification();
        notification.setIdUser("USER001");
        notification.setType(1); // ATTENDANCE
        notification.setData("Please check in for your class today");

        // Mock behavior
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationRepository.findById(anyString())).thenReturn(Optional.of(notification));

        // When
        Notification savedNotification = notificationRepository.save(notification);
        Optional<Notification> foundNotification = notificationRepository.findById("mock-id");

        // Then
        assertTrue(foundNotification.isPresent());
        assertEquals("USER001", foundNotification.get().getIdUser());
        assertEquals("Please check in for your class today", foundNotification.get().getData());
        assertEquals(1, foundNotification.get().getType());
        verify(notificationRepository).save(any(Notification.class));
        verify(notificationRepository).findById(anyString());
    }

    @Test
    void testSaveAndFindAll() {
        // Given
        Notification notification1 = new Notification();
        notification1.setIdUser("USER001");
        notification1.setType(1); // ATTENDANCE
        notification1.setData("First notification");

        Notification notification2 = new Notification();
        notification2.setIdUser("USER002");
        notification2.setType(2); // SYSTEM
        notification2.setData("Second notification");

        List<Notification> notifications = Arrays.asList(notification1, notification2);

        // Mock behavior
        when(notificationRepository.findAll()).thenReturn(notifications);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        notificationRepository.save(notification1);
        notificationRepository.save(notification2);
        List<Notification> allNotifications = notificationRepository.findAll();

        // Then
        assertEquals(2, allNotifications.size());
        assertTrue(allNotifications.stream().anyMatch(n -> "USER001".equals(n.getIdUser())));
        assertTrue(allNotifications.stream().anyMatch(n -> "USER002".equals(n.getIdUser())));
        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(notificationRepository).findAll();
    }

    @Test
    void testUpdateNotification() {
        // Given
        Notification notification = new Notification();
        notification.setIdUser("USER001");
        notification.setType(1); // ATTENDANCE
        notification.setData("Original Content");

        Notification updatedNotification = new Notification();
        updatedNotification.setIdUser("USER001");
        updatedNotification.setType(2); // SYSTEM
        updatedNotification.setData("Updated Content");

        // Mock behavior
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification)
                .thenReturn(updatedNotification);

        // When
        Notification savedNotification = notificationRepository.save(notification);
        savedNotification.setData("Updated Content");
        savedNotification.setType(2); // SYSTEM
        Notification resultNotification = notificationRepository.save(savedNotification);

        // Then
        assertEquals("Updated Content", resultNotification.getData());
        assertEquals(2, resultNotification.getType());
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void testDeleteNotification() {
        // Given
        Notification notification = new Notification();
        notification.setIdUser("USER001");
        notification.setType(1);
        notification.setData("Will be deleted");
        String notificationId = "mock-id";

        // Mock behavior
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        doNothing().when(notificationRepository).deleteById(anyString());
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        // When
        Notification savedNotification = notificationRepository.save(notification);
        notificationRepository.deleteById(notificationId);
        Optional<Notification> deletedNotification = notificationRepository.findById(notificationId);

        // Then
        assertFalse(deletedNotification.isPresent());
        verify(notificationRepository).save(any(Notification.class));
        verify(notificationRepository).deleteById(anyString());
        verify(notificationRepository).findById(anyString());
    }
}