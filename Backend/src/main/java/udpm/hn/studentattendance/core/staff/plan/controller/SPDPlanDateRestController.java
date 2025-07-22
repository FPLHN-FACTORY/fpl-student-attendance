package udpm.hn.studentattendance.core.staff.plan.controller;

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
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDAddOrUpdatePlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDDeletePlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDUpdateLinkMeetRequest;
import udpm.hn.studentattendance.core.staff.plan.services.SPDPlanDateService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteStaffConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteStaffConstant.URL_API_PLAN_DATE_MANAGEMENT)
public class SPDPlanDateRestController {

    private final SPDPlanDateService spdPlanDateService;

    @GetMapping("/{idPlanFactory}")
    public ResponseEntity<?> getDetail(@PathVariable("idPlanFactory") String idPlanFactory) {
        return spdPlanDateService.getDetail(idPlanFactory);
    }

    @GetMapping("/{idPlanFactory}/list")
    public ResponseEntity<?> getAllList(@PathVariable("idPlanFactory") String idPlanFactory,
            @Valid SPDFilterPlanDateRequest request) {
        request.setIdPlanFactory(idPlanFactory);
        return spdPlanDateService.getAllList(request);
    }

    @DeleteMapping("/{idPlanFactory}/delete")
    public ResponseEntity<?> deleteMultiplePlanDate(@RequestBody SPDDeletePlanDateRequest request,
            @PathVariable String idPlanFactory) {
        request.setIdPlanFactory(idPlanFactory);
        return spdPlanDateService.deleteMultiplePlanDate(request);
    }

    @DeleteMapping("/{idPlanFactory}/delete/{idPlanDate}")
    public ResponseEntity<?> deletePlanDate(@PathVariable("idPlanDate") String idPlanDate,
            @PathVariable String idPlanFactory) {
        return spdPlanDateService.deletePlanDate(idPlanDate);
    }

    @PostMapping("/{idPlanFactory}/add")
    public ResponseEntity<?> addPlanDate(@PathVariable("idPlanFactory") String idPlanFactory,
            @Valid @RequestBody SPDAddOrUpdatePlanDateRequest request) {
        request.setIdPlanFactory(idPlanFactory);
        return spdPlanDateService.addPlanDate(request);
    }

    @PutMapping("/{idPlanFactory}/update")
    public ResponseEntity<?> updatePlanDate(@Valid @RequestBody SPDAddOrUpdatePlanDateRequest request) {
        return spdPlanDateService.updatePlanDate(request);
    }

    @PutMapping("/{idPlanFactory}/update-link")
    public ResponseEntity<?> updateLinkMeet(@Valid @RequestBody SPDUpdateLinkMeetRequest request) {
        return spdPlanDateService.updateLinkMeet(request);
    }

    @PostMapping("/{idPlanFactory}/send-mail")
    public ResponseEntity<?> sendMail(@PathVariable String idPlanFactory) {
        return spdPlanDateService.sendMail(idPlanFactory);
    }

}
