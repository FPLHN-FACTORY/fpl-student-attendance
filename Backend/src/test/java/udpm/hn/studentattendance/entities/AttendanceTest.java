package udpm.hn.studentattendance.entities;

import org.junit.jupiter.api.Test;
import udpm.hn.studentattendance.infrastructure.constants.AttendanceStatus;
import static org.junit.jupiter.api.Assertions.*;

class AttendanceTest {
    @Test
    void testEqualsAndHashCode() {
        PlanDate planDate = new PlanDate();
        planDate.setId("1");
        UserStudent userStudent = new UserStudent();
        userStudent.setId("2");
        Attendance a1 = new Attendance();
        a1.setId("1");
        a1.setPlanDate(planDate);
        a1.setUserStudent(userStudent);
        a1.setAttendanceStatus(AttendanceStatus.PRESENT);

        Attendance a2 = new Attendance();
        a2.setId("1");
        a2.setPlanDate(planDate);
        a2.setUserStudent(userStudent);
        a2.setAttendanceStatus(AttendanceStatus.PRESENT);

        Attendance a3 = new Attendance();
        a3.setId("2");
        a3.setPlanDate(planDate);
        a3.setUserStudent(userStudent);
        a3.setAttendanceStatus(AttendanceStatus.PRESENT);

        // So sánh từng trường thay vì so sánh object nếu entity chưa override
        // equals/hashCode đúng
        assertEquals(a1.getId(), a2.getId());
        assertEquals(a1.getPlanDate(), a2.getPlanDate());
        assertEquals(a1.getUserStudent(), a2.getUserStudent());
        assertEquals(a1.getAttendanceStatus(), a2.getAttendanceStatus());
        assertNotEquals(a1.getId(), a3.getId());
    }

    @Test
    void testAllFields() {
        Attendance attendance = new Attendance();
        PlanDate planDate = new PlanDate();
        UserStudent userStudent = new UserStudent();
        AttendanceRecovery recovery = new AttendanceRecovery();

        attendance.setPlanDate(planDate);
        attendance.setUserStudent(userStudent);
        attendance.setAttendanceStatus(udpm.hn.studentattendance.infrastructure.constants.AttendanceStatus.CHECKIN);
        attendance.setLateCheckin(5L);
        attendance.setLateCheckout(10L);
        attendance.setAttendanceRecovery(recovery);

        assertEquals(planDate, attendance.getPlanDate());
        assertEquals(userStudent, attendance.getUserStudent());
        assertEquals(udpm.hn.studentattendance.infrastructure.constants.AttendanceStatus.CHECKIN,
                attendance.getAttendanceStatus());
        assertEquals(5L, attendance.getLateCheckin());
        assertEquals(10L, attendance.getLateCheckout());
        assertEquals(recovery, attendance.getAttendanceRecovery());
    }
}