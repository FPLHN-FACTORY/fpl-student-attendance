package udpm.hn.studentattendance.core.admin.facility.model.respone;

import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface AdFacilityRespone extends IsIdentify, HasOrderNumber {

    String getFacilityName();

    String getFacilityCode();

    Integer getFacilityStatus();

    Long getCreatedDate();

}
