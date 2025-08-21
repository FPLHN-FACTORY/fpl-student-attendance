package udpm.hn.studentattendance.core.staff.plan.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface SPDPlanDateAttendanceResponse extends IsIdentify, HasOrderNumber {

    String getPlanId();

    String getPlanName();

    String getFactoryId();

    String getFactoryName();

    Integer getRequiredCheckin();

    Integer getRequiredCheckout();

    Integer getStatus();

}
