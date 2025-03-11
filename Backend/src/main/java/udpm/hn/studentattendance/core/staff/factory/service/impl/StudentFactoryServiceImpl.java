package udpm.hn.studentattendance.core.staff.factory.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.staff.factory.model.request.StudentFactoryCreateUpdateRequest;
import udpm.hn.studentattendance.core.staff.factory.model.request.StudentFactoryRequest;
import udpm.hn.studentattendance.core.staff.factory.model.request.UserStudentRequest;
import udpm.hn.studentattendance.core.staff.factory.repository.StudentFactoryRepository;
import udpm.hn.studentattendance.core.staff.factory.repository.UserStudentFactoryExtendRepository;
import udpm.hn.studentattendance.core.staff.factory.service.StudentFactoryService;
import udpm.hn.studentattendance.entities.Factory;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.entities.UserStudentFactory;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.repositories.FactoryRepository;
import udpm.hn.studentattendance.repositories.UserStudentRepository;
import udpm.hn.studentattendance.utils.CodeGeneratorUtils;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Validated
public class StudentFactoryServiceImpl implements StudentFactoryService {

    private final StudentFactoryRepository studentFactoryRepository;

    private final UserStudentRepository userStudentRepository;

    private final FactoryRepository factoryRepository;

    private final UserStudentFactoryExtendRepository userStudentFactoryExtendRepository;

    private final SessionHelper sessionHelper;

    @Override
    public ResponseEntity<?> getAllStudentInFactory(String factoryId, StudentFactoryRequest studentFactoryRequest) {
        Pageable pageable = PaginationHelper.createPageable(studentFactoryRequest, "createdAt");
        PageableObject listStudentFactory = PageableObject.of(studentFactoryRepository.getUserStudentInFactory(pageable, factoryId, studentFactoryRequest));
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy tất cả sinh viên trong nhóm xưởng thành công",
                        listStudentFactory
                ),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> deleteStudentInFactory(String userStudentFactoryId) {
        Optional<UserStudentFactory> existStudentFactory = studentFactoryRepository.findById(userStudentFactoryId);
        if (existStudentFactory.isPresent()) {
            studentFactoryRepository.deleteById(userStudentFactoryId);
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.SUCCESS,
                            "Xoá sinh viên khỏi nhóm xưởng thành công",
                            null
                    ),
                    HttpStatus.OK);
        }
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.WARNING,
                        "Sinh viên không tồn tại trong nhóm xưởng",
                        null
                ),
                HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<?> changeStatus(String userStudentFactoryId) {
        Optional<UserStudentFactory> existStudentFactory = studentFactoryRepository.findById(userStudentFactoryId);
        if (existStudentFactory.isPresent()) {
            UserStudentFactory userStudentFactory = existStudentFactory.get();
            userStudentFactory.setStatus(userStudentFactory.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
            studentFactoryRepository.save(userStudentFactory);
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.SUCCESS,
                            "Đổi trạng thaí sinh viên thành công",
                            userStudentFactory
                    ),
                    HttpStatus.OK);
        }
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.WARNING,
                        "Sinh viên không tồn tại trong nhóm xưởng",
                        null
                ),
                HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<?> createOrDeleteStudentFactory(StudentFactoryCreateUpdateRequest studentFactoryCreateUpdateRequest) {
        Optional<UserStudentFactory> existStudentFactory = studentFactoryRepository
                .getUserStudentFactoriesByUserStudentIdAndFactoryId
                        (studentFactoryCreateUpdateRequest.getStudentId(), studentFactoryCreateUpdateRequest.getFactoryId());
        Optional<Factory> existFactory = factoryRepository.findById(studentFactoryCreateUpdateRequest.getFactoryId());
        Optional<UserStudent> existUserStudent = userStudentRepository.findById(studentFactoryCreateUpdateRequest.getStudentId());
        if (existStudentFactory.isPresent()) {
            studentFactoryRepository.deleteById(existStudentFactory.get().getId());
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.WARNING,
                            "Xoá sinh viên ra khỏi nhóm xưởng thành công",
                            null
                    ),
                    HttpStatus.BAD_REQUEST);
        } else {
            UserStudentFactory userStudentFactory = new UserStudentFactory();
            userStudentFactory.setId(CodeGeneratorUtils.generateRandom());
            userStudentFactory.setUserStudent(existUserStudent.get());
            userStudentFactory.setFactory(existFactory.get());
            userStudentFactory.setStatus(EntityStatus.ACTIVE);
            studentFactoryRepository.save(userStudentFactory);
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.WARNING,
                            "Thêm sinh viên vào nhóm xưởng thành công",
                            userStudentFactory
                    ),
                    HttpStatus.OK);
        }
    }

    @Override
    public ResponseEntity<?> getStudentFactoryExist() {
        return null;
    }

    @Override
    public ResponseEntity<?> getAllStudent(UserStudentRequest userStudentRequest) {
        Pageable pageable = PaginationHelper.createPageable(userStudentRequest, "createdAt");
        PageableObject listStudent = PageableObject
                .of(userStudentFactoryExtendRepository.getAllUserStudent
                        (pageable, sessionHelper.getFacilityId(), EntityStatus.ACTIVE, userStudentRequest));
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.WARNING,
                        "Lấy tất cả sinh viên theo cơ sở thành công",
                        listStudent
                ),
                HttpStatus.OK);
    }
}
