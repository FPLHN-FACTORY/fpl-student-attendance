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
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDCreatePlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterCreatePlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanDateDetailRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.services.SPDPlanDateService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteStaffConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteStaffConstant.URL_API_PLAN_DATE_MANAGEMENT)
public class SPDPlanDateRestController {

    private final SPDPlanDateService SPDPlanDateService;

    @GetMapping
    public ResponseEntity<?> getAllList(@Valid SPDFilterPlanDateRequest request) {
        return SPDPlanDateService.getAllList(request);
    }

    @GetMapping("/list/subject")
    public ResponseEntity<?> getAllSubject() {
        return SPDPlanDateService.getAllSubject();
    }

    @GetMapping("/list/level-project")
    public ResponseEntity<?> getAllLevel() {
        return SPDPlanDateService.getAllLevel();
    }

    @GetMapping("/list/semester")
    public ResponseEntity<?> getListSemester() {
        return SPDPlanDateService.getListSemester();
    }

    @GetMapping("/list/year")
    public ResponseEntity<?> getAllYear() {
        return SPDPlanDateService.getAllYear();
    }

    @GetMapping("/list/factory")
    public ResponseEntity<?> getAllFactory(@Valid SPDFilterCreatePlanDateRequest request) {
        return SPDPlanDateService.getListFactory(request);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable("id") String id) {
        return SPDPlanDateService.getDetail(id);
    }

    @GetMapping("/{id}/list")
    public ResponseEntity<?> getDetail(@PathVariable("id") String idFactory, @Valid SPDFilterPlanDateDetailRequest request) {
        request.setIdFactory(idFactory);
        return SPDPlanDateService.getAllDetailList(request);
    }

    @DeleteMapping("/{idFactory}/delete/{idPlanDate}")
    public ResponseEntity<?> deletePlanDate(@PathVariable("idPlanDate") String idPlanDate, @PathVariable String idFactory) {
        return SPDPlanDateService.deletePlanDate(idPlanDate);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createPlanDate(@Valid @RequestBody SPDCreatePlanDateRequest request) {
        return SPDPlanDateService.createPlanDate(request);
    }

    @PostMapping("/{idFactory}/add")
    public ResponseEntity<?> addPlanDate(@PathVariable("idFactory") String idFactory, @Valid @RequestBody SPDAddOrUpdatePlanDateRequest request) {
        request.setIdFactory(idFactory);
        return SPDPlanDateService.addPlanDate(request);
    }

    @PutMapping("/{idFactory}/update")
    public ResponseEntity<?> updatePlanDate(@Valid @RequestBody SPDAddOrUpdatePlanDateRequest request) {
        return SPDPlanDateService.updatePlanDate(request);
    }
}
