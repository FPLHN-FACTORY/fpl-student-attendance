package udpm.hn.studentattendance.core.admin.facility.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFAddOrUpdateFacilityShiftRequest;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFFilterFacilityShiftRequest;
import udpm.hn.studentattendance.core.admin.facility.model.response.AFFacilityShiftResponse;
import udpm.hn.studentattendance.core.admin.facility.repository.AFFacilityExtendRepository;
import udpm.hn.studentattendance.core.admin.facility.repository.AFFacilityShiftRepository;
import udpm.hn.studentattendance.core.admin.facility.service.AFFacilityShiftService;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.FacilityShift;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SettingHelper;
import udpm.hn.studentattendance.helpers.ShiftHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import udpm.hn.studentattendance.infrastructure.constants.SettingKeys;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;

@Service
@RequiredArgsConstructor
public class AFFacilityShiftServiceImpl implements AFFacilityShiftService {

    private final AFFacilityExtendRepository afFacilityExtendRepository;

    private final AFFacilityShiftRepository afFacilityShiftRepository;

    private final UserActivityLogHelper userActivityLogHelper;

    private final SettingHelper settingHelper;

    private final RedisService redisService;

    private final RedisInvalidationHelper redisInvalidationHelper;

    @Value("${spring.cache.redis.time-to-live}")
    private long redisTTL;

    private int MIN_DIFF_SHIFT;

    @PostConstruct
    public void init() {
        try {
            Integer minDiff = settingHelper.getSetting(SettingKeys.SHIFT_MIN_DIFF, Integer.class);
            this.MIN_DIFF_SHIFT = minDiff != null ? minDiff : 10; // fallback to 10 minutes
        } catch (Exception e) {
            // If database is not ready or settings table doesn't exist, use default value
            this.MIN_DIFF_SHIFT = 10; // default to 10 minutes
        }
    }

    public PageableObject<AFFacilityShiftResponse> getShiftList(AFFilterFacilityShiftRequest request) {
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_FACILITY_SHIFT + "list_" +
                "page=" + request.getPage() +
                "_size=" + request.getSize() +
                "_orderBy=" + request.getOrderBy() +
                "_sortBy=" + request.getSortBy() +
                "_q=" + (request.getQ() != null ? request.getQ() : "") +
                "_idFacility=" + (request.getIdFacility() != null ? request.getIdFacility() : "") +
                "_shift=" + (request.getShift() != null ? request.getShift() : "") +
                "_status=" + (request.getStatus() != null ? request.getStatus() : "");

        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            try {
                return redisService.getObject(cacheKey, PageableObject.class);
            } catch (Exception e) {
                redisService.delete(cacheKey);
            }
        }

        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject<AFFacilityShiftResponse> data = PageableObject
                .of(afFacilityShiftRepository.getAllByFilter(pageable, request));

        try {
            redisService.set(cacheKey, data, redisTTL);
        } catch (Exception ignored) {
        }

