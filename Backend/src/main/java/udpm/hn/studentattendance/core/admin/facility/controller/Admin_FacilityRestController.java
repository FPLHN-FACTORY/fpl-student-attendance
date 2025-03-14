package udpm.hn.studentattendance.core.admin.facility.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.admin.facility.model.request.Admin_CreateUpdateFacilityRequest;
import udpm.hn.studentattendance.core.admin.facility.model.request.Admin_FacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.facility.service.Admin_FacilityService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAdminConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteAdminConstant.URL_API_FACILITY_MANAGEMENT)
public class Admin_FacilityRestController {

    private final Admin_FacilityService adFacilityService;

    @GetMapping
    public ResponseEntity<?> getAllFacility
            (Admin_FacilitySearchRequest request) {
        return adFacilityService.getAllFacility(request);
    }
    @GetMapping("/{facilityId}")
    public ResponseEntity<?> updateFacility
            (@PathVariable("facilityId") String facilityId){
        return adFacilityService.getFacilityById(facilityId);
    }
    @PostMapping
    public ResponseEntity<?> createFacility
            (@RequestBody Admin_CreateUpdateFacilityRequest createUpdateFacilityRequest) {
        return adFacilityService.createFacility(createUpdateFacilityRequest);
    }

    @PutMapping("/{facilityId}")
    public ResponseEntity<?> updateFacility
            (@PathVariable("facilityId") String facilityId,
             @RequestBody Admin_CreateUpdateFacilityRequest createUpdateFacilityRequest) {
        return adFacilityService.updateFacility(facilityId, createUpdateFacilityRequest);
    }

    @PutMapping("/status/{facilityId}")
    public ResponseEntity<?> changeStatusFacility
            (@PathVariable("facilityId") String facilityId) {
        return adFacilityService.changeFacilityStatus(facilityId);
    }

}
