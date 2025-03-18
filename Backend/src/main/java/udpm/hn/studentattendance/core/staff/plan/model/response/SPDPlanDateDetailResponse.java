package udpm.hn.studentattendance.core.staff.plan.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface SPDPlanDateDetailResponse extends IsIdentify, HasOrderNumber {

    Long getStartDate();

    String getShift();

    String getDescription();

    Integer getLateArrival();

    String getStatus();

    Long getFromDate();

    Long getToDate();

}
