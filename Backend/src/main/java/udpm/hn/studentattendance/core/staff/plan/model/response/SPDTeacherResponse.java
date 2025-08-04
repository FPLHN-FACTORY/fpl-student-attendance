package udpm.hn.studentattendance.core.staff.plan.model.response;

import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface SPDTeacherResponse extends IsIdentify {

    String getCode();

    String getName();

    String getEmail();

}
