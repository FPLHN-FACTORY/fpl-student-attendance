package udpm.hn.studentattendance.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AttendanceTest {
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