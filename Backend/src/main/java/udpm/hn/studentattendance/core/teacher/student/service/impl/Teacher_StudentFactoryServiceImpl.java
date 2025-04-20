package udpm.hn.studentattendance.core.teacher.student.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.teacher.student.model.request.Teacher_StudentFactoryRequest;
import udpm.hn.studentattendance.core.teacher.student.repository.Teacher_StudentFactoryExtendRepository;
import udpm.hn.studentattendance.core.teacher.student.service.Teacher_StudentFactoryService;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.entities.UserStudentFactory;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.repositories.UserStudentRepository;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Validated
public class Teacher_StudentFactoryServiceImpl implements Teacher_StudentFactoryService {
    private final Teacher_StudentFactoryExtendRepository teacherStudentFactoryExtendRepository;

    private final UserStudentRepository userStudentRepository;

    private final SessionHelper sessionHelper;

    @Override
    public ResponseEntity<?> getAllStudentFactory(Teacher_StudentFactoryRequest studentRequest) {
        Pageable pageable = PaginationHelper.createPageable(studentRequest, "createdAt");
        PageableObject pageableObject = PageableObject
                .of(teacherStudentFactoryExtendRepository
                        .getUserStudentInFactory(pageable, studentRequest.getFactoryId(), studentRequest));
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy tất cả học sinh trong nhóm xưởng thành công",
                        pageableObject
                ),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> deleteStudentFactoryById(String studentFactoryId) {
        Optional<UserStudentFactory> userStudentFactory = teacherStudentFactoryExtendRepository.findById(studentFactoryId);
        if (userStudentFactory.isPresent()) {
            teacherStudentFactoryExtendRepository.deleteById(studentFactoryId);
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.SUCCESS,
                            "Xoá sinh viên có mã " + userStudentFactory.get().getUserStudent().getCode() + " thành công",
                            null
                    ),
                    HttpStatus.OK);
        }
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.ERROR,
                        "Sinh viên không tồn tại",
                        null
                ),
                HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<?> changeStatusStudentFactory(String studentFactoryId) {
        Optional<UserStudentFactory> userStudentFactory = teacherStudentFactoryExtendRepository.findById(studentFactoryId);
        if (userStudentFactory.isPresent()) {
            UserStudentFactory userStudentPresent = userStudentFactory.get();
            userStudentPresent.setStatus(userStudentPresent.getStatus().equals(EntityStatus.ACTIVE) ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
            teacherStudentFactoryExtendRepository.save(userStudentPresent);
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.SUCCESS,
                            "Thay đổi trạng thái thành công",
                            null
                    ),
                    HttpStatus.OK);
        }
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.ERROR,
                        "Sinh viên không tồn tại",
                        null
                ),
                HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<?> deleteFaceStudentFactory(String studentId) {
        Optional<UserStudent> existUserStudent = userStudentRepository.findById(studentId);
        if (existUserStudent.isPresent()){
            UserStudent userStudent = existUserStudent.get();
            userStudent.setFaceEmbedding(null);
            userStudentRepository.save(userStudent);
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.SUCCESS,
                            "Cấp quyền thay đổi mặt thành công",
                            userStudent
                    ),
            HttpStatus.OK);
        }
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.ERROR,
                        "Sinh viên không tồn tại",
                        null
                ),
                HttpStatus.BAD_REQUEST);
    }


}
