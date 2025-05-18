package udpm.hn.studentattendance.core.student.historyattendance.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface STDHistoryPlanDateAttendanceResponse extends IsIdentify, HasOrderNumber {

    String getPlanId();

    String getPlanName();

    String getFactoryId();

    String getFactoryName();

    Integer getRequiredCheckin();

    Integer getRequiredCheckout();
}
