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
import udpm.hn.studentattendance.core.admin.facility.model.request.AFAddOrUpdateFacilityIPRequest;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFFilterFacilityIPRequest;
import udpm.hn.studentattendance.core.admin.facility.service.AFFacilityIPService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAdminConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteAdminConstant.URL_API_FACILITY_MANAGEMENT)
public class AFFacilityIPRestController {

    private final AFFacilityIPService afFacilityIPService;

    @GetMapping("/{idFacility}/list-ip")
    public ResponseEntity<?> getAllList(@Valid AFFilterFacilityIPRequest request, @PathVariable String idFacility) {
        request.setIdFacility(idFacility);
        return afFacilityIPService.getAllList(request);
    }

    @PostMapping("/{idFacility}/add-ip")
    public ResponseEntity<?> addIP(@Valid @RequestBody AFAddOrUpdateFacilityIPRequest request, @PathVariable String idFacility) {
        request.setIdFacility(idFacility);
        return afFacilityIPService.addIP(request);
    }

    @PutMapping("/{idFacility}/update-ip")
    public ResponseEntity<?> updateIP(@Valid @RequestBody AFAddOrUpdateFacilityIPRequest request, @PathVariable String idFacility) {
        request.setIdFacility(idFacility);
        return afFacilityIPService.updateIP(request);
    }

    @DeleteMapping("/{idFacilityIP}/delete-ip")
    public ResponseEntity<?> deleteIP(@PathVariable String idFacilityIP) {
        return afFacilityIPService.deleteIP(idFacilityIP);
    }

    @PutMapping("/{idFacilityIP}/change-status-ip")
    public ResponseEntity<?> changeStatus(@PathVariable String idFacilityIP) {
        return afFacilityIPService.changeStatus(idFacilityIP);
    }

}
