package udpm.hn.studentattendance.core.staff.student.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.helpers.ValidateHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;

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

    private final UserActivityLogHelper userActivityLogHelper;

    private final RedisService redisService;

    @Value("${app.config.disabled-check-email-fpt}")
    private String isDisableCheckEmailFpt;

    @Value("${spring.cache.redis.time-to-live}")
    private long redisTTL;

    public PageableObject<?> getCachedStudentList(USStudentRequest studentRequest) {
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_STUDENT + "list_" + sessionHelper.getFacilityId() + "_"
                + studentRequest.toString();

        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            try {
                return redisService.getObject(cacheKey, PageableObject.class);
            } catch (Exception e) {
                redisService.delete(cacheKey);
            }
        }

        Pageable pageable = PaginationHelper.createPageable(studentRequest);
        PageableObject<?> pageableObject = PageableObject.of(studentExtendRepository
                .getAllStudentByFacility(pageable, studentRequest, sessionHelper.getFacilityId()));

        try {
            redisService.set(cacheKey, pageableObject, redisTTL);
        } catch (Exception ignored) {
        }

        return pageableObject;
    }

    @Override
    public ResponseEntity<?> getAllStudentByFacility(USStudentRequest studentRequest) {
        PageableObject<?> pageableObject = getCachedStudentList(studentRequest);
        return RouterHelper.responseSuccess("Lấy danh sách sinh viên thành công", pageableObject);
    }

    public UserStudent getCachedStudentDetail(String studentId) {
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_STUDENT + studentId;

        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            try {
                return redisService.getObject(cacheKey, UserStudent.class);
            } catch (Exception e) {
                redisService.delete(cacheKey);
            }
        }

        Optional<UserStudent> userStudent = studentExtendRepository.findById(studentId);

        UserStudent result = userStudent.orElse(null);
        if (result != null) {
            try {
                redisService.set(cacheKey, result, redisTTL);
            } catch (Exception ignored) {
            }
        }

        return result;
    }

    @Override
    public ResponseEntity<?> getDetailStudent(String studentId) {
        UserStudent userStudent = getCachedStudentDetail(studentId);
        if (userStudent != null) {
            return RouterHelper.responseSuccess("Hiện thị chi tiết sinh viên thành công", userStudent);
        }
        return RouterHelper.responseError("Sinh viên không tồn tại");
    }

    @Override
    public ResponseEntity<?> createStudent(USStudentCreateUpdateRequest studentCreateUpdateRequest) {
        if (!ValidateHelper.isValidCode(studentCreateUpdateRequest.getCode())) {
            return RouterHelper.responseError(
                    "Mã sinh viên không hợp lệ: không có khoảng trắng, không có ký tự đặc biệt ngoài dấu chấm . và dấu gạch dưới _.");
        }

        if (!ValidateHelper.isValidFullname(studentCreateUpdateRequest.getName())) {
            return RouterHelper.responseError(
                    "Họ Tên admin không hợp lệ: Tối thiểu 2 từ, cách nhau bởi khoảng trắng và Chỉ gồm ký tự chữ không chứa số hay ký tự đặc biệt.");
        }

        String email = studentCreateUpdateRequest.getEmail().trim();
        boolean isValidEmail = false;

        if (ValidateHelper.isValidEmailGmail(email) ||
                ValidateHelper.isValidEmailFE(email) ||
                ValidateHelper.isValidEmailFPT(email)) {
            isValidEmail = true;
        }

        if (!isValidEmail) {
            return RouterHelper.responseError("Email phải có định dạng @gmail.com hoặc kết thúc bằng edu.vn");
        }

        if (!isDisableCheckEmailFpt.equalsIgnoreCase("true")) {
            if (!ValidateHelper.isValidEmailFE(email) && !ValidateHelper.isValidEmailFPT(email)) {
                return RouterHelper.responseError("Email phải kết thúc bằng edu.vn");
            }
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
        UserStudent saveUserStudent = studentExtendRepository.save(userStudent);
        userActivityLogHelper
                .saveLog("vừa thêm 1 sinh viên mới: " + saveUserStudent.getCode() + " - " + saveUserStudent.getName());

        // Xóa cache liên quan đến danh sách sinh viên
        invalidateStudentListCache();

        return RouterHelper.responseSuccess("Thêm sinh viên mới thành công", saveUserStudent);
    }

    @Override
    public ResponseEntity<?> updateStudent(USStudentCreateUpdateRequest studentCreateUpdateRequest) {
        if (!ValidateHelper.isValidCode(studentCreateUpdateRequest.getCode())) {
            return RouterHelper.responseError(
                    "Mã sinh viên không hợp lệ: không có khoảng trắng, không có ký tự đặc biệt ngoài dấu chấm . và dấu gạch dưới _.");
        }

        if (!ValidateHelper.isValidFullname(studentCreateUpdateRequest.getName())) {
            return RouterHelper.responseError(
                    "Họ Tên sinh viên không hợp lệ: Tối thiểu 2 từ, cách nhau bởi khoảng trắng và Chỉ gồm ký tự chữ không chứa số hay ký tự đặc biệt.");
        }

        String email = studentCreateUpdateRequest.getEmail().trim();
        boolean isValidEmail = false;

        if (ValidateHelper.isValidEmailGmail(email) ||
                ValidateHelper.isValidEmailFE(email) ||
                ValidateHelper.isValidEmailFPT(email)) {
            isValidEmail = true;
        }

        if (!isValidEmail) {
            return RouterHelper.responseError("Email phải có định dạng @gmail.com hoặc kết thúc bằng edu.vn");
        }

        if (!isDisableCheckEmailFpt.equalsIgnoreCase("true")) {
            if (!ValidateHelper.isValidEmailFE(email) && !ValidateHelper.isValidEmailFPT(email)) {
                return RouterHelper.responseError("Email phải kết thúc bằng edu.vn");
            }
        }

        Optional<UserStudent> existStudent = studentExtendRepository
                .getStudentById(studentCreateUpdateRequest.getId());

        if (existStudent.isEmpty()) {
            return RouterHelper.responseError("Sinh viên không tồn tại");
        }

        UserStudent current = existStudent.get();

        if (studentExtendRepository.isExistCodeUpdate(studentCreateUpdateRequest.getCode(),
                current.getCode())) {
            return RouterHelper.responseError("Mã sinh viên đã tồn tại");
        }

        if (studentExtendRepository.isExistEmailFeUpdate(studentCreateUpdateRequest.getEmail(),
                current.getEmail())) {
            return RouterHelper.responseError("Đã có sinh viên khác dùng email này");
        }

        UserStudent userStudent = existStudent.get();
        userStudent.setCode(studentCreateUpdateRequest.getCode());
        userStudent.setEmail(studentCreateUpdateRequest.getEmail());
        userStudent.setName(studentCreateUpdateRequest.getName());
        UserStudent saveUserStudent = studentExtendRepository.save(userStudent);
        userActivityLogHelper
                .saveLog("vừa cập nhật sinh viên: " + saveUserStudent.getCode() + " - " + saveUserStudent.getName());

        // Xóa cache liên quan đến sinh viên này
        invalidateStudentCache(saveUserStudent.getId());

        return RouterHelper.responseSuccess("Cập nhật sinh viên thành công", saveUserStudent);
    }

    @Override
    public ResponseEntity<?> changeStatusStudent(String studentId) {
        Optional<UserStudent> existStudent = studentExtendRepository.findById(studentId);

        if (existStudent.isPresent()) {
            UserStudent userStudent = existStudent.get();
            String oldStatus = userStudent.getStatus() == EntityStatus.ACTIVE ? "Hoạt động" : "Không hoạt động";
            userStudent.setStatus(userStudent.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE
                    : EntityStatus.ACTIVE);
            String newStatus = userStudent.getStatus() == EntityStatus.ACTIVE ? "Hoạt động" : "Không hoạt động";
            studentExtendRepository.save(userStudent);
            userActivityLogHelper.saveLog("vừa thay đổi trạng thái sinh viên " + userStudent.getCode() + " - "
                    + userStudent.getName() + " từ " + oldStatus + " thành " + newStatus);

            // Xóa cache liên quan đến sinh viên này
            invalidateStudentCache(studentId);

            return RouterHelper.responseSuccess("Thay đổi trạng thái sinh viên thành công", userStudent);
        } else {
            return RouterHelper.responseError("Sinh viên không tồn tại");
        }
    }

    @Override
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

            userActivityLogHelper.saveLog("vừa xóa dữ liệu khuôn mặt của sinh viên: " + userStudent.getCode() + " - "
                    + userStudent.getName());

            // Xóa cache liên quan đến sinh viên này
            invalidateStudentCache(studentId);
            // Xóa cache trạng thái face
            invalidateFaceStatusCache();

            return RouterHelper.responseSuccess("Cấp quyền thay đổi mặt sinh viên thành công", userStudent);
        }
        return RouterHelper.responseError("Sinh viên không tồn tại");
    }

    public Map<String, Boolean> getCachedFaceStatus() {
        // Tạo cache key
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_STUDENT + "face_status_" + sessionHelper.getFacilityId();

        // Thử lấy từ cache
        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            try {
                return redisService.getObject(cacheKey, Map.class);
            } catch (Exception e) {
                redisService.delete(cacheKey);
            }
        }

        // Nếu không có trong cache, truy vấn từ database
        List<Map<String, Object>> faceStatus = studentExtendRepository
                .existFaceForAllStudents(sessionHelper.getFacilityId());
        Map<String, Boolean> studentFaceMap = faceStatus.stream()
                .collect(Collectors.toMap(
                        m -> (String) m.get("studentId"),
                        m -> ((Number) m.get("hasFace")).intValue() == 1));

        // Lưu vào cache
        try {
            redisService.set(cacheKey, studentFaceMap, redisTTL);
        } catch (Exception ignored) {
        }

        return studentFaceMap;
    }

    @Override
    public ResponseEntity<?> isExistFace() {
        Map<String, Boolean> studentFaceMap = getCachedFaceStatus();
        return RouterHelper.responseSuccess("Lấy trạng thái face của sinh viên thành công", studentFaceMap);
    }

    /**
     * Xóa cache trạng thái face
     */
    private void invalidateFaceStatusCache() {
        redisService.delete(RedisPrefixConstant.REDIS_PREFIX_STUDENT + "face_status_" + sessionHelper.getFacilityId());
    }

    /**
     * Xóa cache liên quan đến một sinh viên cụ thể
     */
    private void invalidateStudentCache(String studentId) {
        // Xóa cache chi tiết sinh viên
        redisService.delete(RedisPrefixConstant.REDIS_PREFIX_STUDENT + studentId);
        // Xóa cache danh sách sinh viên
        invalidateStudentListCache();
        // Xóa cache trạng thái face
        invalidateFaceStatusCache();
    }

    /**
     * Xóa cache danh sách sinh viên
     */
    private void invalidateStudentListCache() {
        redisService.deletePattern(
                RedisPrefixConstant.REDIS_PREFIX_STUDENT + "list_" + sessionHelper.getFacilityId() + "_*");
    }
}
