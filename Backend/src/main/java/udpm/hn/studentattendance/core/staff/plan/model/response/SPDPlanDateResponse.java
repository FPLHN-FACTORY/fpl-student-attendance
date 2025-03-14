package udpm.hn.studentattendance.core.staff.plan.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface SPDPlanDateResponse extends IsIdentify, HasOrderNumber {

    String getFactoryName();

    String getProjectName();

    String getLevel();

    String getSemesterName();

    String getSubjectName();

    String getStaffName();

    Integer getTotalShift();

    Long getFromDate();

    Long getToDate();
}
