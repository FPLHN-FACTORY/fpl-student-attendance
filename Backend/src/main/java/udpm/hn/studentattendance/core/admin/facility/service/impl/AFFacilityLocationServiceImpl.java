package udpm.hn.studentattendance.core.admin.facility.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFAddOrUpdateFacilityLocationRequest;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFFilterFacilityLocationRequest;
import udpm.hn.studentattendance.core.admin.facility.model.response.AFFacilityLocationResponse;
import udpm.hn.studentattendance.core.admin.facility.repository.AFFacilityExtendRepository;
import udpm.hn.studentattendance.core.admin.facility.repository.AFFacilityLocationRepository;
import udpm.hn.studentattendance.core.admin.facility.service.AFFacilityLocationService;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.FacilityLocation;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import com.fasterxml.jackson.core.type.TypeReference;
import udpm.hn.studentattendance.helpers.RedisCacheHelper;
import udpm.hn.studentattendance.helpers.RequestTrimHelper;

@Service
@RequiredArgsConstructor
public class AFFacilityLocationServiceImpl implements AFFacilityLocationService {
    private final AFFacilityExtendRepository afFacilityExtendRepository;

    private final AFFacilityLocationRepository afFacilityLocationRepository;

    private final UserActivityLogHelper userActivityLogHelper;

    private final RedisCacheHelper redisCacheHelper;

    private final RedisInvalidationHelper redisInvalidationHelper;

    public PageableObject<AFFacilityLocationResponse> getLocationList(AFFilterFacilityLocationRequest request) {
        String key = RedisPrefixConstant.REDIS_PREFIX_FACILITY_LOCATION + "list_" + request.toString();
        return redisCacheHelper.getOrSet(
                key,
                () -> PageableObject.of(
                        afFacilityLocationRepository.getAllByFilter(PaginationHelper.createPageable(request), request)),
                new TypeReference<>() {
                });
    }

    @Override
    public ResponseEntity<?> getAllList(AFFilterFacilityLocationRequest request) {
        PageableObject<AFFacilityLocationResponse> data = getLocationList(request);
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    @Override
    public ResponseEntity<?> addLocation(AFAddOrUpdateFacilityLocationRequest request) {

        RequestTrimHelper.trimStringFields(request);

        Facility facility = afFacilityExtendRepository.findById(request.getIdFacility()).orElse(null);

        if (facility == null || facility.getStatus() != EntityStatus.ACTIVE) {
            return RouterHelper.responseError("Không tìm cơ sở");
        }

        if (afFacilityLocationRepository.isExistsLocation(request.getName(), request.getIdFacility(), null)) {
            return RouterHelper.responseError(
                    "Tên địa điểm " + request.getName() + " đã tồn tại trong cơ sở " + facility.getName());
        }
        FacilityLocation facilityLocation = new FacilityLocation();
        facilityLocation.setFacility(facility);
        facilityLocation.setName(request.getName());
        facilityLocation.setLatitude(request.getLatitude());
        facilityLocation.setLongitude(request.getLongitude());
        facilityLocation.setRadius(request.getRadius());

        FacilityLocation savedLocation = afFacilityLocationRepository.save(facilityLocation);
        userActivityLogHelper
                .saveLog("vừa thêm địa điểm mới: " + savedLocation.getName() + " tại cơ sở " + facility.getName());

        // Invalidate all caches
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Tạo mới địa điểm thành công", savedLocation);
    }

    @Override
    public ResponseEntity<?> updateLocation(AFAddOrUpdateFacilityLocationRequest request) {

        RequestTrimHelper.trimStringFields(request);

        FacilityLocation facilityLocation = afFacilityLocationRepository.findById(request.getId()).orElse(null);
        if (facilityLocation == null) {
            return RouterHelper.responseError("Không tìm thấy địa điểm muốn cập nhật");
        }

        Facility facility = afFacilityExtendRepository.findById(request.getIdFacility()).orElse(null);

        if (facility == null || facility.getStatus() != EntityStatus.ACTIVE) {
            return RouterHelper.responseError("Không tìm cơ sở");
        }

        if (afFacilityLocationRepository.isExistsLocation(request.getName(), request.getIdFacility(),
                facilityLocation.getId())) {
            return RouterHelper.responseError(
                    "Tên địa điểm " + request.getName() + " đã tồn tại trong cơ sở " + facility.getName());
        }
        facilityLocation.setName(request.getName());
        facilityLocation.setLongitude(request.getLongitude());
        facilityLocation.setLatitude(request.getLatitude());
        facilityLocation.setRadius(request.getRadius());

        FacilityLocation savedLocation = afFacilityLocationRepository.save(facilityLocation);
        userActivityLogHelper.saveLog("vừa cập nhật địa điểm: " + savedLocation.getName() + " tại cơ sở "
                + savedLocation.getFacility().getName());

        // Invalidate all caches
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Cập nhật địa điểm thành công", savedLocation);
    }

    @Override
    public ResponseEntity<?> deleteLocation(String id) {
        FacilityLocation facilityLocation = afFacilityLocationRepository.findById(id).orElse(null);
        if (facilityLocation == null) {
            return RouterHelper.responseError("Không tìm thấy địa điểm");
        }

        String locationName = facilityLocation.getName();
        String facilityName = facilityLocation.getFacility().getName();
        afFacilityLocationRepository.delete(facilityLocation);
        userActivityLogHelper.saveLog("vừa xóa địa điểm: " + locationName + " tại cơ sở " + facilityName);

        // Invalidate all caches
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Xoá thành công địa điểm: " + facilityLocation.getName());
    }

    @Override
    public ResponseEntity<?> changeStatus(String id) {
        FacilityLocation facilityLocation = afFacilityLocationRepository.findById(id).orElse(null);
        if (facilityLocation == null) {
            return RouterHelper.responseError("Không tìm thấy địa điểm");
        }

        if (facilityLocation.getStatus() == EntityStatus.INACTIVE && afFacilityLocationRepository.isExistsLocation(
                facilityLocation.getName(), facilityLocation.getFacility().getId(), facilityLocation.getId())) {
            return RouterHelper
                    .responseError("Địa điểm " + facilityLocation.getName() + " đã được áp dụng trong cơ sở");
        }

        facilityLocation.setStatus(
                facilityLocation.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        FacilityLocation savedLocation = afFacilityLocationRepository.save(facilityLocation);

        String statusText = savedLocation.getStatus() == EntityStatus.ACTIVE ? "Hoạt động" : "Không hoạt động";
        userActivityLogHelper.saveLog("vừa thay đổi trạng thái địa điểm: " + savedLocation.getName() +
                " tại cơ sở " + savedLocation.getFacility().getName() + " thành " + statusText);

        // Invalidate all caches
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Thay đổi trạng thái địa điểm thành công", savedLocation);
    }

}
