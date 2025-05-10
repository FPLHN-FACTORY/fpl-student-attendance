package udpm.hn.studentattendance.core.staff.project.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.project.model.request.USProjectCreateOrUpdateRequest;
import udpm.hn.studentattendance.core.staff.project.model.request.USProjectSearchRequest;

public interface STProjectManagementService {

    ResponseEntity<?> getListProject(USProjectSearchRequest request);

    ResponseEntity<?> createProject(USProjectCreateOrUpdateRequest request);

    ResponseEntity<?> updateProject(String id, USProjectCreateOrUpdateRequest request);

    ResponseEntity<?> detailProject(String id);

    ResponseEntity<?> changeStatus(String id);

    ResponseEntity<?> changeAllStatusPreviousSemester();

}
