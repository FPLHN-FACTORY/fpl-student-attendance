package udpm.hn.studentattendance.core.staff.project.service.ipml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.staff.project.model.request.Staff_ProjectCreateRequest;
import udpm.hn.studentattendance.core.staff.project.model.request.Staff_ProjectUpdateRequest;
import udpm.hn.studentattendance.core.staff.project.model.request.Staff_ProjectSearchRequest;
import udpm.hn.studentattendance.core.staff.project.repository.Staff_ProjectManagementRepository;
import udpm.hn.studentattendance.core.staff.project.service.Staff_ProjectManagementService;
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
public class Staff_ProjectManagementImpl implements Staff_ProjectManagementService {

    @Autowired
    private Staff_ProjectManagementRepository staffProjectManagementRepository;

    @Autowired
    private LevelProjectRepository levelProjectRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private UserStaffRepository userStaffRepository;

    @Autowired
    private SubjectFacilityRepository subjectFacilityRepository;

    @Override
    public ResponseObject<?> getListProject(Staff_ProjectSearchRequest request) {
        Pageable pageable = PaginationHelper.createPageable(request, "id");
        return new ResponseObject<>(
                PageableObject.of(staffProjectManagementRepository.getListProject(pageable, request)),
                HttpStatus.OK,
                "Lây danh sách dự án thành công"
        );
    }

    @Override
    public ResponseObject<?> createProject(Staff_ProjectCreateRequest request) {
        Project project = new Project();
        project = convertProjectRequestToProject(request, project);
        staffProjectManagementRepository.save(project);
        return new ResponseObject<>(project, HttpStatus.OK, "Thêm dự án thành công");
    }

    @Override
    public ResponseObject<?> updateProject(String idProject, Staff_ProjectUpdateRequest request) {
        Project project = staffProjectManagementRepository.findById(idProject).get();
        if (project.getStatus() == EntityStatus.ACTIVE) {
            project.setStatus(EntityStatus.INACTIVE);
        } else {
            project.setStatus(EntityStatus.ACTIVE);
        }
        staffProjectManagementRepository.save(project);
        return new ResponseObject<>(project, HttpStatus.OK, "Chuyển trạng thái thành công");
    }

    @Override
    public ResponseObject<?> detailProject(String idProject) {
        return staffProjectManagementRepository.getDetailProject(idProject)
                .map(project -> new ResponseObject<>(project, HttpStatus.OK, "Detail thành công!"))
                .orElseGet(() -> new ResponseObject<>(null, HttpStatus.CONFLICT, "Không tìm thấy dự án!"));
    }

    @Override
    public ResponseObject<?> deleteProject(String idProject) {
        Project project = staffProjectManagementRepository.findById(idProject).get();
        if (project.getStatus() == EntityStatus.ACTIVE) {
            project.setStatus(EntityStatus.INACTIVE);
        } else {
            project.setStatus(EntityStatus.ACTIVE);
        }
        staffProjectManagementRepository.save(project);
        return new ResponseObject<>(project, HttpStatus.OK, "Chuyển trạng thái thành công");
    }

    private Project convertProjectRequestToProject(Staff_ProjectCreateRequest request, Project project) {
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setLevelProject(levelProjectRepository.findById(request.getLevelProjectId()).get());
        project.setSemester(semesterRepository.findById(request.getSemesterId()).get());
        project.setSubjectFacility(subjectFacilityRepository.findById(request.getSubjectFacilityId()).get());
        project.setStatus(EntityStatus.ACTIVE);
        return project;
    }

    private Project convertProjectUpdateRequestToProject(Staff_ProjectUpdateRequest request, Project project) {
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setLevelProject(levelProjectRepository.findById(request.getIdLevelProject()).get());
        project.setSemester(semesterRepository.findById(request.getIdSemester()).get());
        project.setSubjectFacility(subjectFacilityRepository.findById(request.getIdSubject()).get());
//        if (request.getStatus().equals("ACTIVE")) {
//            project.setStatus(EntityStatus.ACTIVE);
//        } else {
//            project.setStatus(EntityStatus.INACTIVE);
//        }
        return project;
    }
}
