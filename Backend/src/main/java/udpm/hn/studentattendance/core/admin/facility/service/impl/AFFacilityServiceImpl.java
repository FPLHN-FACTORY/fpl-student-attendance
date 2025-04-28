package udpm.hn.studentattendance.core.admin.facility.service.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFCreateUpdateFacilityRequest;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFFacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.facility.repository.AFFacilityExtendRepository;
import udpm.hn.studentattendance.core.admin.facility.service.AFFacilityService;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.helpers.GenerateNameHelper;
import udpm.hn.studentattendance.helpers.MailerHelper;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonUserStudentRepository;
import udpm.hn.studentattendance.infrastructure.config.mailer.model.MailerDefaultRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


@Service
@RequiredArgsConstructor
@Validated
public class AFFacilityServiceImpl implements AFFacilityService {

    private final AFFacilityExtendRepository facilityRepository;

    private final CommonUserStudentRepository commonUserStudentRepository;

    private final MailerHelper mailerHelper;

    @Value("${app.config.app-name}")
    private String appName;

    @Override
    public ResponseEntity<?> getAllFacility(AFFacilitySearchRequest request) {
        Pageable pageable = PaginationHelper.createPageable(request, "createdAt");
        PageableObject facilities = PageableObject.of(facilityRepository.getAllFacility(pageable, request));
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy tất cả cơ sở thành công",
                        facilities
                ), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> createFacility(AFCreateUpdateFacilityRequest request) {
        Optional<Facility> existFacility = facilityRepository.findByName(request.getFacilityName().trim());
        if (existFacility.isPresent()) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.WARNING,
                            "Cơ sở đã tồn tại",
                            null
                    ),
                    HttpStatus.BAD_REQUEST);
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
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Thêm cơ sở mới thành công",
                        null
                ),
                HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<?> updateFacility(String facilityId, AFCreateUpdateFacilityRequest request) {
//        if (!facilityRepository.existsByNameAndId(request.getFacilityName(), facilityId)) {
//            return new ResponseEntity<>(
//                    new ApiResponse(
//                            RestApiStatus.ERROR,
//                            "Cơ sở không tồn tại",
//                            null
//                    ),
//                    HttpStatus.NOT_FOUND);
//        }
        Optional<Facility> existFacility = facilityRepository.findById(facilityId);
        existFacility.map(facilities -> {
            facilities.setCode(GenerateNameHelper.generateCodeFromName(request.getFacilityName().trim()));
            facilities.setName(GenerateNameHelper.replaceManySpaceToOneSpace(request.getFacilityName().trim()));
            facilities.setCreatedAt(facilities.getCreatedAt());
//            facilities.setStatus(EntityStatus.ACTIVE);
            return facilityRepository.save(facilities);
        });
        return existFacility
                .map(
                        facility -> new ResponseEntity<>(
                                new ApiResponse(
                                        RestApiStatus.SUCCESS,
                                        "Cập nhật cơ sở thành công",
                                        null
                                ),
                                HttpStatus.CREATED)
                )
                .orElseGet(
                        () -> new ResponseEntity<>(
                                new ApiResponse(
                                        RestApiStatus.ERROR,
                                        "Cơ sở không tồn tại",
                                        null
                                ),
                                HttpStatus.NOT_FOUND)
                );
    }

    @Override
    public ResponseEntity<?> changeFacilityStatus(String facilityId) {
        Optional<Facility> facilityOptional = facilityRepository.findById(facilityId);

        if (facilityOptional.isEmpty()) {
            return RouterHelper.responseError("Không tìm thấy cơ sở");
        }

        Facility facility = facilityOptional.get();
        // ----- BỔ SUNG: kiểm tra đã đổi status trong cùng ngày chưa -----
        long lastUpdatedMillis = facility.getUpdatedAt(); // epoch millis
        LocalDate lastUpdatedDate = Instant
                .ofEpochMilli(lastUpdatedMillis)
                .atZone(ZoneId.of("Asia/Ho_Chi_Minh"))    // dùng timezone phù hợp
                .toLocalDate();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        if (lastUpdatedDate.isEqual(today)) {
            return RouterHelper.responseError("Chỉ được thay đổi trạng thái cơ sở 1 lần mỗi ngày");
        }
        // ---------------------------------------------------------------


        facility.setStatus(facility.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
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
                mailerDefaultRequest.setTitle("Thông báo từ " + appName);
                mailerDefaultRequest.setContent(MailerHelper.loadTemplate(MailerHelper.TEMPLATE_CHANGE_STATUS_FACILITY, Map.of("FACILITY_NAME", entity.getName())));
                mailerHelper.send(mailerDefaultRequest);
            }
        } else {
            commonUserStudentRepository.disableAllStudentDuplicateShiftByIdFacility(facility.getId());
        }

        return RouterHelper.responseSuccess("Thay đổi trạng thái cơ sở thành công", entity);
    }

    @Override
    public ResponseEntity<?> getFacilityById(String facilityId) {
        return facilityRepository.getDetailFacilityById(facilityId)
                .map(
                        facility -> new ResponseEntity<>(
                                new ApiResponse(
                                        RestApiStatus.SUCCESS,
                                        "Hiển thị chi tiết cơ sở thành công",
                                        facilityRepository.getDetailFacilityById(facilityId)
                                ),
                                HttpStatus.OK)
                )
                .orElseGet(
                        () -> new ResponseEntity<>(
                                new ApiResponse(
                                        RestApiStatus.WARNING,
                                        "Cơ sở không tồn tại",
                                        null
                                ),
                                HttpStatus.NOT_FOUND)
                );

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
        return RouterHelper.responseSuccess("Tăng mức ưu tiên hiển thị thành công", facilityRepository.save(facility));
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
        return RouterHelper.responseSuccess("Giảm mức ưu tiên hiển thị thành công", facilityRepository.save(facility));
    }
}
