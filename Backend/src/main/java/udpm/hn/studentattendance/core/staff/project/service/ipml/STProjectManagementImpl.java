package udpm.hn.studentattendance.core.staff.project.service.ipml;

import jakarta.mail.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.staff.project.model.request.USProjectCreateRequest;
import udpm.hn.studentattendance.core.staff.project.model.request.USProjectUpdateRequest;
import udpm.hn.studentattendance.core.staff.project.model.request.USProjectSearchRequest;
import udpm.hn.studentattendance.core.staff.project.repository.STLevelProjectExtendRepository;
import udpm.hn.studentattendance.core.staff.project.repository.STProjectExtendRepository;
import udpm.hn.studentattendance.core.staff.project.repository.STProjectSemesterExtendRepository;
import udpm.hn.studentattendance.core.staff.project.repository.STProjectSubjectFacilityExtendRepository;
import udpm.hn.studentattendance.core.staff.project.service.STProjectManagementService;
import udpm.hn.studentattendance.entities.Project;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

@Service
@RequiredArgsConstructor
@Validated
public class STProjectManagementImpl implements STProjectManagementService {


    private final STProjectExtendRepository projectManagementRepository;

    private final STLevelProjectExtendRepository levelProjectRepository;

    private final STProjectSemesterExtendRepository semesterRepository;

    private final STProjectSubjectFacilityExtendRepository subjectFacilityRepository;

    private final SessionHelper sessionHelper;

    @Override
    public ResponseEntity<?> getListProject(USProjectSearchRequest request) {
        Pageable pageable = PaginationHelper.createPageable(request, "createdAt");
        PageableObject list = PageableObject.of(projectManagementRepository.getListProject(pageable, request));
        return RouterHelper.responseSuccess("Lấy danh sách dự án thành công", list);

    }

    @Override
    public ResponseEntity<?> createProject(USProjectCreateRequest request) {
        Project project = new Project();

        project = convertProjectRequestToProject(request, project);
        boolean isExistProject =
                projectManagementRepository
                        .isExistProjectInSameLevel(project.getName(), project.getLevelProject().getId(),
                        project.getSemester().getId(), sessionHelper.getFacilityId());
        if (isExistProject){
            return RouterHelper.responseError("Dự án này đã tồn tại ở dự án " + project.getLevelProject().getName());
        }
        projectManagementRepository.save(project);
        return RouterHelper.responseSuccess( "Thêm dự án thành công", project);
    }

    @Override
    public ResponseEntity<?> updateProject(String idProject, USProjectUpdateRequest request) {
        Project project = projectManagementRepository.findById(idProject).get();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setLevelProject(levelProjectRepository.findById(request.getIdLevelProject()).get());
        project.setSemester(semesterRepository.findById(request.getIdSemester()).get());
        project.setSubjectFacility(subjectFacilityRepository.findById(request.getIdSubjectFacility()).get());
        projectManagementRepository.save(project);
        return RouterHelper.responseSuccess( "Chuyển trạng thái thành công", project);
    }

    @Override
    public ResponseEntity<?> detailProject(String idProject) {
        return projectManagementRepository.getDetailProject(idProject)
                .map(project -> RouterHelper.responseSuccess( "Detail thành công!",project))
                .orElseGet(() -> RouterHelper.responseSuccess( "Không tìm thấy dự án",null));
    }

    @Override
    public ResponseEntity<?> changeStatus(String idProject) {
        Project project = projectManagementRepository.findById(idProject).get();
        if (project.getStatus() == EntityStatus.ACTIVE) {
            project.setStatus(EntityStatus.INACTIVE);
        } else {
            project.setStatus(EntityStatus.ACTIVE);
        }
        projectManagementRepository.save(project);
        return RouterHelper.responseSuccess( "Chuyển trạng thái thành công", project);
    }

    private Project convertProjectRequestToProject(USProjectCreateRequest request, Project project) {

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setLevelProject(levelProjectRepository.findById(request.getLevelProjectId()).get());
        project.setSemester(semesterRepository.findById(request.getSemesterId()).get());
        project.setSubjectFacility(subjectFacilityRepository.findById(request.getSubjectFacilityId()).get());
        project.setStatus(EntityStatus.ACTIVE);
        return project;
    }

}
