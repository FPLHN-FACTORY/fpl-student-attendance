package udpm.hn.studentattendance.core.teacher.factory.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasAudit;
import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface TCPlanDateStudentFactoryResponse extends IsIdentify, HasOrderNumber, HasAudit {

    String getName();

    String getCode();

    Long getStartDate();

    Long getEndDate();

    String getShift();

    Integer getStatus();

    Integer getRequiredCheckin();

    Integer getRequiredCheckout();

}
