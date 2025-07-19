package udpm.hn.studentattendance.core.staff.plan.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasAudit;
import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface SPDPlanDateStudentResponse extends IsIdentify, HasOrderNumber, HasAudit {

    String getCode();

    String getName();

    Integer getStatus();

    Long getLateCheckin();

    Long getLateCheckout();
    
}
