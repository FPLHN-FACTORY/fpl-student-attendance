package udpm.hn.studentattendance.core.staff.plan.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface SPDPlanResponse extends IsIdentify, HasOrderNumber {

    String getPlanName();

    String getProjectName();

    String getLevel();

    String getSemesterName();

    String getSubjectName();

    Long getFromDate();

    Long getToDate();

    Integer getStatus();

}
