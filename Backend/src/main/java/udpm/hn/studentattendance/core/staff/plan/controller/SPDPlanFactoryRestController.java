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
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDAddPlanFactoryRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanFactoryRequest;
import udpm.hn.studentattendance.core.staff.plan.services.SPDPlanFactoryService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteStaffConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteStaffConstant.URL_API_PLAN_FACTORY_MANAGEMENT)
public class SPDPlanFactoryRestController {

    private final SPDPlanFactoryService spdPlanFactoryService;

    @GetMapping("/{idPlan}")
    public ResponseEntity<?> getAllList(@Valid SPDFilterPlanFactoryRequest request, @PathVariable String idPlan) {
        request.setIdPlan(idPlan);
        return spdPlanFactoryService.getAllList(request);
    }

    @PostMapping("/{idPlan}")
    public ResponseEntity<?> createPlanFactory(@Valid @RequestBody SPDAddPlanFactoryRequest request, @PathVariable String idPlan) {
        request.setIdPlan(idPlan);
        return spdPlanFactoryService.createPlanFactory(request);
    }

    @GetMapping("{idPlan}/list/factory")
    public ResponseEntity<?> getAllFactory(@PathVariable("idPlan") String idPlan) {
        return spdPlanFactoryService.getListFactory(idPlan);
    }

    @GetMapping("/list/shift")
    public ResponseEntity<?> getAllShift() {
        return spdPlanFactoryService.getListShift();
    }

    @DeleteMapping("/{idPlanFactory}")
    public ResponseEntity<?> deletePlan(@PathVariable("idPlanFactory") String idPlanFactory) {
        return spdPlanFactoryService.deletePlanFactory(idPlanFactory);
    }

    @PutMapping("/{idPlanFactory}/change-status")
    public ResponseEntity<?> changeStatus(@PathVariable("idPlanFactory") String idPlanFactory) {
        return spdPlanFactoryService.changeStatus(idPlanFactory);
    }

}
