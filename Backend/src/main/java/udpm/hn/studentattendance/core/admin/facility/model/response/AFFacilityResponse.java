package udpm.hn.studentattendance.core.admin.facility.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface AFFacilityResponse extends IsIdentify, HasOrderNumber {
    Integer getFacilityIndex();

    String getFacilityName();

    String getFacilityCode();

    Integer getFacilityStatus();

    Long getCreatedDate();

    Integer getMaxPosition();
}
