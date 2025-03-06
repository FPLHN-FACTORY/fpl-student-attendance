package udpm.hn.studentattendance.core.staff.projectmanagement.service;

import udpm.hn.studentattendance.core.staff.projectmanagement.model.request.ProjectCreateRequest;
import udpm.hn.studentattendance.core.staff.projectmanagement.model.request.ProjectUpdateRequest;
import udpm.hn.studentattendance.core.staff.projectmanagement.model.request.StaffProjectSearchRequest;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;

public interface StaffProjectManagementService {

    ResponseObject<?> getListProject(StaffProjectSearchRequest request);

    ResponseObject<?> createProject(ProjectCreateRequest request);

    ResponseObject<?> updateProject(String id, ProjectUpdateRequest request);

    ResponseObject<?> detailProject(String id);

    ResponseObject<?> deleteProject(String id);

}
