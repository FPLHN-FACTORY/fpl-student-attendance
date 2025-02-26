package udpm.hn.studentattendance.core.staff.levelprojectmanagement.service;

import udpm.hn.studentattendance.core.staff.levelprojectmanagement.model.request.LevelProjectCreateRequest;
import udpm.hn.studentattendance.core.staff.levelprojectmanagement.model.request.StaffLevelProjectSearchRequest;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;

public interface StaffLevelProjectManagementService {

    ResponseObject<?> getListLevelProject(StaffLevelProjectSearchRequest request);

    ResponseObject<?> createLevelProject(LevelProjectCreateRequest request);

    ResponseObject<?> updateLevelProject(String id,LevelProjectCreateRequest request);

    ResponseObject<?> detailLevelProject(String id);

    ResponseObject<?> deleteLevelProject(String id);

}
