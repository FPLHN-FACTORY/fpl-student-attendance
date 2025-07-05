package udpm.hn.studentattendance.core.staff.plan.model.response;

import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface SPDUserStudentResponse extends IsIdentify {

    String getCode();

    String getName();

    String getEmail();

    String getFactoryName();

    String getPlanName();

}
