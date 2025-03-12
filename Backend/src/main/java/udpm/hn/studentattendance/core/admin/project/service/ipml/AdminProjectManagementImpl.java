package udpm.hn.studentattendance.core.admin.project.service.ipml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.admin.project.model.request.ProjectCreateRequest;
import udpm.hn.studentattendance.core.admin.project.model.request.ProjectUpdateRequest;
import udpm.hn.studentattendance.core.admin.project.model.request.AdminProjectSearchRequest;
import udpm.hn.studentattendance.core.admin.project.repository.AdminProjectManagementRepository;
import udpm.hn.studentattendance.core.admin.project.service.AdminProjectManagementService;
import udpm.hn.studentattendance.entities.Project;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.ResponseObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.LevelProjectRepository;
import udpm.hn.studentattendance.repositories.SemesterRepository;
import udpm.hn.studentattendance.repositories.SubjectFacilityRepository;
import udpm.hn.studentattendance.repositories.UserStaffRepository;

@Service
public class AdminProjectManagementImpl implements AdminProjectManagementService {

    @Autowired
    private AdminProjectManagementRepository repository;

    @Autowired
    private LevelProjectRepository levelProjectRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private UserStaffRepository userStaffRepository;

    @Autowired
    private SubjectFacilityRepository subjectFacilityRepository;

    @Override
    public ResponseObject<?> getListProject(AdminProjectSearchRequest request) {
        Pageable pageable = PaginationHelper.createPageable(request, "id");
        return new ResponseObject<>(
                PageableObject.of(repository.getListProject(pageable, request)),
                HttpStatus.OK,
                "Lây danh sách dự án thành công"
        );
    }

    @Override
    public ResponseObject<?> createProject(ProjectCreateRequest request) {
        Project project = new Project();
        project = convertProjectRequestToProject(request, project);
        repository.save(project);
        return new ResponseObject<>(project, HttpStatus.OK, "Thêm dự án thành công");
    }

    @Override
    public ResponseObject<?> updateProject(String idProject, ProjectUpdateRequest request) {
        Project project = repository.findById(idProject).get();
        project = convertProjectUpdateRequestToProject(request, project);
        repository.save(project);
        return new ResponseObject<>(project, HttpStatus.OK, "Sửa dự án thành công");
    }

    @Override
    public ResponseObject<?> detailProject(String idProject) {
        return repository.getDetailProject(idProject)
                .map(project -> new ResponseObject<>(project, HttpStatus.OK, "Detail thành công!"))
                .orElseGet(() -> new ResponseObject<>(null, HttpStatus.CONFLICT, "Không tìm thấy dự án!"));
    }

    @Override
    public ResponseObject<?> deleteProject(String idProject) {
        repository.deleteById(idProject);
        return new ResponseObject<>(null, HttpStatus.OK, "Xóa dự án thành công");
    }

    private Project convertProjectRequestToProject(ProjectCreateRequest request, Project project) {
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setLevelProject(levelProjectRepository.findById(request.getLevelProjectId()).get());
        project.setSemester(semesterRepository.findById(request.getSemesterId()).get());
        project.setSubjectFacility(repository.getSubjectFacilityById(request.getSubjectId(), request.getFacilityId()));
        project.setStatus(EntityStatus.ACTIVE);
        return project;
    }

    private Project convertProjectUpdateRequestToProject(ProjectUpdateRequest request, Project project) {
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setLevelProject(levelProjectRepository.findById(request.getIdLevelProject()).get());
        project.setSemester(semesterRepository.findById(request.getIdSemester()).get());
        project.setSubjectFacility(subjectFacilityRepository.findById(request.getIdSubject()).get());
        if (request.getStatus().equals("ACTIVE")) {
            project.setStatus(EntityStatus.ACTIVE);
        } else {
            project.setStatus(EntityStatus.INACTIVE);
        }
        return project;
    }
}
