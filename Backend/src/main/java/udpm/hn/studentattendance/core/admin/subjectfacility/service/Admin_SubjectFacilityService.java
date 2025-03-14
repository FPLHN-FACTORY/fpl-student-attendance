package udpm.hn.studentattendance.core.admin.subjectfacility.service;

import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.Admin_SubjectFacilityCreateRequest;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.Admin_SubjectFacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.Admin_SubjectFacilityUpdateRequest;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;

public interface Admin_SubjectFacilityService {

    ResponseObject<?> getListSubjectFacility(Admin_SubjectFacilitySearchRequest request);

    ResponseObject<?> createSubjectFacility(Admin_SubjectFacilityCreateRequest request);

    ResponseObject<?> updateSubjectFacility(String id, Admin_SubjectFacilityUpdateRequest request);

    ResponseObject<?> detailSubjectFacility(String id);

    ResponseObject<?> deleteSubjectFacility(String id);

}
