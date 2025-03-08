package udpm.hn.studentattendance.core.admin.levelproject.service;

import udpm.hn.studentattendance.core.admin.levelproject.model.request.LevelProjectCreateRequest;
import udpm.hn.studentattendance.core.admin.levelproject.model.request.LevelProjectUpdateRequest;
import udpm.hn.studentattendance.core.admin.levelproject.model.request.AdminLevelProjectSearchRequest;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;

public interface AdminLevelProjectManagementService {

    ResponseObject<?> getListLevelProject(AdminLevelProjectSearchRequest request);

    ResponseObject<?> createLevelProject(LevelProjectCreateRequest request);

    ResponseObject<?> updateLevelProject(String id, LevelProjectUpdateRequest request);

    ResponseObject<?> detailLevelProject(String id);

    ResponseObject<?> deleteLevelProject(String id);

}
