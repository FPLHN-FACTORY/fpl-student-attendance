package udpm.hn.studentattendance.core.teacher.factory.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasAudit;
import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface TCPlanDateStudentResponse extends IsIdentify, HasOrderNumber, HasAudit {

    String getCode();

    String getName();

    Integer getStatus();

    Long getLateCheckin();

    Long getLateCheckout();

}
