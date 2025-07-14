package udpm.hn.studentattendance.core.admin.subjectfacility.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.ADSubjectFacilityCreateRequest;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.ADSubjectFacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.ADSubjectFacilityUpdateRequest;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.response.ADSubjectFacilityResponse;
import udpm.hn.studentattendance.core.admin.subjectfacility.repository.ADFacilityRepository;
import udpm.hn.studentattendance.core.admin.subjectfacility.repository.ADSubjectFacilityRepository;
import udpm.hn.studentattendance.core.admin.subjectfacility.repository.ADSubjectRepository;
import udpm.hn.studentattendance.core.admin.subjectfacility.service.ADSubjectFacilityService;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.Subject;
import udpm.hn.studentattendance.entities.SubjectFacility;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.RequestTrimHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonUserStudentRepository;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;
import udpm.hn.studentattendance.repositories.FacilityRepository;
import udpm.hn.studentattendance.repositories.SubjectRepository;

@Service
@RequiredArgsConstructor
public class ADSubjectFacilityServiceImpl implements ADSubjectFacilityService {

    private final ADSubjectFacilityRepository repository;

    private final ADSubjectRepository subjectRepository;

    private final ADFacilityRepository facilityRepository;

    private final CommonUserStudentRepository commonUserStudentRepository;

    private final UserActivityLogHelper userActivityLogHelper;

    private final RedisService redisService;

    private final RedisInvalidationHelper redisInvalidationHelper;

    @Value("${spring.cache.redis.time-to-live}")
    private long redisTTL;

    public PageableObject<ADSubjectFacilityResponse> getSubjectFacilityList(ADSubjectFacilitySearchRequest request) {
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_SUBJECT_FACILITY + "list_" +
                "page=" + request.getPage() +
                "_size=" + request.getSize() +
                "_orderBy=" + request.getOrderBy() +
                "_sortBy=" + request.getSortBy() +
                "_q=" + (request.getQ() != null ? request.getQ() : "") +
                "_name=" + (request.getName() != null ? request.getName() : "") +
                "_facilityId=" + (request.getFacilityId() != null ? request.getFacilityId() : "") +
                "_subjectId=" + (request.getSubjectId() != null ? request.getSubjectId() : "") +
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
        PageableObject<ADSubjectFacilityResponse> result = PageableObject.of(repository.getAll(pageable, request));

        // Store in cache
        try {
            redisService.set(cacheKey, result, redisTTL);
        } catch (Exception ignored) {
        }

        return result;
    }

    /**
     * Lấy thông tin chi tiết bộ môn cơ sở từ cache hoặc DB
     */
    public ADSubjectFacilityResponse getSubjectFacilityDetail(String id) {
        return repository.getOneById(id).orElse(null);
    }

    @Override
    public ResponseEntity<?> getListSubjectFacility(ADSubjectFacilitySearchRequest request) {
        PageableObject<ADSubjectFacilityResponse> result = getSubjectFacilityList(request);
        return RouterHelper.responseSuccess("Lây danh sách bộ môn cơ sở thành công", result);
    }

    @Override
    public ResponseEntity<?> createSubjectFacility(ADSubjectFacilityCreateRequest request) {
        // Trim all string fields in the request
        RequestTrimHelper.trimStringFields(request);

        Facility facility = facilityRepository.findById(request.getFacilityId()).orElse(null);
        if (facility == null || facility.getStatus() == EntityStatus.INACTIVE) {
            return RouterHelper.responseError("Không tìm thấy cơ sở");
        }

        Subject subject = subjectRepository.findById(request.getSubjectId()).orElse(null);
        if (subject == null || subject.getStatus() == EntityStatus.INACTIVE) {
            return RouterHelper.responseError("Không tìm thấy bộ môn");
        }

        if (repository.isExistsSubjectFacility(facility.getId(), subject.getId(), null)) {
            return RouterHelper.responseError("Bộ môn đã tồn tại trong cơ sở này");
        }
        SubjectFacility subjectFacility = new SubjectFacility();
        subjectFacility.setFacility(facility);
        subjectFacility.setSubject(subject);

        SubjectFacility savedSubjectFacility = repository.save(subjectFacility);
        userActivityLogHelper
                .saveLog("vừa thêm mới bộ môn cơ sở : " + subject.getName() + " tại cơ sở " + facility.getName());

        // Invalidate all caches
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Thêm bộ môn cơ sở thành công", savedSubjectFacility);
    }

    @Override
    public ResponseEntity<?> updateSubjectFacility(String id, ADSubjectFacilityUpdateRequest request) {
        // Trim all string fields in the request
        RequestTrimHelper.trimStringFields(request);

        SubjectFacility subjectFacility = repository.findById(id).orElse(null);
        if (subjectFacility == null) {
            return RouterHelper.responseError("Không tìm thấy bộ môn cơ sở");
        }

        Facility facility = facilityRepository.findById(request.getFacilityId()).orElse(null);
        if (facility == null || facility.getStatus() == EntityStatus.INACTIVE) {
            return RouterHelper.responseError("Không tìm thấy cơ sở");
        }

        Subject subject = subjectRepository.findById(request.getSubjectId()).orElse(null);
        if (subject == null || subject.getStatus() == EntityStatus.INACTIVE) {
            return RouterHelper.responseError("Không tìm thấy bộ môn");
        }

        if (repository.isExistsSubjectFacility(facility.getId(), subject.getId(), subjectFacility.getId())) {
            return RouterHelper.responseError("Bộ môn đã tồn tại trong cơ sở này");
        }
        subjectFacility.setFacility(facility);
        subjectFacility.setSubject(subject);

        SubjectFacility savedSubjectFacility = repository.save(subjectFacility);
        userActivityLogHelper
                .saveLog("vừa cập nhật bộ môn cơ sở: " + subject.getName() + " tại cơ sở " + facility.getName());

        // Invalidate all caches
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Cập nhật bộ môn cơ sở thành công", savedSubjectFacility);
    }

    @Override
    public ResponseEntity<?> detailSubjectFacility(String id) {
        ADSubjectFacilityResponse subjectFacility = getSubjectFacilityDetail(id);
        if (subjectFacility == null) {
            return RouterHelper.responseError("Không tìm thấy bộ môn cơ sở");
        }
        return RouterHelper.responseSuccess("Lấy thông tin bộ môn cơ sở thành công", subjectFacility);
    }

    @Override
    public ResponseEntity<?> changeStatus(String id) {
        SubjectFacility subjectFacility = repository.findById(id).orElse(null);
        if (subjectFacility == null) {
            return RouterHelper.responseError("Không tìm thấy bộ môn cơ sở");
        }
        subjectFacility.setStatus(
                subjectFacility.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);

        SubjectFacility newEntity = repository.save(subjectFacility);

        if (subjectFacility.getStatus() == EntityStatus.ACTIVE) {
            commonUserStudentRepository.disableAllStudentDuplicateShiftByIdSubjectFacility(subjectFacility.getId());
        }
        String statusText = newEntity.getStatus() == EntityStatus.ACTIVE ? "Hoạt động" : "Không hoạt động";
        userActivityLogHelper.saveLog("vừa thay đổi trạng thái bộ môn cơ sở: " + newEntity.getSubject().getName()
                + " tại cơ sở " + newEntity.getFacility().getName() + " thành " + statusText);

        // Invalidate all caches
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Thay đổi trạng thái thành công", newEntity);
    }

}
