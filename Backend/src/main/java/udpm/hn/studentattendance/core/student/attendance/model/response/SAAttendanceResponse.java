package udpm.hn.studentattendance.core.student.attendance.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface SAAttendanceResponse extends IsIdentify, HasOrderNumber {

    String getIdPlanDate();

    Long getStartDate();

    Long getEndDate();

    Long getTimeCheckin();

    String getFactoryName();

    String getTeacherName();

    String getShift();

    String getLink();

    Integer getType();

    Integer getStatus();

    Long getLateArrival();

    Integer getRequiredCheckin();

    Integer getRequiredCheckout();

    Integer getTotalLateAttendance();

    Integer getCurrentLateAttendance();

}
