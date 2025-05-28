package udpm.hn.studentattendance.core.admin.facility.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface AFFacilityLocationResponse extends IsIdentify, HasOrderNumber {

    String getName();

    Integer getStatus();

    Double getLatitude();

    Double getLongitude();

    Integer getRadius();

}
