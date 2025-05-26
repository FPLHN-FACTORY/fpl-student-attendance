package udpm.hn.studentattendance.core.staff.student.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.ValidateHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

import java.util.*;
import java.util.stream.Collectors;

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
        Pageable pageable = PaginationHelper.createPageable(studentRequest);
        PageableObject pageableObject = PageableObject.of(studentExtendRepository
                .getAllStudentByFacility(pageable, studentRequest, sessionHelper.getFacilityId()));

        return RouterHelper.responseSuccess("Lấy danh sách sinh viên thành công", pageableObject);
    }

    @Override
    public ResponseEntity<?> getDetailStudent(String studentId) {
        Optional<UserStudent> userStudent = studentExtendRepository.findById(studentId);

        if (userStudent.isPresent()) {
            return RouterHelper.responseSuccess("Hiện thị chi tiết nhân viên thành công", userStudent);
        }

        return RouterHelper.responseError("Sinh viên không tồn tại");
    }

    @Override
    public ResponseEntity<?> createStudent(USStudentCreateUpdateRequest studentCreateUpdateRequest) {
        // Validate input
        if (!ValidateHelper.isValidCode(studentCreateUpdateRequest.getCode())) {
            return RouterHelper.responseError("Mã sinh viên không hợp lệ");
        }

        if (!ValidateHelper.isValidFullname(studentCreateUpdateRequest.getName())) {
            return RouterHelper.responseError("Họ tên sinh viên không hợp lệ");
        }

        if (!ValidateHelper.isValidEmailGmail(studentCreateUpdateRequest.getEmail())) {
            return RouterHelper.responseError("Email phải có định dạng @gmail.com");
        }

        Optional<UserStudent> existStudentCode = studentExtendRepository
                .getUserStudentByCode(studentCreateUpdateRequest.getCode());
        Optional<UserStudent> existStudentEmail = studentExtendRepository
                .getUserStudentByEmail(studentCreateUpdateRequest.getEmail());

        Optional<Facility> facility = facilityRepository.findById(sessionHelper.getFacilityId());

        if (existStudentCode.isPresent()) {
            return RouterHelper.responseError("Mã sinh viên đã tồn tại");
        }
        if (existStudentEmail.isPresent()) {
            return RouterHelper.responseError("Email sinh viên đã tồn tại");
        }

        UserStudent userStudent = new UserStudent();
        userStudent.setCode(studentCreateUpdateRequest.getCode());
        userStudent.setName(studentCreateUpdateRequest.getName());
        userStudent.setEmail(studentCreateUpdateRequest.getEmail());
        userStudent.setFacility(facility.get());
        userStudent.setStatus(EntityStatus.ACTIVE);
        studentExtendRepository.save(userStudent);
        return RouterHelper.responseSuccess("Thêm sinh viên mới thành công", userStudent);
    }

    @Override
    public ResponseEntity<?> updateStudent(USStudentCreateUpdateRequest studentCreateUpdateRequest) {
        // Validate input
        if (!ValidateHelper.isValidCode(studentCreateUpdateRequest.getCode())) {
            return RouterHelper.responseError("Mã sinh viên không hợp lệ");
        }

        if (!ValidateHelper.isValidFullname(studentCreateUpdateRequest.getName())) {
            return RouterHelper.responseError("Họ tên sinh viên không hợp lệ");
        }

        if (!ValidateHelper.isValidEmailGmail(studentCreateUpdateRequest.getEmail())) {
            return RouterHelper.responseError("Email phải có định dạng @gmail.com");
        }

        Optional<UserStudent> existStudent = studentExtendRepository
                .getStudentById(studentCreateUpdateRequest.getId());

        if (existStudent.isEmpty()) {
            return RouterHelper.responseError("Sinh viên không tồn tại");
        }

        UserStudent current = existStudent.get();

        // Check trùng code
        if (studentExtendRepository.isExistCodeUpdate(studentCreateUpdateRequest.getCode(),
                current.getCode())) {
            return RouterHelper.responseError("Mã sinh viên đã tồn tại");
        }

        // Check trùng email
        if (studentExtendRepository.isExistEmailFeUpdate(studentCreateUpdateRequest.getEmail(),
                current.getEmail())) {
            return RouterHelper.responseError("Đã có sinh viên khác dùng email này");
        }

        UserStudent userStudent = existStudent.get();
        userStudent.setCode(studentCreateUpdateRequest.getCode());
        userStudent.setEmail(studentCreateUpdateRequest.getEmail());
        userStudent.setName(studentCreateUpdateRequest.getName());
        studentExtendRepository.save(userStudent);
        return RouterHelper.responseSuccess("Cập nhật sinh viên thành công", userStudent);
    }

    @Override
    public ResponseEntity<?> changeStatusStudent(String studentId) {
        Optional<UserStudent> existStudent = studentExtendRepository.findById(studentId);

        if (existStudent.isPresent()) {
            UserStudent userStudent = existStudent.get();
            userStudent.setStatus(userStudent.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE
                    : EntityStatus.ACTIVE);
            studentExtendRepository.save(userStudent);
            return RouterHelper.responseSuccess("Thay đổi trạng thái sinh viên thành công", userStudent);
        } else {
            return RouterHelper.responseError("Sinh viên không tồn tại");
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

            return RouterHelper.responseSuccess("Cấp quyền thay đổi mặt sinh viên thành công", userStudent);
        }
        return RouterHelper.responseError("Sinh viên không tồn tại");
    }

    @Override
    public ResponseEntity<?> isExistFace() {
        List<Map<String, Object>> faceStatus = studentExtendRepository
                .existFaceForAllStudents(sessionHelper.getFacilityId());
        Map<String, Boolean> studentFaceMap = faceStatus.stream()
                .collect(Collectors.toMap(
                        m -> (String) m.get("studentId"),
                        m -> ((Number) m.get("hasFace")).intValue() == 1));

        return RouterHelper.responseSuccess("Lấy trạng thái face của sinh viên thành công", studentFaceMap);
    }

}
