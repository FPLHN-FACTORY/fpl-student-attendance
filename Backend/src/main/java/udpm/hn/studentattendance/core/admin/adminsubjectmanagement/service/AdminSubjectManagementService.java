package udpm.hn.studentattendance.core.admin.adminsubjectmanagement.service;

import udpm.hn.studentattendance.core.admin.adminsubjectmanagement.model.request.AdminSubjectCreateRequest;
import udpm.hn.studentattendance.core.admin.adminsubjectmanagement.model.request.AdminSubjectSearchRequest;
import udpm.hn.studentattendance.core.admin.adminsubjectmanagement.model.request.AdminSubjectUpdateRequest;
import udpm.hn.studentattendance.core.staff.levelprojectmanagement.model.request.LevelProjectUpdateRequest;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;

public interface AdminSubjectManagementService {

    ResponseObject<?> getListSubject(AdminSubjectSearchRequest request);

    ResponseObject<?> createSubject(AdminSubjectCreateRequest request);

    ResponseObject<?> updateSubject(String id, AdminSubjectUpdateRequest request);

    ResponseObject<?> detailSubject(String id);

    ResponseObject<?> deleteSubject(String id);

}
