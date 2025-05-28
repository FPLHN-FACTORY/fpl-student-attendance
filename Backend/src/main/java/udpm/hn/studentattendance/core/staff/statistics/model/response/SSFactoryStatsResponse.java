package udpm.hn.studentattendance.core.staff.statistics.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface SSFactoryStatsResponse extends IsIdentify, HasOrderNumber {

    String getProjectId();

    String getProjectName();

    String getFactoryName();

    String getLevelProject();

    String getSubjectName();

    Integer getTotalShift();

    Integer getTotalCurrentShift();

    Integer getTotalStudent();

    Integer getStatus();

}
