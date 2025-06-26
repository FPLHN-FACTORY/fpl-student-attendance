package udpm.hn.studentattendance.core.admin.subject.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.admin.subject.model.request.ADSubjectCreateRequest;
import udpm.hn.studentattendance.core.admin.subject.model.request.ADSubjectSearchRequest;
import udpm.hn.studentattendance.core.admin.subject.model.request.ADSubjectUpdateRequest;
import udpm.hn.studentattendance.core.admin.subject.repository.ADSubjectExtendRepository;
import udpm.hn.studentattendance.core.admin.subject.service.ADSubjectManagementService;
import udpm.hn.studentattendance.entities.Subject;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.helpers.ValidateHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonUserStudentRepository;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;

@Service
@RequiredArgsConstructor
public class ADSubjectManagementServiceImpl implements ADSubjectManagementService {

    private final ADSubjectExtendRepository adminSubjectRepository;

    private final CommonUserStudentRepository commonUserStudentRepository;

    private final UserActivityLogHelper userActivityLogHelper;

    private final RedisService redisService;

    @Value("${spring.cache.redis.time-to-live:3600}")
    private long redisTTL;

    @Override
    public ResponseEntity<?> getListSubject(ADSubjectSearchRequest request) {
        String cacheKey = "admin:subject:list:" + request.toString();

        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            return RouterHelper.responseSuccess("Lấy danh sách bộ môn thành công (cached)", cachedData);
        }

        Pageable pageable = PaginationHelper.createPageable(request, "id");
        PageableObject result = PageableObject.of(adminSubjectRepository.getAll(pageable, request));

        redisService.set(cacheKey, result, redisTTL * 2); // Cache lâu hơn vì danh sách bộ môn ít thay đổi

        return RouterHelper.responseSuccess("Lấy danh sách bộ môn thành công", result);
    }

    @Override
    public ResponseEntity<?> createSubject(ADSubjectCreateRequest request) {
        if (!ValidateHelper.isValidCode(request.getCode())) {
            return RouterHelper.responseError(
                    "Mã bộ môn không hợp lệ: không có khoảng trắng, không có ký tự đặc biệt ngoài dấu chấm . và dấu gạch dưới _.");
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

        invalidateSubjectListCache();

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

        invalidateSubjectCache(id);

        return RouterHelper.responseSuccess("Cập nhật bộ môn thành công", saveSubject);
    }

    @Override
    public ResponseEntity<?> detailSubject(String id) {
        String cacheKey = "admin:subject:detail:" + id;

        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            return RouterHelper.responseSuccess("Lấy thông tin bộ môn thành công (cached)", cachedData);
        }

        Subject s = adminSubjectRepository.findById(id).orElse(null);
        if (s == null) {
            return RouterHelper.responseError("Không tìm thấy bộ môn");
        }

        redisService.set(cacheKey, s, redisTTL * 3); // Cache lâu hơn vì thông tin bộ môn ít thay đổi

        return RouterHelper.responseSuccess("Lấy thông tin bộ môn thành công", s);
    }

    @Override
    public ResponseEntity<?> changeStatus(String id) {
        Subject s = adminSubjectRepository.findById(id).orElse(null);
        if (s == null) {
            return RouterHelper.responseError("Không tìm thấy bộ môn");
        }
        s.setStatus(s.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);

        Subject newEntity = adminSubjectRepository.save(s);

        if (s.getStatus() == EntityStatus.ACTIVE) {
            commonUserStudentRepository.disableAllStudentDuplicateShiftByIdSubject(s.getId());
        }
        userActivityLogHelper
                .saveLog("vừa thay đổi trạng thái 1 bộ môn : " + newEntity.getCode() + " - " + newEntity.getName());

        invalidateSubjectCache(id);

        return RouterHelper.responseSuccess("Đổi trạng thái bộ môn thành công", newEntity);
    }

    /**
     * Xóa cache liên quan đến một bộ môn cụ thể
     */
    private void invalidateSubjectCache(String subjectId) {
        redisService.delete("admin:subject:detail:" + subjectId);
        invalidateSubjectListCache();
    }

    /**
     * Xóa cache danh sách bộ môn
     */
    private void invalidateSubjectListCache() {
        redisService.deletePattern("admin:subject:list:*");
    }
}
