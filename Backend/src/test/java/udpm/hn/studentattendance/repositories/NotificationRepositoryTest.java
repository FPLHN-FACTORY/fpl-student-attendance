package udpm.hn.studentattendance.repositories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import udpm.hn.studentattendance.entities.Notification;
import udpm.hn.studentattendance.infrastructure.config.TestDatabaseConfig;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.context.annotation.Import;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@Import(TestDatabaseConfig.class)
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void testSaveAndFindById() {
        // Given
        Notification notification = new Notification();
        notification.setIdUser("USER001");
        notification.setType(1); // ATTENDANCE
        notification.setData("Please check in for your class today");

        // When
        Notification savedNotification = notificationRepository.save(notification);
        Optional<Notification> foundNotification = notificationRepository.findById(savedNotification.getId());

        // Then
        assertTrue(foundNotification.isPresent());
        assertEquals("USER001", foundNotification.get().getIdUser());
        assertEquals("Please check in for your class today", foundNotification.get().getData());
        assertEquals(1, foundNotification.get().getType());
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

        // When
        notificationRepository.save(notification1);
        notificationRepository.save(notification2);
        List<Notification> allNotifications = notificationRepository.findAll();

        // Then
        assertTrue(allNotifications.size() >= 2);
        assertTrue(allNotifications.stream().anyMatch(n -> "USER001".equals(n.getIdUser())));
        assertTrue(allNotifications.stream().anyMatch(n -> "USER002".equals(n.getIdUser())));
    }

    @Test
    void testUpdateNotification() {
        // Given
        Notification notification = new Notification();
        notification.setIdUser("USER001");
        notification.setType(1); // ATTENDANCE
        notification.setData("Original Content");

        Notification savedNotification = notificationRepository.save(notification);

        // When
        savedNotification.setData("Updated Content");
        savedNotification.setType(2); // SYSTEM
        Notification updatedNotification = notificationRepository.save(savedNotification);

        // Then
        assertEquals("Updated Content", updatedNotification.getData());
        assertEquals(2, updatedNotification.getType());
    }

    @Test
    void testDeleteNotification() {
        // Given
        Notification notification = new Notification();
        notification.setIdUser("USER001");
        notification.setType(1);
        notification.setData("Will be deleted");

        Notification savedNotification = notificationRepository.save(notification);
        String notificationId = savedNotification.getId();

        // When
        notificationRepository.deleteById(notificationId);
        Optional<Notification> deletedNotification = notificationRepository.findById(notificationId);

        // Then
        assertFalse(deletedNotification.isPresent());
    }
}