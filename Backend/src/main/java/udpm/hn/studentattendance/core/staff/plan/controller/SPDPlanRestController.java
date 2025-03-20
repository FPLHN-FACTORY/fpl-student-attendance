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
import udpm.hn.studentattendance.core.staff.plan.model.request.SDPAddOrUpdatePlanRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterCreatePlanRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanRequest;
import udpm.hn.studentattendance.core.staff.plan.services.SPDPlanService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteStaffConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteStaffConstant.URL_API_PLAN_MANAGEMENT)
public class SPDPlanRestController {

    private final SPDPlanService spdPlanService;

    @GetMapping
    public ResponseEntity<?> getAllList(@Valid SPDFilterPlanRequest request) {
        return spdPlanService.getAllList(request);
    }

    @GetMapping("/list/subject")
    public ResponseEntity<?> getAllSubject() {
        return spdPlanService.getAllSubject();
    }

    @GetMapping("/list/level-project")
    public ResponseEntity<?> getAllLevel() {
        return spdPlanService.getAllLevel();
    }

    @GetMapping("/list/semester")
    public ResponseEntity<?> getListSemester() {
        return spdPlanService.getListSemester();
    }

    @GetMapping("/list/year")
    public ResponseEntity<?> getAllYear() {
        return spdPlanService.getAllYear();
    }

    @GetMapping("/list/project")
    public ResponseEntity<?> getAllFactory(@Valid SPDFilterCreatePlanRequest request) {
        return spdPlanService.getListProject(request);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPlan(@PathVariable("id") String id) {
        return spdPlanService.getPlan(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePlan(@PathVariable("id") String id) {
        return spdPlanService.deletePlan(id);
    }

    @PutMapping("/{id}/change-status")
    public ResponseEntity<?> changeStatus(@PathVariable("id") String id) {
        return spdPlanService.changeStatus(id);
    }

    @PostMapping
    public ResponseEntity<?> createPlan(@Valid @RequestBody SDPAddOrUpdatePlanRequest request) {
        return spdPlanService.createPlan(request);
    }

    @PutMapping
    public ResponseEntity<?> updatePlan(@Valid @RequestBody SDPAddOrUpdatePlanRequest request) {
        return spdPlanService.updatePlan(request);
    }
}
