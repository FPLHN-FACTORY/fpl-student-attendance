package udpm.hn.studentattendance.core.staff.student.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.notification.model.request.NotificationAddRequest;
import udpm.hn.studentattendance.core.notification.service.NotificationService;
import udpm.hn.studentattendance.core.staff.student.model.request.USStudentCreateUpdateRequest;
import udpm.hn.studentattendance.core.staff.student.model.request.USStudentRequest;
import udpm.hn.studentattendance.core.staff.student.repository.USStudentExtendRepository;
import udpm.hn.studentattendance.core.staff.student.repository.USStudentFacilityExtendRepository;
import udpm.hn.studentattendance.core.staff.student.service.STStudentService;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.helpers.NotificationHelper;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;

import java.util.*;

@Service
@RequiredArgsConstructor
@Validated
public class STStudentServiceImpl implements STStudentService {

    private final USStudentExtendRepository studentExtendRepository;

    private final SessionHelper sessionHelper;

    private final USStudentFacilityExtendRepository facilityRepository;

    private final NotificationService notificationService;

    @Override
    public ResponseEntity<?> getAllStudentByFacility(USStudentRequest studentRequest) {
        Pageable pageable = PaginationHelper.createPageable(studentRequest, "createdAt");
        PageableObject pageableObject = PageableObject.of(studentExtendRepository
                .getAllStudentByFacility(pageable, studentRequest, sessionHelper.getFacilityId()));

        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy danh sách sinh viên thành công",
                        pageableObject),
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
                            userStudent),
                    HttpStatus.OK);
        }

        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.ERROR,
                        "Sinh viên không tồn tại",
                        null),
                HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<?> createStudent(USStudentCreateUpdateRequest studentCreateUpdateRequest) {
        Optional<UserStudent> existStudentCode = studentExtendRepository
                .getUserStudentByCode(studentCreateUpdateRequest.getCode());
        Optional<UserStudent> existStudentEmail = studentExtendRepository
                .getUserStudentByEmail(studentCreateUpdateRequest.getEmail());

        Optional<Facility> facility = facilityRepository.findById(sessionHelper.getFacilityId());

        if (existStudentCode.isPresent()) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.ERROR,
                            "Mã sinh viên đã tồn tại",
                            null),
                    HttpStatus.CONFLICT);
        }
        if (existStudentEmail.isPresent()) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.ERROR,
                            "Email sinh viên đã tồn tại",
                            null),
                    HttpStatus.CONFLICT);
        }
        UserStudent userStudent = new UserStudent();
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
                        userStudent),
                HttpStatus.CREATED);

    }

    @Override
    public ResponseEntity<?> updateStudent(USStudentCreateUpdateRequest studentCreateUpdateRequest) {
        Optional<UserStudent> existStudent = studentExtendRepository
                .getStudentById(studentCreateUpdateRequest.getId());

        UserStudent current = existStudent.get();

        // 2. Check trùng code
        if (studentExtendRepository.isExistCodeUpdate(studentCreateUpdateRequest.getCode(), current.getCode())) {
            return new ResponseEntity<>(
                    new ApiResponse(RestApiStatus.ERROR, "Mã sinh viên đã tồn tại", null),
                    HttpStatus.BAD_REQUEST);
        }
        // 3. Check trùng email FE
        if (studentExtendRepository.isExistEmailFeUpdate(studentCreateUpdateRequest.getEmail(), current.getEmail())) {
            return new ResponseEntity<>(
                    new ApiResponse(RestApiStatus.ERROR, "Đã có sinh viên khác dùng email này", null),
                    HttpStatus.BAD_REQUEST);
        }
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
                            userStudent),
                    HttpStatus.OK);
        }
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.ERROR,
                        "Sinh viên không tồn tại",
                        null),
                HttpStatus.NOT_FOUND);

    }

    @Override
    public ResponseEntity<?> changeStatusStudent(String studentId) {
        Optional<UserStudent> existStudent = studentExtendRepository.findById(studentId);

        if (existStudent.isPresent()) {
            UserStudent userStudent = existStudent.get();
            userStudent.setStatus(userStudent.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE
                    : EntityStatus.ACTIVE);
            studentExtendRepository.save(userStudent);
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.SUCCESS,
                            "Thay đổi trạng thái sinh viên thành công",
                            userStudent),
                    HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.ERROR,
                            "Sinh viên không tồn tại",
                            null),
                    HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> deleteFaceStudentFactory(String studentId) {
        Optional<UserStudent> existUserStudent = studentExtendRepository.findById(studentId);
        if (existUserStudent.isPresent()) {
            UserStudent userStudent = existUserStudent.get();
            userStudent.setFaceEmbedding(null);
            studentExtendRepository.save(userStudent);

            Map<String, Object> dataNotification = new HashMap<>();
            dataNotification.put(NotificationHelper.KEY_USER_STAFF,
                    sessionHelper.getUserCode() + " - " + sessionHelper.getUserName());
            NotificationAddRequest notificationAddRequest = new NotificationAddRequest();
            notificationAddRequest.setIdUser(userStudent.getId());
            notificationAddRequest.setType(NotificationHelper.TYPE_REMOVE_FACE_ID);
            notificationAddRequest.setData(dataNotification);
            notificationService.add(notificationAddRequest);

            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.SUCCESS,
                            "Cấp quyền thay đổi mặt sinh viên thành công",
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

    @Override
    public ResponseEntity<?> isExistFace() {
        List<Integer> raw = studentExtendRepository.existFaceForAllStudents(sessionHelper.getFacilityId());
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.ERROR,
                        "Sinh viên đã đăng ký mặt",
                        raw),
                HttpStatus.OK);
    }

}

