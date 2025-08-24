package udpm.hn.studentattendance.core.staff.statistics.model.response;

import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface SSUserResponse extends IsIdentify {

    String getCode();

    String getName();

    String getEmail();

}
