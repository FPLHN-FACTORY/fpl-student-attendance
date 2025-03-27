package udpm.hn.studentattendance.core.student.attendance.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface SAAttendanceResponse extends IsIdentify, HasOrderNumber {

    String getIdPlanDate();

    Long getDate();

    Long getTimeCheckin();

    String getFactoryName();

    String getTeacherName();

    Integer getShift();

    Integer getType();

    Integer getStatus();

    Long getLateArrival();

}