        return data;
    }

    @Override
    public ResponseEntity<?> getAllList(AFFilterFacilityShiftRequest request) {
        PageableObject<AFFacilityShiftResponse> data = getShiftList(request);
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    @Override
    public ResponseEntity<?> addShift(AFAddOrUpdateFacilityShiftRequest request) {

        Facility facility = afFacilityExtendRepository.findById(request.getIdFacility()).orElse(null);

        if (facility == null || facility.getStatus() != EntityStatus.ACTIVE) {
            return RouterHelper.responseError("Không tìm cơ sở");
        }

        int diffTime = ShiftHelper.getDiffTime(request.getFromHour(), request.getFromMinute(), request.getToHour(),
                request.getToMinute());
        if (diffTime / 1000 < MIN_DIFF_SHIFT * 60) {
            return RouterHelper.responseError("Ca học phải diễn ra tối thiểu trong " + MIN_DIFF_SHIFT + " phút");
        }

        if (afFacilityShiftRepository.isExistsShift(request.getShift(), request.getIdFacility(), null)) {
            return RouterHelper
                    .responseError("Ca " + request.getShift() + " đã tồn tại trong cơ sở " + facility.getName());
        }

        if (afFacilityShiftRepository.isExistsTime(request.getIdFacility(), request.getFromHour(),
                request.getFromMinute(), request.getToHour(), request.getToMinute(), null)) {
            return RouterHelper.responseError("Thời gian học " + request.getFromHour() + ":" + request.getFromMinute()
                    + " đến " + request.getToHour() + ":" + request.getToMinute() + " đã tồn tại trong cơ sở "
                    + facility.getName());
        }

        FacilityShift facilityShift = new FacilityShift();
        facilityShift.setFacility(facility);
        facilityShift.setShift(request.getShift());
        facilityShift.setFromHour(request.getFromHour());
        facilityShift.setFromMinute(request.getFromMinute());
        facilityShift.setToHour(request.getToHour());
        facilityShift.setToMinute(request.getToMinute());
        FacilityShift saveFacilityShift = afFacilityShiftRepository.save(facilityShift);

        // Log user activity
        userActivityLogHelper.saveLog("Tạo ca học mới: " + request.getShift() + " (" + request.getFromHour() + ":"
                + request.getFromMinute() +
                " - " + request.getToHour() + ":" + request.getToMinute() + ") tại cơ sở " + facility.getName());

        // Invalidate all caches
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Tạo mới ca học thành công", saveFacilityShift);
    }

    @Override
    public ResponseEntity<?> updateShift(AFAddOrUpdateFacilityShiftRequest request) {
        FacilityShift facilityShift = afFacilityShiftRepository.findById(request.getId()).orElse(null);
        if (facilityShift == null) {
            return RouterHelper.responseError("Không tìm thấy ca học muốn cập nhật");
        }

        int diffTime = ShiftHelper.getDiffTime(request.getFromHour(), request.getFromMinute(), request.getToHour(),
                request.getToMinute());
        if (diffTime / 1000 < MIN_DIFF_SHIFT * 60) {
            return RouterHelper.responseError("Ca học phải diễn ra tối thiểu trong " + MIN_DIFF_SHIFT + " phút");
        }

        Facility facility = afFacilityExtendRepository.findById(request.getIdFacility()).orElse(null);

        if (facility == null || facility.getStatus() != EntityStatus.ACTIVE) {
            return RouterHelper.responseError("Không tìm cơ sở");
        }

        if (afFacilityShiftRepository.isExistsShift(request.getShift(), request.getIdFacility(),
                facilityShift.getId())) {
            return RouterHelper
                    .responseError("Ca " + request.getShift() + " đã tồn tại trong cơ sở " + facility.getName());
        }

        if (afFacilityShiftRepository.isExistsTime(request.getIdFacility(), request.getFromHour(),
                request.getFromMinute(), request.getToHour(), request.getToMinute(), facilityShift.getId())) {
            return RouterHelper.responseError("Thời gian học " + request.getFromHour() + ":" + request.getFromMinute()
                    + " đến " + request.getToHour() + ":" + request.getToMinute() + " đã tồn tại trong cơ sở "
                    + facility.getName());
        }
        String oldShiftInfo = facilityShift.getShift() + " (" + facilityShift.getFromHour() + ":"
                + facilityShift.getFromMinute() +
                " - " + facilityShift.getToHour() + ":" + facilityShift.getToMinute() + ")";

        facilityShift.setShift(request.getShift());
        facilityShift.setFromHour(request.getFromHour());
        facilityShift.setFromMinute(request.getFromMinute());
        facilityShift.setToHour(request.getToHour());
        facilityShift.setToMinute(request.getToMinute());
        FacilityShift saveFacilityShift = afFacilityShiftRepository.save(facilityShift);

        // Log user activity
        userActivityLogHelper.saveLog("Cập nhật ca học từ: " + oldShiftInfo + " thành: " + request.getShift() + " (" +
                request.getFromHour() + ":" + request.getFromMinute() + " - " + request.getToHour() + ":"
                + request.getToMinute() +
                ") tại cơ sở " + facility.getName());

        // Invalidate all caches
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Cập nhật ca học thành công", saveFacilityShift);
    }

    @Override
    public ResponseEntity<?> deleteShift(String id) {
        FacilityShift facilityShift = afFacilityShiftRepository.findById(id).orElse(null);
        if (facilityShift == null) {
            return RouterHelper.responseError("Không tìm thấy ca học");
        }
        String shiftInfo = facilityShift.getShift() + " (" + facilityShift.getFromHour() + ":"
                + facilityShift.getFromMinute() +
                " - " + facilityShift.getToHour() + ":" + facilityShift.getToMinute() + ")";
        String facilityName = facilityShift.getFacility().getName();

        afFacilityShiftRepository.delete(facilityShift);

        // Log user activity
        userActivityLogHelper.saveLog("Xóa ca học: " + shiftInfo + " tại cơ sở " + facilityName);

        // Invalidate all caches
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Xoá thành công ca học: " + facilityShift.getShift());
    }

    @Override
    public ResponseEntity<?> changeStatus(String id) {
        FacilityShift facilityShift = afFacilityShiftRepository.findById(id).orElse(null);
        if (facilityShift == null) {
            return RouterHelper.responseError("Không tìm thấy địa điểm");
        }
        if (facilityShift.getStatus() == EntityStatus.INACTIVE && afFacilityShiftRepository
                .isExistsShift(facilityShift.getShift(), facilityShift.getFacility().getId(), facilityShift.getId())) {
            return RouterHelper.responseError("Ca " + facilityShift.getShift() + " đã được áp dụng trong cơ sở");
        }

        EntityStatus oldStatus = facilityShift.getStatus();
        facilityShift.setStatus(
                facilityShift.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        FacilityShift savedShift = afFacilityShiftRepository.save(facilityShift);

        // Log user activity
        String statusChange = oldStatus == EntityStatus.ACTIVE ? "Ngừng hoạt động" : "Kích hoạt";
        userActivityLogHelper.saveLog(statusChange + " ca học: " + facilityShift.getShift() + " (" +
                facilityShift.getFromHour() + ":" + facilityShift.getFromMinute() + " - " +
                facilityShift.getToHour() + ":" + facilityShift.getToMinute() + ") tại cơ sở "
                + facilityShift.getFacility().getName());

        // Invalidate all caches
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Thay đổi trạng thái ca học thành công", savedShift);
    }

    /**
     * @deprecated Use redisInvalidationHelper.invalidateAllCaches() instead
     */
    private void invalidateShiftCaches() {
        redisInvalidationHelper.invalidateAllCaches();
    }
}
