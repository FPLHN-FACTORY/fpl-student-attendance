package udpm.hn.studentattendance.core.staff.plan.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface SPDPlanDateStudentResponse extends IsIdentify, HasOrderNumber {

    String getCode();

    String getName();

    Integer getStatus();

}
