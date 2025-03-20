package udpm.hn.studentattendance.core.teacher.student.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.teacher.student.model.request.Teacher_FactoryRequest;
import udpm.hn.studentattendance.core.teacher.student.repository.Teacher_FactoryExtendRepository;
import udpm.hn.studentattendance.core.teacher.student.repository.Teacher_ProjectExtendRepository;
import udpm.hn.studentattendance.core.teacher.student.service.Teacher_FactoryService;
import udpm.hn.studentattendance.entities.Project;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;

import java.util.List;


@Service
@RequiredArgsConstructor
@Validated
public class Teacher_FactoryServiceImpl implements Teacher_FactoryService {

    private final Teacher_FactoryExtendRepository teacherStudentFactoryExtendRepository;

    private final SessionHelper sessionHelper;

    private final Teacher_ProjectExtendRepository teacherStudentProjectExtendRepository;

    @Override
    public ResponseEntity<?> getAllFactoryByTeacher(Teacher_FactoryRequest teacherStudentRequest) {
        Pageable pageable = PaginationHelper.createPageable(teacherStudentRequest, "createdAt");
        PageableObject listFactoryByTeacher = PageableObject
                .of(teacherStudentFactoryExtendRepository.getAllFactoryByTeacher
                        (pageable, sessionHelper.getFacilityId(), sessionHelper.getUserCode(), teacherStudentRequest));
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy tất cả nhóm xưởng do giảng viên " + sessionHelper.getUserCode() + " thành công",
                        listFactoryByTeacher
                ),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getAllProjectByFacility() {
        List<Project> projects = teacherStudentProjectExtendRepository.getAllProjectName(sessionHelper.getFacilityId());
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy tất cả dự án theo cơ sở thành công",
                        projects
                ),
                HttpStatus.OK);
    }
}
