package udpm.hn.studentattendance.core.staff.project.service.ipml;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.staff.project.model.request.USProjectCreateOrUpdateRequest;
import udpm.hn.studentattendance.core.staff.project.model.request.USProjectSearchRequest;
import udpm.hn.studentattendance.core.staff.project.model.response.USProjectResponse;
import udpm.hn.studentattendance.core.staff.project.repository.*;
import udpm.hn.studentattendance.core.staff.project.service.STProjectManagementService;
import udpm.hn.studentattendance.entities.*;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonUserStudentRepository;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class STProjectManagementImpl implements STProjectManagementService {

    private final STProjectExtendRepository projectManagementRepository;

    private final STLevelProjectExtendRepository levelProjectRepository;

    private final STProjectSemesterExtendRepository semesterRepository;

    private final STProjectSubjectFacilityExtendRepository subjectFacilityRepository;

    private final CommonUserStudentRepository commonUserStudentRepository;

    private final SessionHelper sessionHelper;

    private final STProjectChangeSemesterExtendRepository deleteBulkByProject;

    private final UserActivityLogHelper userActivityLogHelper;

    @Override
    public ResponseEntity<?> getListProject(USProjectSearchRequest request) {
        request.setFacilityId(sessionHelper.getFacilityId());
        Pageable pageable = PaginationHelper.createPageable(request, "createdAt");
        PageableObject<USProjectResponse> list = PageableObject
                .of(projectManagementRepository.getListProject(pageable, request));
        return RouterHelper.responseSuccess("Lấy danh sách dự án thành công", list);

    }

    @Override
    public ResponseEntity<?> createProject(USProjectCreateOrUpdateRequest request) {

        LevelProject levelProject = levelProjectRepository.findById(request.getLevelProjectId()).orElse(null);
        if (levelProject == null) {
            return RouterHelper.responseError("Không tìm thấy cấp độ dự án");
        }

        Semester semester = semesterRepository.findById(request.getSemesterId()).orElse(null);
        if (semester == null) {
            return RouterHelper.responseError("Không tìm thấy học kỳ");
        }

        Long now = new Date().getTime();
        if (semester.getToDate() < now) {
            return RouterHelper.responseError("Vui lòng chọn học kỳ đang hoặc chưa diễn ra cho dự án mới");
        }

        SubjectFacility subjectFacility = subjectFacilityRepository.findById(request.getSubjectFacilityId())
                .orElse(null);
        if (subjectFacility == null) {
            return RouterHelper.responseError("Không tìm thấy bộ môn");
        }

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setLevelProject(levelProject);
        project.setSemester(semester);
        project.setSubjectFacility(subjectFacility);
        project.setStatus(EntityStatus.ACTIVE);

        boolean isExistProject = projectManagementRepository
                .isExistProjectInSameLevel(project.getName(), project.getLevelProject().getId(),
                        project.getSemester().getId(), sessionHelper.getFacilityId(), null);
        if (isExistProject) {
            return RouterHelper.responseError("Dự án này đã tồn tại ở " + project.getLevelProject().getName() + " - "
                    + project.getSemester().getSemesterName() + " - " + project.getSemester().getYear() + " - "
                    + project.getSubjectFacility().getSubject().getName());
        }
        projectManagementRepository.save(project);
        userActivityLogHelper.saveLog("vừa thêm 1 dự án mới: " + project.getName());
        return RouterHelper.responseSuccess("Thêm dự án thành công", project);
    }

    @Override
    public ResponseEntity<?> updateProject(String idProject, USProjectCreateOrUpdateRequest request) {
        Project project = projectManagementRepository.findById(idProject).orElse(null);
        if (project == null) {
            return RouterHelper.responseError("Không tìm thấy dự án");
        }

        LevelProject levelProject = levelProjectRepository.findById(request.getLevelProjectId()).orElse(null);
        if (levelProject == null) {
            return RouterHelper.responseError("Không tìm thấy cấp độ dự án");
        }

        Semester semester = semesterRepository.findById(request.getSemesterId()).orElse(null);
        if (semester == null) {
            return RouterHelper.responseError("Không tìm thấy học kỳ");
        }

        SubjectFacility subjectFacility = subjectFacilityRepository.findById(request.getSubjectFacilityId())
                .orElse(null);
        if (subjectFacility == null) {
            return RouterHelper.responseError("Không tìm thấy bộ môn");
        }

        boolean isExistProject = projectManagementRepository
                .isExistProjectInSameLevel(request.getName(), request.getLevelProjectId(),
                        request.getSemesterId(), sessionHelper.getFacilityId(), project.getId());
        if (isExistProject) {
            return RouterHelper.responseError("Dự án này đã tồn tại ở " + project.getLevelProject().getName() + " - "
                    + project.getSemester().getSemesterName() + " - " + project.getSemester().getYear() + " - "
                    + project.getSubjectFacility().getSubject().getName());
        }
        if (!project.getSemester().getId().equals(request.getSemesterId())) {
            deleteBulkByProject.deleteAllByProjectId(project.getId());
        }
        String oldName = project.getName();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setLevelProject(levelProject);
        project.setSemester(semester);
        project.setSubjectFacility(subjectFacility);
        projectManagementRepository.save(project);

        userActivityLogHelper.saveLog("vừa cập nhật dự án: " + oldName + " → " + project.getName());
        return RouterHelper.responseSuccess("Cập nhật dự án thành công", project);
    }

    @Override
    public ResponseEntity<?> detailProject(String idProject) {
        return projectManagementRepository.getDetailProject(idProject)
                .map(project -> RouterHelper.responseSuccess("Detail thành công!", project))
                .orElseGet(() -> RouterHelper.responseSuccess("Không tìm thấy dự án", null));
    }

    @Override
    public ResponseEntity<?> changeStatus(String idProject) {
        Project project = projectManagementRepository.findById(idProject).get();
        String oldStatus = project.getStatus() == EntityStatus.ACTIVE ? "Hoạt động" : "Không hoạt động";
        if (project.getStatus() == EntityStatus.ACTIVE) {
            project.setStatus(EntityStatus.INACTIVE);
        } else {
            project.setStatus(EntityStatus.ACTIVE);
        }
        String newStatus = project.getStatus() == EntityStatus.ACTIVE ? "Hoạt động" : "Không hoạt động";
        projectManagementRepository.save(project);
        if (project.getStatus() == EntityStatus.ACTIVE) {
            commonUserStudentRepository.disableAllStudentDuplicateShiftByIdProject(project.getId());
        }
        userActivityLogHelper.saveLog(
                "vừa thay đổi trạng thái dự án " + project.getName() + " từ " + oldStatus + " thành " + newStatus);
        return RouterHelper.responseSuccess("Chuyển trạng thái thành công", project);
    }

    @Override
    public ResponseEntity<?> changeAllStatusPreviousSemester() {
        Long now = new Date().getTime();
        String semesterId = null;

        for (Semester semester : semesterRepository.findAll()) {
            if (semester.getToDate() < now) {
                semesterId = semester.getId();
                break;
            }
        }

        if (semesterId == null) {
            return RouterHelper.responseError("Không có kỳ học nào đã kết thúc");
        }

        List<Project> projects = projectManagementRepository.getAllProjectBySemester(sessionHelper.getFacilityId(),
                semesterId);

        if (projects.isEmpty()) {
            return RouterHelper.responseSuccess("Không có dự án nào thuộc kỳ học đã kết thúc", projects);
        }

        for (Project project : projects) {
            project.setStatus(project.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE
                    : EntityStatus.ACTIVE);
            projectManagementRepository.save(project);
            if (project.getStatus() == EntityStatus.ACTIVE) {
                commonUserStudentRepository.disableAllStudentDuplicateShiftByIdProject(project.getId());
            }
        }

        userActivityLogHelper
                .saveLog("vừa thay đổi trạng thái hàng loạt " + projects.size() + " dự án của kỳ học đã kết thúc");
        return RouterHelper.responseSuccess("Đổi trạng thái các dự án kỳ trước thành công", projects);
    }

}
