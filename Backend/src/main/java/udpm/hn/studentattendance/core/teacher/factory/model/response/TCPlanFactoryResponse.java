package udpm.hn.studentattendance.core.teacher.factory.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface TCPlanFactoryResponse extends IsIdentify, HasOrderNumber {

    String getPlanId();

    String getPlanName();

    String getFactoryName();

    String getStaffName();

    Integer getTotalShift();

    Integer getTotalStudent();

    Long getFromDate();

    Long getToDate();

    Integer getStatus();

}
