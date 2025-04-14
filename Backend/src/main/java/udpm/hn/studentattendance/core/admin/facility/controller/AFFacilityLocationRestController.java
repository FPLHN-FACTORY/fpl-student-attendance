package udpm.hn.studentattendance.core.admin.facility.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFAddOrUpdateFacilityLocationRequest;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFFilterFacilityLocationRequest;
import udpm.hn.studentattendance.core.admin.facility.service.AFFacilityLocationService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAdminConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteAdminConstant.URL_API_FACILITY_MANAGEMENT)
public class AFFacilityLocationRestController {

    private final AFFacilityLocationService afFacilityLocationService;

    @GetMapping("/{idFacility}/list-location")
    public ResponseEntity<?> getAllList(@Valid AFFilterFacilityLocationRequest request, @PathVariable String idFacility) {
        request.setIdFacility(idFacility);
        return afFacilityLocationService.getAllList(request);
    }

    @PostMapping("/{idFacility}/add-location")
    public ResponseEntity<?> addIP(@Valid @RequestBody AFAddOrUpdateFacilityLocationRequest request, @PathVariable String idFacility) {
        request.setIdFacility(idFacility);
        return afFacilityLocationService.addLocation(request);
    }

    @PutMapping("/{idFacility}/update-location")
    public ResponseEntity<?> updateIP(@Valid @RequestBody AFAddOrUpdateFacilityLocationRequest request, @PathVariable String idFacility) {
        request.setIdFacility(idFacility);
        return afFacilityLocationService.updateLocation(request);
    }

    @DeleteMapping("/{idFacilityLocation}/delete-location")
    public ResponseEntity<?> deleteIP(@PathVariable String idFacilityLocation) {
        return afFacilityLocationService.deleteLocation(idFacilityLocation);
    }

    @PutMapping("/{idFacilityLocation}/change-status-location")
    public ResponseEntity<?> changeStatus(@PathVariable String idFacilityLocation) {
        return afFacilityLocationService.changeStatus(idFacilityLocation);
    }

}
