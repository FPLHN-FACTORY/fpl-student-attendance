package udpm.hn.studentattendance.core.admin.subject_facility.service;

import udpm.hn.studentattendance.core.admin.subject_facility.model.request.ADSubjectFacilityCreateRequest;
import udpm.hn.studentattendance.core.admin.subject_facility.model.request.ADSubjectFacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.subject_facility.model.request.ADSubjectFacilityUpdateRequest;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;

public interface Admin_SubjectFacilityService {

    ResponseObject<?> getListSubjectFacility(ADSubjectFacilitySearchRequest request);

    ResponseObject<?> createSubjectFacility(ADSubjectFacilityCreateRequest request);

    ResponseObject<?> updateSubjectFacility(String id, ADSubjectFacilityUpdateRequest request);

    ResponseObject<?> detailSubjectFacility(String id);

    ResponseObject<?> changeStatus(String id);

}
