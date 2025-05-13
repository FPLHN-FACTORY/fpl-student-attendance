package udpm.hn.studentattendance.core.admin.subject.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface ADSubjectResponse extends IsIdentify, HasOrderNumber {

    String getName();

    String getCode();

    Integer getStatus();

    String getSizeSubjectSemester();

}
