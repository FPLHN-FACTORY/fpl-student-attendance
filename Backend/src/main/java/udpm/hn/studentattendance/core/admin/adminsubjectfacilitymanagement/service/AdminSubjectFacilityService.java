package udpm.hn.studentattendance.core.admin.adminsubjectfacilitymanagement.service;

import udpm.hn.studentattendance.core.admin.adminsubjectfacilitymanagement.model.request.AdminSubjectFacilityCreateRequest;
import udpm.hn.studentattendance.core.admin.adminsubjectfacilitymanagement.model.request.AdminSubjectFacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.adminsubjectfacilitymanagement.model.request.AdminSubjectFacilityUpdateRequest;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;

public interface AdminSubjectFacilityService {

    ResponseObject<?> getListSubjectFacility(AdminSubjectFacilitySearchRequest request);

    ResponseObject<?> createSubjectFacility(AdminSubjectFacilityCreateRequest request);

    ResponseObject<?> updateSubjectFacility(String id, AdminSubjectFacilityUpdateRequest request);

    ResponseObject<?> detailSubjectFacility(String id);

    ResponseObject<?> deleteSubjectFacility(String id);

}
