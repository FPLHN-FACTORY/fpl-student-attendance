package udpm.hn.studentattendance.core.staff.student.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.staff.student.model.request.Staff_StudentCreateUpdateRequest;
import udpm.hn.studentattendance.core.staff.student.model.request.Staff_StudentRequest;
import udpm.hn.studentattendance.core.staff.student.repository.Staff_StudentExtendRepository;
import udpm.hn.studentattendance.core.staff.student.service.Staff_StudentService;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.repositories.FacilityRepository;
import udpm.hn.studentattendance.utils.CodeGeneratorUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Validated
public class Staff_StudentServiceImpl implements Staff_StudentService {

    private final Staff_StudentExtendRepository studentExtendRepository;

    private final SessionHelper sessionHelper;

    private final FacilityRepository facilityRepository;

    @Override
    public ResponseEntity<?> getAllStudentByFacility(Staff_StudentRequest studentRequest) {
        Pageable pageable = PaginationHelper.createPageable(studentRequest, "createdAt");
        PageableObject pageableObject = PageableObject.of
                (studentExtendRepository.getAllStudentByFacility(pageable, studentRequest, sessionHelper.getFacilityId()));

        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy danh sách sinh viên thành công",
                        pageableObject
                ),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getDetailStudent(String studentId) {
        Optional<UserStudent> userStudent = studentExtendRepository.findById(studentId);

        if (userStudent.isPresent()) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.SUCCESS,
                            "Hiện thị chi tiết nhân viên thành công",
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
                HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<?> createStudent(Staff_StudentCreateUpdateRequest studentCreateUpdateRequest) {
        Optional<UserStudent> existStudentCode = studentExtendRepository.getUserStudentByCode(studentCreateUpdateRequest.getCode());
        Optional<UserStudent> existStudentEmail = studentExtendRepository.getUserStudentByEmail(studentCreateUpdateRequest.getEmail());

        Optional<Facility> facility = facilityRepository.findById(sessionHelper.getFacilityId());

        if (existStudentCode.isEmpty() || existStudentEmail.isEmpty()) {
            UserStudent userStudent = new UserStudent();
            userStudent.setId(CodeGeneratorUtils.generateRandom());
            userStudent.setCode(studentCreateUpdateRequest.getCode());
            userStudent.setName(studentCreateUpdateRequest.getName());
            userStudent.setEmail(studentCreateUpdateRequest.getEmail());
            userStudent.setFacility(facility.get());
            userStudent.setStatus(EntityStatus.ACTIVE);
            studentExtendRepository.save(userStudent);
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.SUCCESS,
                            "Thêm sinh viên mới thành công",
                            userStudent
                    ),
                    HttpStatus.CREATED);
        }

        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.ERROR,
                        "Sinh viên đã tồn tại",
                        null
                ),
                HttpStatus.CONFLICT);
    }

    @Override
    public ResponseEntity<?> updateStudent(Staff_StudentCreateUpdateRequest studentCreateUpdateRequest) {
        Optional<UserStudent> existStudent = studentExtendRepository.getStudentById(studentCreateUpdateRequest.getId());

        if (existStudent.isPresent()) {
            UserStudent userStudent = existStudent.get();
            userStudent.setCode(studentCreateUpdateRequest.getCode());
            userStudent.setEmail(studentCreateUpdateRequest.getEmail());
            userStudent.setName(studentCreateUpdateRequest.getName());
            studentExtendRepository.save(userStudent);
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.SUCCESS,
                            "Cập nhật sinh viên thành công",
                            userStudent
                    ),
                    HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.ERROR,
                            "Sinh viên không tồn tại",
                            null
                    ),
                    HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ResponseEntity<?> changeStatusStudent(String studentId) {
        Optional<UserStudent> existStudent = studentExtendRepository.findById(studentId);

        if (existStudent.isPresent()) {
            UserStudent userStudent = existStudent.get();
            userStudent.setStatus(userStudent.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
            studentExtendRepository.save(userStudent);
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.SUCCESS,
                            "Cập nhật sinh viên thành công",
                            userStudent
                    ),
                    HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.ERROR,
                            "Sinh viên không tồn tại",
                            null
                    ),
                    HttpStatus.NOT_FOUND);
        }
    }

}
