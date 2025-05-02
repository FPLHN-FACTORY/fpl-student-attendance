package udpm.hn.studentattendance.core.teacher.factory.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCStudentFactoryRequest;
import udpm.hn.studentattendance.core.teacher.factory.repository.TCStudentFactoryExtendRepository;
import udpm.hn.studentattendance.core.teacher.factory.repository.TCUserStudentExtendRepository;
import udpm.hn.studentattendance.core.teacher.factory.service.TCStudentFactoryService;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.entities.UserStudentFactory;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Validated
public class TCStudentFactoryServiceImpl implements TCStudentFactoryService {
    private final TCStudentFactoryExtendRepository teacherStudentFactoryExtendRepository;

    private final TCUserStudentExtendRepository userStudentRepository;

    private final SessionHelper sessionHelper;

    @Override
    public ResponseEntity<?> getAllStudentFactory(TCStudentFactoryRequest studentRequest) {
        Pageable pageable = PaginationHelper.createPageable(studentRequest, "createdAt");
        PageableObject pageableObject = PageableObject
                .of(teacherStudentFactoryExtendRepository
                        .getUserStudentInFactory(pageable, studentRequest.getFactoryId(),
                                studentRequest));
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy tất cả học sinh trong nhóm xưởng thành công",
                        pageableObject),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> deleteStudentFactoryById(String studentFactoryId) {
        Optional<UserStudentFactory> userStudentFactory = teacherStudentFactoryExtendRepository
                .findById(studentFactoryId);
        if (userStudentFactory.isPresent()) {
            teacherStudentFactoryExtendRepository.deleteById(studentFactoryId);
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.SUCCESS,
                            "Xoá sinh viên có mã " + userStudentFactory.get()
                                    .getUserStudent().getCode() + " thành công",
                            null),
                    HttpStatus.OK);
        }
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.ERROR,
                        "Sinh viên không tồn tại",
                        null),
                HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<?> changeStatusStudentFactory(String studentFactoryId) {
        Optional<UserStudentFactory> userStudentFactory = teacherStudentFactoryExtendRepository
                .findById(studentFactoryId);

        boolean isExistsShift = teacherStudentFactoryExtendRepository
                .isStudentExistsShift(
                        sessionHelper.getFacilityId(),
                        userStudentFactory.get().getFactory().getId(),
                        userStudentFactory.get().getId());

        if (isExistsShift) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.ERROR,
                            "Đổi trạng thái sinh viên thất bại: Sinh viên  tồn tại ở ca khác",
                            null),
                    HttpStatus.BAD_REQUEST);
        }

        if (userStudentFactory.isPresent()) {
            UserStudentFactory userStudentPresent = userStudentFactory.get();
            userStudentPresent.setStatus(userStudentPresent.getStatus().equals(EntityStatus.ACTIVE)
                    ? EntityStatus.INACTIVE
                    : EntityStatus.ACTIVE);
            teacherStudentFactoryExtendRepository.save(userStudentPresent);
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.SUCCESS,
                            "Thay đổi trạng thái thành công",
                            null),
                    HttpStatus.OK);
        }
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.ERROR,
                        "Sinh viên không tồn tại",
                        null),
                HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<?> deleteFaceStudentFactory(String studentId) {
        Optional<UserStudent> existUserStudent = userStudentRepository.findById(studentId);
        if (existUserStudent.isPresent()) {
            UserStudent userStudent = existUserStudent.get();
            userStudent.setFaceEmbedding(null);
            userStudentRepository.save(userStudent);
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.SUCCESS,
                            "Cấp quyền thay đổi mặt thành công",
                            userStudent),
                    HttpStatus.OK);
        }
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.ERROR,
                        "Sinh viên không tồn tại",
                        null),
                HttpStatus.BAD_REQUEST);
    }

}
