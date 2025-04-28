package udpm.hn.studentattendance.core.staff.project.service;

import udpm.hn.studentattendance.core.staff.project.model.request.USProjectCreateRequest;
import udpm.hn.studentattendance.core.staff.project.model.request.USProjectUpdateRequest;
import udpm.hn.studentattendance.core.staff.project.model.request.USProjectSearchRequest;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;

public interface Staff_ProjectManagementService {

    ResponseObject<?> getListProject(USProjectSearchRequest request);

    ResponseObject<?> createProject(USProjectCreateRequest request);

    ResponseObject<?> updateProject(String id, USProjectUpdateRequest request);

    ResponseObject<?> detailProject(String id);

    ResponseObject<?> changeStatus(String id);

}
