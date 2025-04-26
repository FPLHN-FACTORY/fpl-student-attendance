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
import udpm.hn.studentattendance.core.admin.facility.model.request.AFAddOrUpdateFacilityShiftRequest;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFFilterFacilityShiftRequest;
import udpm.hn.studentattendance.core.admin.facility.service.AFFacilityShiftService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAdminConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteAdminConstant.URL_API_FACILITY_MANAGEMENT)
public class AFFacilityShiftRestController {

    private final AFFacilityShiftService afFacilityShiftService;

    @GetMapping("/{idFacility}/list-shift")
    public ResponseEntity<?> getAllList(@Valid AFFilterFacilityShiftRequest request, @PathVariable String idFacility) {
        request.setIdFacility(idFacility);
        return afFacilityShiftService.getAllList(request);
    }

    @PostMapping("/{idFacility}/add-shift")
    public ResponseEntity<?> add(@Valid @RequestBody AFAddOrUpdateFacilityShiftRequest request,
            @PathVariable String idFacility) {
        request.setIdFacility(idFacility);
        return afFacilityShiftService.addShift(request);
    }

    @PutMapping("/{idFacility}/update-shift")
    public ResponseEntity<?> update(@Valid @RequestBody AFAddOrUpdateFacilityShiftRequest request,
            @PathVariable String idFacility) {
        request.setIdFacility(idFacility);
        return afFacilityShiftService.updateShift(request);
    }

    @DeleteMapping("/{idFacilityShift}/delete-shift")
    public ResponseEntity<?> delete(@PathVariable String idFacilityShift) {
        return afFacilityShiftService.deleteShift(idFacilityShift);
    }

    @PutMapping("/{idFacilityShift}/change-status-shift")
    public ResponseEntity<?> changeStatus(@PathVariable String idFacilityShift) {
        return afFacilityShiftService.changeStatus(idFacilityShift);
    }

}
