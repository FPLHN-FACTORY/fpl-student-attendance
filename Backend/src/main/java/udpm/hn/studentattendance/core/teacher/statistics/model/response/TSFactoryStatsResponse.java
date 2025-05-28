package udpm.hn.studentattendance.core.teacher.statistics.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface TSFactoryStatsResponse extends IsIdentify, HasOrderNumber {

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
