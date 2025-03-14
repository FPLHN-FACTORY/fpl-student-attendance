package udpm.hn.studentattendance.core.admin.facility.service.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.admin.facility.model.request.Admin_CreateUpdateFacilityRequest;
import udpm.hn.studentattendance.core.admin.facility.model.request.Admin_FacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.facility.repository.Admin_FacilityExtendRepository;
import udpm.hn.studentattendance.core.admin.facility.service.Admin_FacilityService;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.helpers.GenerateNameHelper;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Validated
public class Admin_FacilityServiceImpl implements Admin_FacilityService {
    private final Admin_FacilityExtendRepository facilityRepository;

    @Override
    public ResponseEntity<?> getAllFacility(Admin_FacilitySearchRequest request) {
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
    public ResponseEntity<?> createFacility(@Valid Admin_CreateUpdateFacilityRequest request) {
        Optional<Facility> existFacility = facilityRepository.findByName(request.getFacilityName().trim());
        if (!existFacility.isEmpty()) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.WARNING,
                            "Cơ sở đã tồn tại",
                            null
                    ),
                    HttpStatus.BAD_REQUEST);
        }
        String code = GenerateNameHelper.generateCodeFromName(request.getFacilityName());
        Facility facility = new Facility();
        facility.setCode(code);
        facility.setName(GenerateNameHelper.replaceManySpaceToOneSpace(request.getFacilityName()));
        facility.setCreatedAt(System.currentTimeMillis());
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
    public ResponseEntity<?> updateFacility(String facilityId, @Valid Admin_CreateUpdateFacilityRequest request) {
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
        facilityOptional.map(facility -> {
            facility.setStatus(facility.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
            return facilityRepository.save(facility);
        });
        return facilityOptional
                .map(
                        object -> new ResponseEntity<>(
                                new ApiResponse(
                                        RestApiStatus.SUCCESS,
                                        "Cập nhật cơ sở thành công",
                                        null
                                ),
                                HttpStatus.OK)
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
}
