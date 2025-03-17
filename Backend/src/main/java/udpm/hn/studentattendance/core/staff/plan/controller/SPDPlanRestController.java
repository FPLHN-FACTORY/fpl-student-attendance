package udpm.hn.studentattendance.core.staff.plan.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDCreatePlanRequest;
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

    @GetMapping("{id}/list/factory")
    public ResponseEntity<?> getAllFactory(@PathVariable("id") String idPlan) {
        return spdPlanService.getListFactory(idPlan);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPlan(@PathVariable("id") String id) {
        return spdPlanService.getPlan(id);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createPlan(@Valid @RequestBody SPDCreatePlanRequest request) {
        return spdPlanService.createPlan(request);
    }

}
