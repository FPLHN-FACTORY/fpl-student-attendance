package udpm.hn.studentattendance.core.admin.subject.service;

import udpm.hn.studentattendance.core.admin.subject.model.request.AdminSubjectCreateRequest;
import udpm.hn.studentattendance.core.admin.subject.model.request.AdminSubjectSearchRequest;
import udpm.hn.studentattendance.core.admin.subject.model.request.AdminSubjectUpdateRequest;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;

public interface AdminSubjectManagementService {

    ResponseObject<?> getListSubject(AdminSubjectSearchRequest request);

    ResponseObject<?> createSubject(AdminSubjectCreateRequest request);

    ResponseObject<?> updateSubject(String id, AdminSubjectUpdateRequest request);

    ResponseObject<?> detailSubject(String id);

    ResponseObject<?> deleteSubject(String id);

}
