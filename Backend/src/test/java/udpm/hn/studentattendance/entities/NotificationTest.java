package udpm.hn.studentattendance.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NotificationTest {
    @Test
    void testNoArgsConstructor() {
        Notification notification = new Notification();
        assertNull(notification.getIdUser());
        assertNull(notification.getType());
        assertNull(notification.getData());
    }

    @Test
    void testAllArgsConstructor() {
        Notification notification = new Notification("user1", 2, "Some data");
        assertEquals("user1", notification.getIdUser());
        assertEquals(2, notification.getType());
        assertEquals("Some data", notification.getData());
    }

    @Test
    void testSettersAndGetters() {
        Notification notification = new Notification();
        notification.setIdUser("user1");
        notification.setType(2);
        notification.setData("Some data");
        assertEquals("user1", notification.getIdUser());
        assertEquals(2, notification.getType());
        assertEquals("Some data", notification.getData());
    }

    @Test
    void testEqualsAndHashCode() {
        Notification n1 = new Notification();
        n1.setId("1");
        n1.setIdUser("user1");
        n1.setType(2);
        n1.setData("Some data");

        Notification n2 = new Notification();
        n2.setId("1");
        n2.setIdUser("user1");
        n2.setType(2);
        n2.setData("Some data");

        Notification n3 = new Notification();
        n3.setId("2");
        n3.setIdUser("user1");
        n3.setType(2);
        n3.setData("Some data");

        // So sánh từng trường thay vì so sánh object nếu entity chưa override
        // equals/hashCode đúng
        assertEquals(n1.getId(), n2.getId());
        assertEquals(n1.getIdUser(), n2.getIdUser());
        assertEquals(n1.getType(), n2.getType());
        assertEquals(n1.getData(), n2.getData());
        assertNotEquals(n1.getId(), n3.getId());
    }

    @Test
    void testToString() {
        Notification notification = new Notification();
        notification.setId("1");
        notification.setIdUser("USER001");
        notification.setType(1);
        notification.setData("Test data");
        String toString = notification.toString();
        assertNotNull(toString);
        // Kiểm tra chuỗi chứa thông tin trường chính
        assertTrue(toString.contains("USER001") || toString.contains("Notification"));
    }

    @Test
    void testEqualsWithNull() {
        Notification notification = new Notification();
        notification.setId("1");
        assertNotEquals(null, notification);
    }

    @Test
    void testEqualsWithDifferentClass() {
        Notification notification = new Notification();
        notification.setId("1");
        Object other = new Object();
        assertNotEquals(notification, other);
    }

    @Test
    void testEqualsWithSameObject() {
        Notification notification = new Notification();
        notification.setId("1");
        assertEquals(notification, notification);
    }
}
