package udpm.hn.studentattendance.core.teacher.factory.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface TCPlanDateAttendanceResponse extends IsIdentify, HasOrderNumber {

    String getPlanId();

    String getPlanName();

    String getFactoryId();

    String getFactoryName();

    Integer getRequiredCheckin();

    Integer getRequiredCheckout();

}
