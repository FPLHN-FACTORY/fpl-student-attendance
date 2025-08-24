package udpm.hn.studentattendance.core.teacher.studentattendance.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasAudit;
import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface TeacherStudentAttendanceResponse extends IsIdentify, HasOrderNumber, HasAudit {

    String getCode();

    String getIdAttendance();

    String getName();

    Integer getRequiredCheckin();

    Integer getRequiredCheckout();

    Integer getStatus();

}
