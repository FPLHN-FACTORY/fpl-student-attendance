package udpm.hn.studentattendance.core.teacher.statistics.model.response;

import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface TSUserResponse extends IsIdentify {

    String getCode();

    String getName();

    String getEmail();

}
