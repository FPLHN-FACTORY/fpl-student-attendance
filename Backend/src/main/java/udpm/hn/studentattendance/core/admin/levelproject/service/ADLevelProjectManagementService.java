package udpm.hn.studentattendance.core.admin.levelproject.service;

import udpm.hn.studentattendance.core.admin.levelproject.model.request.ADLevelProjectCreateRequest;
import udpm.hn.studentattendance.core.admin.levelproject.model.request.ADLevelProjectUpdateRequest;
import udpm.hn.studentattendance.core.admin.levelproject.model.request.ADLevelProjectSearchRequest;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;

public interface ADLevelProjectManagementService {

    ResponseObject<?> getListLevelProject(ADLevelProjectSearchRequest request);

    ResponseObject<?> createLevelProject(ADLevelProjectCreateRequest request);

    ResponseObject<?> updateLevelProject(String id, ADLevelProjectUpdateRequest request);

    ResponseObject<?> detailLevelProject(String id);

    ResponseObject<?> changeStatus(String id);

}
