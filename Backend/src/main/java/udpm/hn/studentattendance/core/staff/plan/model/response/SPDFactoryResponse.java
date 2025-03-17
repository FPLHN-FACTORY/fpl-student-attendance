package udpm.hn.studentattendance.core.staff.plan.model.response;

import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface SPDFactoryResponse extends IsIdentify {

    String getFactoryName();

    String getStaffName();

}
