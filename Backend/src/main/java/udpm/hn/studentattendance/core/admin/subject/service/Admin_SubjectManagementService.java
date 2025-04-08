package udpm.hn.studentattendance.core.admin.subject.service;

import udpm.hn.studentattendance.core.admin.subject.model.request.Admin_SubjectCreateRequest;
import udpm.hn.studentattendance.core.admin.subject.model.request.Admin_SubjectSearchRequest;
import udpm.hn.studentattendance.core.admin.subject.model.request.Admin_SubjectUpdateRequest;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;

public interface Admin_SubjectManagementService {

    ResponseObject<?> getListSubject(Admin_SubjectSearchRequest request);

    ResponseObject<?> createSubject(Admin_SubjectCreateRequest request);

    ResponseObject<?> updateSubject(String id, Admin_SubjectUpdateRequest request);

    ResponseObject<?> detailSubject(String id);

    ResponseObject<?> changeStatus(String id);

}
