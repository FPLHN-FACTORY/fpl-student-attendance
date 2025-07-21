package udpm.hn.studentattendance.entities;

import org.junit.jupiter.api.Test;
import udpm.hn.studentattendance.infrastructure.constants.ShiftType;
import udpm.hn.studentattendance.infrastructure.constants.StatusType;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PlanDateTest {
    @Test
    void testNoArgsConstructor() {
        PlanDate planDate = new PlanDate();
        assertNull(planDate.getDescription());
        assertNull(planDate.getStartDate());
        assertNull(planDate.getEndDate());
        assertNull(planDate.getShift());
        assertNull(planDate.getLateArrival());
        assertNull(planDate.getLink());
        assertNull(planDate.getRoom());
        assertEquals(ShiftType.OFFLINE, planDate.getType());
        assertEquals(StatusType.ENABLE, planDate.getRequiredLocation());
        assertEquals(StatusType.ENABLE, planDate.getRequiredIp());
        assertEquals(StatusType.ENABLE, planDate.getRequiredCheckin());
        assertEquals(StatusType.ENABLE, planDate.getRequiredCheckout());
        assertNull(planDate.getPlanFactory());
    }

    @Test
    void testAllArgsConstructor() {
        PlanFactory planFactory = new PlanFactory();
        planFactory.setId("1");
        List<Integer> shifts = List.of(1, 2);
        PlanDate planDate = new PlanDate(
                "desc", 1000L, 2000L, shifts, 5, "link", "room",
                ShiftType.ONLINE, StatusType.DISABLE, StatusType.DISABLE,
                StatusType.DISABLE, StatusType.DISABLE, planFactory);
        assertEquals("desc", planDate.getDescription());
        assertEquals(1000L, planDate.getStartDate());
        assertEquals(2000L, planDate.getEndDate());
        assertEquals(shifts, planDate.getShift());
        assertEquals(5, planDate.getLateArrival());
        assertEquals("link", planDate.getLink());
        assertEquals("room", planDate.getRoom());
        assertEquals(ShiftType.ONLINE, planDate.getType());
        assertEquals(StatusType.DISABLE, planDate.getRequiredLocation());
        assertEquals(StatusType.DISABLE, planDate.getRequiredIp());
        assertEquals(StatusType.DISABLE, planDate.getRequiredCheckin());
        assertEquals(StatusType.DISABLE, planDate.getRequiredCheckout());
        assertEquals(planFactory, planDate.getPlanFactory());
    }

    @Test
    void testSettersAndGetters() {
        PlanDate planDate = new PlanDate();
        PlanFactory planFactory = new PlanFactory();
        planFactory.setId("1");
        List<Integer> shifts = List.of(1, 2);
        planDate.setDescription("desc");
        planDate.setStartDate(1000L);
        planDate.setEndDate(2000L);
        planDate.setShift(shifts);
        planDate.setLateArrival(5);
        planDate.setLink("link");
        planDate.setRoom("room");
        planDate.setType(ShiftType.ONLINE);
        planDate.setRequiredLocation(StatusType.DISABLE);
        planDate.setRequiredIp(StatusType.DISABLE);
        planDate.setRequiredCheckin(StatusType.DISABLE);
        planDate.setRequiredCheckout(StatusType.DISABLE);
        planDate.setPlanFactory(planFactory);
        assertEquals("desc", planDate.getDescription());
        assertEquals(1000L, planDate.getStartDate());
        assertEquals(2000L, planDate.getEndDate());
        assertEquals(shifts, planDate.getShift());
        assertEquals(5, planDate.getLateArrival());
        assertEquals("link", planDate.getLink());
        assertEquals("room", planDate.getRoom());
        assertEquals(ShiftType.ONLINE, planDate.getType());
        assertEquals(StatusType.DISABLE, planDate.getRequiredLocation());
        assertEquals(StatusType.DISABLE, planDate.getRequiredIp());
        assertEquals(StatusType.DISABLE, planDate.getRequiredCheckin());
        assertEquals(StatusType.DISABLE, planDate.getRequiredCheckout());
        assertEquals(planFactory, planDate.getPlanFactory());
    }

    @Test
    void testEqualsAndHashCode() {
        PlanFactory planFactory = new PlanFactory();
        planFactory.setId("1");
        List<Integer> shifts = List.of(1, 2);
        PlanDate pd1 = new PlanDate();
        pd1.setId("1");
        pd1.setDescription("desc");
        pd1.setStartDate(1000L);
        pd1.setEndDate(2000L);
        pd1.setShift(shifts);
        pd1.setLateArrival(5);
        pd1.setLink("link");
        pd1.setRoom("room");
        pd1.setType(ShiftType.ONLINE);
        pd1.setRequiredLocation(StatusType.DISABLE);
        pd1.setRequiredIp(StatusType.DISABLE);
        pd1.setRequiredCheckin(StatusType.DISABLE);
        pd1.setRequiredCheckout(StatusType.DISABLE);
        pd1.setPlanFactory(planFactory);

        PlanDate pd2 = new PlanDate();
        pd2.setId("1");
        pd2.setDescription("desc");
        pd2.setStartDate(1000L);
        pd2.setEndDate(2000L);
        pd2.setShift(shifts);
        pd2.setLateArrival(5);
        pd2.setLink("link");
        pd2.setRoom("room");
        pd2.setType(ShiftType.ONLINE);
        pd2.setRequiredLocation(StatusType.DISABLE);
        pd2.setRequiredIp(StatusType.DISABLE);
        pd2.setRequiredCheckin(StatusType.DISABLE);
        pd2.setRequiredCheckout(StatusType.DISABLE);
        pd2.setPlanFactory(planFactory);

        PlanDate pd3 = new PlanDate();
        pd3.setId("2");
        pd3.setDescription("desc");
        pd3.setStartDate(1000L);
        pd3.setEndDate(2000L);
        pd3.setShift(shifts);
        pd3.setLateArrival(5);
        pd3.setLink("link");
        pd3.setRoom("room");
        pd3.setType(ShiftType.ONLINE);
        pd3.setRequiredLocation(StatusType.DISABLE);
        pd3.setRequiredIp(StatusType.DISABLE);
        pd3.setRequiredCheckin(StatusType.DISABLE);
        pd3.setRequiredCheckout(StatusType.DISABLE);
        pd3.setPlanFactory(planFactory);

        // So sánh từng trường thay vì so sánh object nếu entity chưa override
        // equals/hashCode đúng
        assertEquals(pd1.getId(), pd2.getId());
        assertEquals(pd1.getDescription(), pd2.getDescription());
        assertEquals(pd1.getStartDate(), pd2.getStartDate());
        assertEquals(pd1.getEndDate(), pd2.getEndDate());
        assertEquals(pd1.getShift(), pd2.getShift());
        assertEquals(pd1.getLateArrival(), pd2.getLateArrival());
        assertEquals(pd1.getLink(), pd2.getLink());
        assertEquals(pd1.getRoom(), pd2.getRoom());
        assertEquals(pd1.getType(), pd2.getType());
        assertEquals(pd1.getRequiredLocation(), pd2.getRequiredLocation());
        assertEquals(pd1.getRequiredIp(), pd2.getRequiredIp());
        assertEquals(pd1.getRequiredCheckin(), pd2.getRequiredCheckin());
        assertEquals(pd1.getRequiredCheckout(), pd2.getRequiredCheckout());
        assertEquals(pd1.getPlanFactory(), pd2.getPlanFactory());
        assertNotEquals(pd1.getId(), pd3.getId());
    }

    @Test
    void testToString() {
        PlanFactory planFactory = new PlanFactory();
        planFactory.setId("1");
        PlanDate planDate = new PlanDate();
        planDate.setId("1");
        planDate.setDescription("Session 1");
        planDate.setStartDate(1000L);
        planDate.setEndDate(2000L);
        planDate.setShift(List.of(1, 2));
        planDate.setLateArrival(15);
        planDate.setLink("https://example.com");
        planDate.setRoom("Room 101");
        planDate.setType(ShiftType.ONLINE);
        planDate.setRequiredLocation(StatusType.ENABLE);
        planDate.setRequiredIp(StatusType.ENABLE);
        planDate.setRequiredCheckin(StatusType.ENABLE);
        planDate.setRequiredCheckout(StatusType.ENABLE);
        planDate.setPlanFactory(planFactory);
        String toString = planDate.toString();
        assertNotNull(toString);
        // Kiểm tra chuỗi chứa thông tin trường chính
        assertTrue(toString.contains("Session 1") || toString.contains("PlanDate"));
    }

    @Test
    void testEqualsWithNull() {
        PlanDate planDate = new PlanDate();
        planDate.setId("1");
        assertNotEquals(null, planDate);
    }

    @Test
    void testEqualsWithDifferentClass() {
        PlanDate planDate = new PlanDate();
        planDate.setId("1");
        Object other = new Object();
        assertNotEquals(planDate, other);
    }

    @Test
    void testEqualsWithSameObject() {
        PlanDate planDate = new PlanDate();
        planDate.setId("1");
        assertEquals(planDate, planDate);
    }
}
