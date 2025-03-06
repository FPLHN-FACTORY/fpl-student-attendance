package udpm.hn.studentattendance.core.admin.adminprojectmanagement.service;

import udpm.hn.studentattendance.core.admin.adminprojectmanagement.model.request.ProjectCreateRequest;
import udpm.hn.studentattendance.core.admin.adminprojectmanagement.model.request.ProjectUpdateRequest;
import udpm.hn.studentattendance.core.admin.adminprojectmanagement.model.request.StaffProjectSearchRequest;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;

public interface StaffProjectManagementService {

    ResponseObject<?> getListProject(StaffProjectSearchRequest request);

    ResponseObject<?> createProject(ProjectCreateRequest request);

    ResponseObject<?> updateProject(String id, ProjectUpdateRequest request);

    ResponseObject<?> detailProject(String id);

    ResponseObject<?> deleteProject(String id);

}
