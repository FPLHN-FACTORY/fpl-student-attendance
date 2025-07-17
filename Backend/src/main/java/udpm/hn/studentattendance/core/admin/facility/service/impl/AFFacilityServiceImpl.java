package udpm.hn.studentattendance.core.admin.facility.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFCreateUpdateFacilityRequest;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFFacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.facility.model.response.AFFacilityResponse;
import udpm.hn.studentattendance.core.admin.facility.repository.AFFacilityExtendRepository;
import udpm.hn.studentattendance.core.admin.facility.service.AFFacilityService;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.helpers.GenerateNameHelper;
import udpm.hn.studentattendance.helpers.MailerHelper;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonUserStudentRepository;
import udpm.hn.studentattendance.infrastructure.config.mailer.model.MailerDefaultRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import com.fasterxml.jackson.core.type.TypeReference;
import udpm.hn.studentattendance.helpers.RedisCacheHelper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Validated
public class AFFacilityServiceImpl implements AFFacilityService {

    private final AFFacilityExtendRepository facilityRepository;

    private final CommonUserStudentRepository commonUserStudentRepository;

    private final MailerHelper mailerHelper;

    private final UserActivityLogHelper userActivityLogHelper;

    private final RedisCacheHelper redisCacheHelper;

    private final RedisInvalidationHelper redisInvalidationHelper;

    @Value("${app.config.app-name}")
    private String appName;

    public PageableObject<AFFacilityResponse> getCachedFacilities(AFFacilitySearchRequest request) {
        String key = RedisPrefixConstant.REDIS_PREFIX_FACILITY + "list_" + request.toString();
        return redisCacheHelper.getOrSet(
                key,
                () -> PageableObject.of(facilityRepository
                        .getAllFacility(PaginationHelper.createPageable(request, "createdAt"), request)),
                new TypeReference<>() {
                });
    }

    @Override
    public ResponseEntity<?> getAllFacility(AFFacilitySearchRequest request) {
        PageableObject<AFFacilityResponse> facilities = getCachedFacilities(request);
        return RouterHelper.responseSuccess("Lấy tất cả cơ sở thành công", facilities);
    }

    @Override
    public ResponseEntity<?> createFacility(AFCreateUpdateFacilityRequest request) {

        Optional<Facility> existFacility = facilityRepository.findByName(request.getFacilityName());
        if (existFacility.isPresent()) {
            return RouterHelper.responseError("Tên cơ sở đã tồn tại trên hệ thống");
        }
        String code = GenerateNameHelper.generateCodeFromName(request.getFacilityName());
        int position = facilityRepository.getLastPosition() + 1;
        Facility facility = new Facility();
        facility.setCode(code);
        facility.setName(GenerateNameHelper.replaceManySpaceToOneSpace(request.getFacilityName()));
        facility.setCreatedAt(System.currentTimeMillis());
        facility.setPosition(position);
        facility.setStatus(EntityStatus.ACTIVE);
        facilityRepository.save(facility);
        userActivityLogHelper.saveLog("vừa thêm 1 cơ sở mới: " + facility.getName());

        // Invalidate all caches
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Thêm cơ sở mới thành công", null);
    }

    @Override
    public ResponseEntity<?> updateFacility(String facilityId, AFCreateUpdateFacilityRequest request) {

        Optional<Facility> existFacility = facilityRepository.findById(facilityId);
        if (existFacility.isEmpty()) {
            return RouterHelper.responseError("Không tìm thấy cơ sở");
        }

        if (facilityRepository.isExistsByName(request.getFacilityName(), facilityId)) {
            return RouterHelper.responseError("Tên cơ sở đã tồn tại trên hệ thống");
        }

        Facility facility = existFacility.get();
        String oldName = facility.getName();
        facility.setCode(GenerateNameHelper.generateCodeFromName(request.getFacilityName()));
        facility.setName(GenerateNameHelper.replaceManySpaceToOneSpace(request.getFacilityName()));

        Facility savedFacility = facilityRepository.save(facility);
        userActivityLogHelper.saveLog("vừa cập nhật cơ sở: " + oldName + " → " + savedFacility.getName());

        // Invalidate all caches
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Cập nhật cơ sở thành công", savedFacility);
    }

