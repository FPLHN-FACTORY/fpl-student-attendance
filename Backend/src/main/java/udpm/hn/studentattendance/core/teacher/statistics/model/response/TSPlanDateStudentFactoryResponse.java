package udpm.hn.studentattendance.core.teacher.statistics.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasAudit;
import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface TSPlanDateStudentFactoryResponse extends IsIdentify, HasOrderNumber, HasAudit {

    String getName();

    String getCode();

    Long getStartDate();

    Long getEndDate();

    String getShift();

    Integer getStatus();

    Integer getRequiredCheckin();

    Integer getRequiredCheckout();

}
