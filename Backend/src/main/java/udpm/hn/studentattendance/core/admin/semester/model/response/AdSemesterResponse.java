package udpm.hn.studentattendance.core.admin.semester.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface AdSemesterResponse extends IsIdentify, HasOrderNumber {

    Integer getSemesterIndex();

    String getSemesterCode();

    String getSemesterName();

    Integer getSemesterStatus();

    Long getStartDate();

    Long getEndDate();
}
