package udpm.hn.studentattendance.core.admin.facility.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.admin.facility.model.request.CreateUpdateFacilityRequest;
import udpm.hn.studentattendance.core.admin.facility.model.request.FacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.facility.service.AdFacilityService;
import udpm.hn.studentattendance.infrastructure.constants.RoutesConstant;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAdminConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteAdminConstant.URL_API_FACILITY_MANAGEMENT)
public class AdFacilityRestController {

    private final AdFacilityService adFacilityService;

    @GetMapping
    public ResponseEntity<?> getAllFacility
            (FacilitySearchRequest request) {
        return adFacilityService.getAllFacility(request);
    }
    @GetMapping("/{facilityId}")
    public ResponseEntity<?> updateFacility
            (@PathVariable("facilityId") String facilityId){
        return adFacilityService.getFacilityById(facilityId);
    }
    @PostMapping
    public ResponseEntity<?> createFacility
            (@RequestBody CreateUpdateFacilityRequest createUpdateFacilityRequest) {
        return adFacilityService.createFacility(createUpdateFacilityRequest);
    }

    @PutMapping("/{facilityId}")
    public ResponseEntity<?> updateFacility
            (@PathVariable("facilityId") String facilityId,
             @RequestBody CreateUpdateFacilityRequest createUpdateFacilityRequest) {
        return adFacilityService.updateFacility(facilityId, createUpdateFacilityRequest);
    }

    @PutMapping("/status/{facilityId}")
    public ResponseEntity<?> changeStatusFacility
            (@PathVariable("facilityId") String facilityId) {
        return adFacilityService.changeFacilityStatus(facilityId);
    }

}
