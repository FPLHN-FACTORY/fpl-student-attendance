package udpm.hn.studentattendance.core.admin.subject.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.subject.model.request.ADSubjectCreateRequest;
import udpm.hn.studentattendance.core.admin.subject.model.request.ADSubjectSearchRequest;
import udpm.hn.studentattendance.core.admin.subject.model.request.ADSubjectUpdateRequest;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;

public interface ADSubjectManagementService {

    ResponseEntity<?> getListSubject(ADSubjectSearchRequest request);

    ResponseEntity<?> createSubject(ADSubjectCreateRequest request);

    ResponseEntity<?> updateSubject(String id, ADSubjectUpdateRequest request);

    ResponseEntity<?> detailSubject(String id);

    ResponseEntity<?> changeStatus(String id);

}
