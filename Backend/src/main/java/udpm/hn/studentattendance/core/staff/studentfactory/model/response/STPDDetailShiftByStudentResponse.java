package udpm.hn.studentattendance.core.staff.studentfactory.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface STPDDetailShiftByStudentResponse extends IsIdentify, HasOrderNumber {

    Long getStartDate();

    Long getEndDate();

    String getShift();

    String getStatus();

    Integer getType();

    Integer getRequiredIp();

    Integer getRequiredLocation();

    Long getStatusAttendance();


}
