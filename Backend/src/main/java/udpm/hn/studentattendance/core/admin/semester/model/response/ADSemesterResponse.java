package udpm.hn.studentattendance.core.admin.semester.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface ADSemesterResponse extends IsIdentify, HasOrderNumber {

    Integer getSemesterIndex();

    String getSemesterCode();

    String getSemesterName();

    Integer getSemesterStatus();

    Long getStartDate();

    Long getEndDate();
}
