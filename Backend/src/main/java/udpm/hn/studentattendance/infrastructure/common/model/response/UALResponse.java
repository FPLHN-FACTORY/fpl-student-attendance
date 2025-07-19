package udpm.hn.studentattendance.infrastructure.common.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasAudit;
import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;

public interface UALResponse extends IsIdentify, HasAudit {

    String getUserName();

    String getFacilityName();

    String getUserCode();

    String getMessage();

    Integer getRole();


}
