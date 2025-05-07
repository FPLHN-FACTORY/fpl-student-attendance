package udpm.hn.studentattendance.core.admin.subject_facility.model.response;

import udpm.hn.studentattendance.infrastructure.common.HasOrderNumber;
import udpm.hn.studentattendance.infrastructure.common.IsIdentify;

public interface ADSubjectFacilityResponse extends IsIdentify, HasOrderNumber {

    String getSubjectId();

    String getSubjectName();

    String getFacilityName();

    String getFacilityId();

    String getStatus();

    Long getCreatedAt();

    Long getUpdatedAt();

}
