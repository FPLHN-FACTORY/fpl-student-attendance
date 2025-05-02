package udpm.hn.studentattendance.core.admin.subject.service;

import udpm.hn.studentattendance.core.admin.subject.model.request.ADSubjectCreateRequest;
import udpm.hn.studentattendance.core.admin.subject.model.request.ADSubjectSearchRequest;
import udpm.hn.studentattendance.core.admin.subject.model.request.ADSubjectUpdateRequest;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;

public interface ADSubjectManagementService {

    ResponseObject<?> getListSubject(ADSubjectSearchRequest request);

    ResponseObject<?> createSubject(ADSubjectCreateRequest request);

    ResponseObject<?> updateSubject(String id, ADSubjectUpdateRequest request);

    ResponseObject<?> detailSubject(String id);

    ResponseObject<?> changeStatus(String id);

}
