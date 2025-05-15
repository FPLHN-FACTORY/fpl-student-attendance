package udpm.hn.studentattendance.core.staff.factory.model.response.userstudentfactory;

import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface Staff_PDDetailShiftByStudentResponse extends IsIdentify, HasOrderNumber {

    Long getStartDate();

    Long getEndDate();

    String getShift();

    String getStatus();

    Integer getType();

    Integer getRequiredIp();

    Integer getRequiredLocation();

    Long getStatusAttendance();


}
