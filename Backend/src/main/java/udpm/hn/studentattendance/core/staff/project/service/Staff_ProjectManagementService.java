package udpm.hn.studentattendance.core.staff.project.service;

import udpm.hn.studentattendance.core.staff.project.model.request.Staff_ProjectCreateRequest;
import udpm.hn.studentattendance.core.staff.project.model.request.Staff_ProjectUpdateRequest;
import udpm.hn.studentattendance.core.staff.project.model.request.Staff_ProjectSearchRequest;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;

public interface Staff_ProjectManagementService {

    ResponseObject<?> getListProject(Staff_ProjectSearchRequest request);

    ResponseObject<?> createProject(Staff_ProjectCreateRequest request);

    ResponseObject<?> updateProject(String id, Staff_ProjectUpdateRequest request);

    ResponseObject<?> detailProject(String id);

    ResponseObject<?> changeStatus(String id);

}
