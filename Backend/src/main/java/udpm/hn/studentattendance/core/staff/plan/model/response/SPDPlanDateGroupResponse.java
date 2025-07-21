package udpm.hn.studentattendance.core.staff.plan.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;

public interface SPDPlanDateGroupResponse extends HasOrderNumber {

    String getDay();

    Long getStartDate();

    Integer getTotalShift();

    String getStatus();

}
