package udpm.hn.studentattendance.core.admin.subject.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.admin.subject.model.request.ADSubjectCreateRequest;
import udpm.hn.studentattendance.core.admin.subject.model.request.ADSubjectSearchRequest;
import udpm.hn.studentattendance.core.admin.subject.model.request.ADSubjectUpdateRequest;
import udpm.hn.studentattendance.core.admin.subject.repository.ADSubjectExtendRepository;
import udpm.hn.studentattendance.core.admin.subject.service.ADSubjectManagementService;
import udpm.hn.studentattendance.entities.Subject;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.helpers.ValidateHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonPlanDateRepository;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import com.fasterxml.jackson.core.type.TypeReference;
import udpm.hn.studentattendance.helpers.RedisCacheHelper;

@Service
@RequiredArgsConstructor
public class ADSubjectManagementServiceImpl implements ADSubjectManagementService {

    private final ADSubjectExtendRepository adminSubjectRepository;

    private final CommonPlanDateRepository commonPlanDateRepository;

    private final UserActivityLogHelper userActivityLogHelper;

    private final RedisCacheHelper redisCacheHelper;

    private final RedisInvalidationHelper redisInvalidationHelper;

    public PageableObject getSubjects(ADSubjectSearchRequest request) {
        String key = RedisPrefixConstant.REDIS_PREFIX_SUBJECT + "list_" + request.toString();
        return redisCacheHelper.getOrSet(
                key,
                () -> PageableObject
                        .of(adminSubjectRepository.getAll(PaginationHelper.createPageable(request, "id"), request)),
                new TypeReference<>() {
                });
    }

    public Subject getSubjectById(String id) {
        return adminSubjectRepository.findById(id).orElse(null);
    }

    @Override
    public ResponseEntity<?> getListSubject(ADSubjectSearchRequest request) {
        PageableObject result = getSubjects(request);
        return RouterHelper.responseSuccess("Lấy danh sách bộ môn thành công", result);
    }

    @Override
    public ResponseEntity<?> createSubject(ADSubjectCreateRequest request) {


        if (!ValidateHelper.isValidCode(request.getCode())) {
            return RouterHelper.responseError(
                    "Mã bộ môn không hợp lệ: không có khoảng trắng, không có ký tự đặc biệt ngoài dấu chấm . và dấu gạch dưới _.");
        }
        if (!ValidateHelper.isValidFullname(request.getName())) {
            return RouterHelper.responseError(
                    "Tên bộ môn không hợp lệ: Tối thiểu 2 từ, cách nhau bởi khoảng trắng và Chỉ gồm ký tự chữ không chứa số hay ký tự đặc biệt.");
        }
        Subject s = new Subject();
        s.setName(request.getName().trim());
        s.setCode(request.getCode().toUpperCase());

        if (adminSubjectRepository.isExistsCodeSubject(s.getCode(), null)) {
            return RouterHelper.responseError("Mã bộ môn đã tồn tại trên hệ thống");
        }

        if (adminSubjectRepository.isExistsNameSubject(s.getName(), null)) {
            return RouterHelper.responseError("Tên bộ môn đã tồn tại trên hệ thống");
        }
        Subject saveSubject = adminSubjectRepository.save(s);
        userActivityLogHelper
                .saveLog("vừa thêm 1 bộ môn mới: " + saveSubject.getCode() + " - " + saveSubject.getName());

        // Invalidate all caches
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Thêm mới bộ môn thành công", saveSubject);
    }

    @Override
    public ResponseEntity<?> updateSubject(String id, ADSubjectUpdateRequest request) {


        Subject s = adminSubjectRepository.findById(id).orElse(null);
        if (s == null) {
            return RouterHelper.responseError("Không tìm thấy bộ môn");
        }

        if (!ValidateHelper.isValidCode(request.getCode())) {
            return RouterHelper.responseError(
                    "Mã bộ môn không hợp lệ:  không có khoảng trắng, không có ký tự đặc biệt ngoài dấu chấm . và dấu gạch dưới _.");
        }
        if (!ValidateHelper.isValidFullname(request.getName())) {
            return RouterHelper.responseError(
                    "Tên bộ môn không hợp lệ: Tối thiểu 2 từ, cách nhau bởi khoảng trắng và Chỉ gồm ký tự chữ không chứa số hay ký tự đặc biệt.");
        }
        s.setName(request.getName().trim());
        s.setCode(request.getCode().toUpperCase());

        if (adminSubjectRepository.isExistsCodeSubject(s.getCode(), s.getId())) {
            return RouterHelper.responseError("Mã bộ môn đã tồn tại trên hệ thống");
        }

        if (adminSubjectRepository.isExistsNameSubject(s.getName(), s.getId())) {
            return RouterHelper.responseError("Tên bộ môn đã tồn tại trên hệ thống");
        }

        Subject saveSubject = adminSubjectRepository.save(s);
        userActivityLogHelper
                .saveLog("vừa cập nhật 1 bộ môn: " + saveSubject.getCode() + " - " + saveSubject.getName());

        // Invalidate all caches
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Cập nhật bộ môn thành công", saveSubject);
    }

    @Override
    public ResponseEntity<?> detailSubject(String id) {
        Subject subject = getSubjectById(id);
        if (subject == null) {
            return RouterHelper.responseError("Không tìm thấy bộ môn");
        }

        return RouterHelper.responseSuccess("Lấy thông tin bộ môn thành công", subject);
    }

    @Override
    public ResponseEntity<?> changeStatus(String id) {
        Subject s = adminSubjectRepository.findById(id).orElse(null);
        if (s == null) {
            return RouterHelper.responseError("Không tìm thấy bộ môn");
        }

        if (commonPlanDateRepository.existsNotYetStartedBySubject(s.getId())) {
            return RouterHelper.responseError("Đang tồn tại ca chưa hoặc đang diễn ra. Không thể thay đổi trạng thái");
        }

        s.setStatus(s.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);

        Subject newEntity = adminSubjectRepository.save(s);

        userActivityLogHelper
                .saveLog("vừa thay đổi trạng thái 1 bộ môn : " + newEntity.getCode() + " - " + newEntity.getName());

        // Invalidate all caches
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Đổi trạng thái bộ môn thành công", newEntity);
    }

}
