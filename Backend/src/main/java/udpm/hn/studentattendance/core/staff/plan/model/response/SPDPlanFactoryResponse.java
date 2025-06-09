package udpm.hn.studentattendance.core.staff.plan.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface SPDPlanFactoryResponse extends IsIdentify, HasOrderNumber {

    String getPlanId();

    String getPlanName();

    String getFactoryName();

    String getStaffName();

    Integer getTotalShift();

    Integer getTotalCurrentShift();

    Integer getTotalStudent();

    Long getFromDate();

    Long getToDate();

    Integer getStatus();

}
