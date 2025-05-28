package udpm.hn.studentattendance.core.staff.statistics.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasAudit;
import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface SSPlanDateStudentFactoryResponse extends IsIdentify, HasOrderNumber, HasAudit {

    String getName();

    String getCode();

    Long getStartDate();

    Long getEndDate();

    String getShift();

    Integer getStatus();

    Integer getRequiredCheckin();

    Integer getRequiredCheckout();

}
