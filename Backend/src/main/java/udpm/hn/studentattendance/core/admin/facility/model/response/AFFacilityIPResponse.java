package udpm.hn.studentattendance.core.admin.facility.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface AFFacilityIPResponse extends IsIdentify, HasOrderNumber {

    String getIP();

    Integer getStatus();

    Integer getType();

}
