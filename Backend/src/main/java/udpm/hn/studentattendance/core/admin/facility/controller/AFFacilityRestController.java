package udpm.hn.studentattendance.core.admin.facility.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFCreateUpdateFacilityRequest;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFFacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.facility.service.AFFacilityService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAdminConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteAdminConstant.URL_API_FACILITY_MANAGEMENT)
public class AFFacilityRestController {

    private final AFFacilityService adFacilityService;

    @GetMapping
    public ResponseEntity<?> getAllFacility
            (AFFacilitySearchRequest request) {
        return adFacilityService.getAllFacility(request);
    }

    @GetMapping("/{facilityId}")
    public ResponseEntity<?> getFacility
            (@PathVariable("facilityId") String facilityId){
        return adFacilityService.getFacilityById(facilityId);
    }
    @PostMapping
    public ResponseEntity<?> createFacility
            (@RequestBody AFCreateUpdateFacilityRequest createUpdateFacilityRequest) {
        return adFacilityService.createFacility(createUpdateFacilityRequest);
    }

    @PutMapping("/{facilityId}")
    public ResponseEntity<?> updateFacility
            (@PathVariable("facilityId") String facilityId,
             @RequestBody AFCreateUpdateFacilityRequest createUpdateFacilityRequest) {
        return adFacilityService.updateFacility(facilityId, createUpdateFacilityRequest);
    }

    @PutMapping("/status/{facilityId}")
    public ResponseEntity<?> changeStatusFacility
            (@PathVariable("facilityId") String facilityId) {
        return adFacilityService.changeFacilityStatus(facilityId);
    }

    @PutMapping("/up/{facilityId}")
    public ResponseEntity<?> up(@PathVariable("facilityId") String facilityId) {
        return adFacilityService.up(facilityId);
    }

    @PutMapping("/down/{facilityId}")
    public ResponseEntity<?> down(@PathVariable("facilityId") String facilityId) {
        return adFacilityService.down(facilityId);
    }

}
