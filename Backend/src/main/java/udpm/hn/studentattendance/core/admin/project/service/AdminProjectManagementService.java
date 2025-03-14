package udpm.hn.studentattendance.core.admin.project.service;

import udpm.hn.studentattendance.core.admin.project.model.request.ProjectCreateRequest;
import udpm.hn.studentattendance.core.admin.project.model.request.ProjectUpdateRequest;
import udpm.hn.studentattendance.core.admin.project.model.request.AdminProjectSearchRequest;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;

public interface AdminProjectManagementService {

    ResponseObject<?> getListProject(AdminProjectSearchRequest request);

    ResponseObject<?> createProject(ProjectCreateRequest request);

    ResponseObject<?> updateProject(String id, ProjectUpdateRequest request);

    ResponseObject<?> detailProject(String id);

    ResponseObject<?> deleteProject(String id);

}
