package udpm.hn.studentattendance.core.admin.levelproject.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.levelproject.model.request.ADLevelProjectCreateRequest;
import udpm.hn.studentattendance.core.admin.levelproject.model.request.ADLevelProjectUpdateRequest;
import udpm.hn.studentattendance.core.admin.levelproject.model.request.ADLevelProjectSearchRequest;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;

public interface ADLevelProjectManagementService {

    ResponseEntity<?> getListLevelProject(ADLevelProjectSearchRequest request);

    ResponseEntity<?> createLevelProject(ADLevelProjectCreateRequest request);

    ResponseEntity<?> updateLevelProject(String id, ADLevelProjectUpdateRequest request);

    ResponseEntity<?> detailLevelProject(String id);

    ResponseEntity<?> changeStatus(String id);

}
