package udpm.hn.studentattendance.core.staff.plan.model.response;

import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface SPDProjectResponse extends IsIdentify {

    String getName();

    Long getFromDate();

    Long getToDate();

}