    @Override
    public ResponseEntity<?> changeFacilityStatus(String facilityId) {
        Optional<Facility> facilityOptional = facilityRepository.findById(facilityId);

        if (facilityOptional.isEmpty()) {
            return RouterHelper.responseError("Không tìm thấy cơ sở");
        }

        Facility facility = facilityOptional.get();

        long lastUpdatedMillis = facility.getUpdatedAt(); // epoch millis
        LocalDate lastUpdatedDate = Instant
                .ofEpochMilli(lastUpdatedMillis)
                .atZone(ZoneId.of("Asia/Ho_Chi_Minh")) // dùng timezone phù hợp
                .toLocalDate();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        if (lastUpdatedDate.isEqual(today) && facility.getStatus() == EntityStatus.ACTIVE) {
            return RouterHelper.responseError("Chỉ được đổi trạng thái cơ sở ngừng hoạt động 1 lần mỗi ngày");
        }

        if (facilityRepository.countFacility() < 2) {
            return RouterHelper.responseError("Không thể thay đổi trạng thái cơ sở cuối cùng còn hoạt động");
        }

        String oldStatus = facility.getStatus() == EntityStatus.ACTIVE ? "Hoạt động" : "Không hoạt động";
        facility.setStatus(facility.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        String newStatus = facility.getStatus() == EntityStatus.ACTIVE ? "Hoạt động" : "Không hoạt động";
        Facility entity = facilityRepository.save(facility);

        if (entity.getStatus() == EntityStatus.INACTIVE) {
            List<String> lstEmails = facilityRepository.getListEmailUserDisableFacility(entity.getId());
            if (!lstEmails.isEmpty()) {
                String to = lstEmails.remove(0);
                MailerDefaultRequest mailerDefaultRequest = new MailerDefaultRequest();
                mailerDefaultRequest.setTo(to);
                if (!lstEmails.isEmpty()) {
                    mailerDefaultRequest.setBcc(lstEmails.toArray(new String[0]));
                }

                mailerDefaultRequest.setTemplate(null);
                mailerDefaultRequest.setTitle("Thông báo quan trọng từ " + appName);
                mailerDefaultRequest.setContent(MailerHelper.loadTemplate(MailerHelper.TEMPLATE_CHANGE_STATUS_FACILITY,
                        Map.of("FACILITY_NAME", entity.getName())));
                mailerHelper.send(mailerDefaultRequest);
            }
        } else {
            commonUserStudentRepository.disableAllStudentDuplicateShiftByIdFacility(facility.getId());
        }

        userActivityLogHelper.saveLog(
                "vừa thay đổi trạng thái cơ sở " + facility.getName() + " từ " + oldStatus + " thành " + newStatus);

        // Invalidate all caches
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Thay đổi trạng thái cơ sở thành công", entity);
    }

    public AFFacilityResponse getCachedFacilityById(String facilityId) {
        Optional<AFFacilityResponse> facility = facilityRepository.getDetailFacilityById(facilityId);
        return facility.orElse(null);
    }

    @Override
    public ResponseEntity<?> getFacilityById(String facilityId) {
        AFFacilityResponse facility = getCachedFacilityById(facilityId);
        if (facility != null) {
            return RouterHelper.responseSuccess("Hiển thị chi tiết cơ sở thành công", facility);
        } else {
            return RouterHelper.responseError("Cơ sở không tồn tại");
        }
    }

    @Override
    public ResponseEntity<?> up(String facilityId) {
        Facility facility = facilityRepository.findById(facilityId).orElse(null);
        if (facility == null) {
            return RouterHelper.responseError("Không tìm thấy cơ sở");
        }

        if (facility.getPosition() <= 1) {
            return RouterHelper.responseError("Cơ sở đã đang ở mức ưu tiên hiển thị cao nhất");
        }

        facility.setPosition(Math.max(1, facility.getPosition() - 1));
        facilityRepository.updatePositionPreUp(facility.getPosition(), facilityId);
        Facility updated = facilityRepository.save(facility);

        // Invalidate all caches
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Tăng mức ưu tiên hiển thị thành công", updated);
    }

    @Override
    public ResponseEntity<?> down(String facilityId) {
        Facility facility = facilityRepository.findById(facilityId).orElse(null);
        if (facility == null) {
            return RouterHelper.responseError("Không tìm thấy cơ sở");
        }
        int maxPosition = facilityRepository.getLastPosition();
        if (facility.getPosition() >= maxPosition) {
            return RouterHelper.responseError("Cơ sở đã đang ở mức ưu tiên hiển thị thấp nhất");
        }

        facility.setPosition(Math.min(maxPosition, facility.getPosition() + 1));
        facilityRepository.updatePositionNextDown(facility.getPosition(), facilityId);
        Facility updated = facilityRepository.save(facility);

        // Invalidate all caches
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Giảm mức ưu tiên hiển thị thành công", updated);
    }

}
