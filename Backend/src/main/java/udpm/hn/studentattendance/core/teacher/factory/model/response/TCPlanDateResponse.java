package udpm.hn.studentattendance.core.teacher.factory.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface TCPlanDateResponse extends IsIdentify, HasOrderNumber {

    Long getStartDate();

    Long getEndDate();

    String getShift();

    String getDescription();

    Integer getLateArrival();

    String getLink();

    String getStatus();

    Integer getType();

    String getRoom();

    Long getFromDate();

    Long getToDate();

    Integer getRequiredIp();

    Integer getRequiredLocation();

    Integer getRequiredCheckin();

    Integer getRequiredCheckout();

}
