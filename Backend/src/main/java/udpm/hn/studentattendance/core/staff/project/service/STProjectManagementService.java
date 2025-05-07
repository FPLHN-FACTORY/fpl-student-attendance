package udpm.hn.studentattendance.core.staff.project.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.project.model.request.USProjectCreateRequest;
import udpm.hn.studentattendance.core.staff.project.model.request.USProjectUpdateRequest;
import udpm.hn.studentattendance.core.staff.project.model.request.USProjectSearchRequest;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;

public interface STProjectManagementService {

    ResponseEntity<?> getListProject(USProjectSearchRequest request);

    ResponseEntity<?> createProject(USProjectCreateRequest request);

    ResponseEntity<?> updateProject(String id, USProjectUpdateRequest request);

    ResponseEntity<?> detailProject(String id);

    ResponseEntity<?> changeStatus(String id);

}
