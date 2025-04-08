package udpm.hn.studentattendance.core.admin.levelproject.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.levelproject.model.request.Admin_LevelProjectCreateRequest;
import udpm.hn.studentattendance.core.admin.levelproject.model.request.Admin_LevelProjectUpdateRequest;
import udpm.hn.studentattendance.core.admin.levelproject.model.request.Admin_LevelProjectSearchRequest;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;

public interface Admin_LevelProjectManagementService {

    ResponseObject<?> getListLevelProject(Admin_LevelProjectSearchRequest request);

    ResponseObject<?> createLevelProject(Admin_LevelProjectCreateRequest request);

    ResponseObject<?> updateLevelProject(String id, Admin_LevelProjectUpdateRequest request);

    ResponseObject<?> detailLevelProject(String id);

    ResponseObject<?> changeStatus(String id);

}
