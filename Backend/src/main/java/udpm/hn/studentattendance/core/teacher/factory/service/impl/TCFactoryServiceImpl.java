package udpm.hn.studentattendance.core.teacher.factory.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCFactoryRequest;
import udpm.hn.studentattendance.core.teacher.factory.repository.TCFactoryExtendRepository;
import udpm.hn.studentattendance.core.teacher.factory.repository.TCProjectExtendRepository;
import udpm.hn.studentattendance.core.teacher.factory.service.TCFactoryService;
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
public class TCFactoryServiceImpl implements TCFactoryService {

        private final TCFactoryExtendRepository teacherStudentFactoryExtendRepository;

        private final SessionHelper sessionHelper;

        private final TCProjectExtendRepository teacherStudentProjectExtendRepository;

        @Override
        public ResponseEntity<?> getAllFactoryByTeacher(TCFactoryRequest teacherStudentRequest) {
                Pageable pageable = PaginationHelper.createPageable(teacherStudentRequest, "createdAt");
                PageableObject listFactoryByTeacher = PageableObject
                                .of(teacherStudentFactoryExtendRepository.getAllFactoryByTeacher(pageable,
                                                sessionHelper.getFacilityId(), sessionHelper.getUserCode(),
                                                teacherStudentRequest));
                return new ResponseEntity<>(
                                new ApiResponse(
                                                RestApiStatus.SUCCESS,
                                                "Lấy tất cả nhóm xưởng do giảng viên " + sessionHelper.getUserCode()
                                                                + " thành công",
                                                listFactoryByTeacher),
                                HttpStatus.OK);
        }

        @Override
        public ResponseEntity<?> getAllProjectByFacility() {
                List<Project> projects = teacherStudentProjectExtendRepository
                                .getAllProjectName(sessionHelper.getFacilityId());
                return new ResponseEntity<>(
                                new ApiResponse(
                                                RestApiStatus.SUCCESS,
                                                "Lấy tất cả dự án theo cơ sở thành công",
                                                projects),
                                HttpStatus.OK);
        }

        @Override
        public ResponseEntity<?> getAllPlanDateByFactory() {
                return null;
        }
}
