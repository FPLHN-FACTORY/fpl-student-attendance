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
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.helpers.ValidateHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonUserStudentRepository;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;

@Service
@RequiredArgsConstructor
public class ADSubjectManagementServiceImpl implements ADSubjectManagementService {

    private final ADSubjectExtendRepository adminSubjectRepository;

    private final CommonUserStudentRepository commonUserStudentRepository;

    private final UserActivityLogHelper userActivityLogHelper;

    private final RedisService redisService;
    
    private final RedisInvalidationHelper redisInvalidationHelper;

    @Value("${spring.cache.redis.time-to-live}")
    private long redisTTL;

    // Phương thức helper để lấy danh sách bộ môn từ cache hoặc DB
    public PageableObject getSubjects(ADSubjectSearchRequest request) {
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_SUBJECT + "list_" +
                "page=" + request.getPage() +
                "_size=" + request.getSize() +
                "_orderBy=" + request.getOrderBy() +
                "_sortBy=" + request.getSortBy() +
                "_q=" + (request.getQ() != null ? request.getQ() : "") +
                "_name=" + (request.getName() != null ? request.getName() : "") +
                "_status=" + (request.getStatus() != null ? request.getStatus() : "");

        // Kiểm tra cache
        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            try {
                return redisService.getObject(cacheKey, PageableObject.class);
            } catch (Exception e) {
                redisService.delete(cacheKey);
            }
        }

        // Cache miss - fetch from database
        Pageable pageable = PaginationHelper.createPageable(request, "id");
        PageableObject result = PageableObject.of(adminSubjectRepository.getAll(pageable, request));

        // Store in cache
        try {
            redisService.set(cacheKey, result, redisTTL);
        } catch (Exception ignored) {
        }

        return result;
    }

    // Phương thức helper để lấy thông tin chi tiết bộ môn từ cache hoặc DB
    public Subject getSubjectById(String id) {
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_SUBJECT + id;

        // Kiểm tra cache
        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            try {
                return redisService.getObject(cacheKey, Subject.class);
            } catch (Exception e) {
                redisService.delete(cacheKey);
            }
        }

        // Cache miss - fetch from database
        Subject subject = adminSubjectRepository.findById(id).orElse(null);

        // Store in cache if found
        if (subject != null) {
            try {
                redisService.set(cacheKey, subject, redisTTL);
            } catch (Exception ignored) {
            }
        }

        return subject;
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
        s.setStatus(s.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);

        Subject newEntity = adminSubjectRepository.save(s);

        if (s.getStatus() == EntityStatus.ACTIVE) {
            commonUserStudentRepository.disableAllStudentDuplicateShiftByIdSubject(s.getId());
        }
        userActivityLogHelper
                .saveLog("vừa thay đổi trạng thái 1 bộ môn : " + newEntity.getCode() + " - " + newEntity.getName());

        // Invalidate all caches
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Đổi trạng thái bộ môn thành công", newEntity);
    }

    /**
     * @deprecated Use redisInvalidationHelper.invalidateAllCaches() instead
     */
    private void invalidateSubjectCache(String subjectId) {
        redisInvalidationHelper.invalidateAllCaches();
    }

    /**
     * @deprecated Use redisInvalidationHelper.invalidateAllCaches() instead
     */
    private void invalidateSubjectListCache() {
        redisInvalidationHelper.invalidateAllCaches();
    }
}
