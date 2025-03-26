package udpm.hn.studentattendance.core.staff.factory.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.staff.factory.model.request.Staff_FactoryCreateUpdateRequest;
import udpm.hn.studentattendance.core.staff.factory.model.request.Staff_FactoryRequest;
import udpm.hn.studentattendance.core.staff.factory.service.Staff_FactoryService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteStaffConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteStaffConstant.URL_API_FACTORY_MANAGEMENT)
public class Staff_FactoryRestController {

    private final Staff_FactoryService factoryService;

    @GetMapping
    public ResponseEntity<?> getAllFactory(Staff_FactoryRequest staffFactoryRequest) {
        return factoryService.getAllFactory(staffFactoryRequest);
    }

    @GetMapping("/detail/{factoryId}")
    public ResponseEntity<?> getDetailFactory(@PathVariable("factoryId") String factoryId) {
        return factoryService.getDetailFactory(factoryId);
    }

    @GetMapping("/{factoryId}")
    public ResponseEntity<?> infoDetailFactory(@PathVariable("factoryId") String factoryId) {
        return factoryService.detailFactory(factoryId);
    }

    @GetMapping("/project")
    public ResponseEntity<?> getAllProject() {
        return factoryService.getAllProject();
    }

    @GetMapping("/staff")
    public ResponseEntity<?> getAllStaff() {
        return factoryService.getAllStaff();
    }

    @GetMapping("/subject-facility")
    public ResponseEntity<?> getAllSubjectFacility() {
        return factoryService.getAllSubjectFacility();
    }

    @PostMapping
    public ResponseEntity<?> createFacility(@Valid @RequestBody Staff_FactoryCreateUpdateRequest factoryCreateUpdateRequest) {
        return factoryService.createFactory(factoryCreateUpdateRequest);
    }

    @PutMapping
    public ResponseEntity<?> updateFacility(@Valid @RequestBody Staff_FactoryCreateUpdateRequest factoryCreateUpdateRequest) {
        return factoryService.updateFactory(factoryCreateUpdateRequest);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<?> changeStatus(@PathVariable("id") String factoryId) {
        return factoryService.changeStatus(factoryId);
    }

    @GetMapping("/exist-plan/{factoryId}")
    public ResponseEntity<?> getPlanByFactory(@PathVariable("factoryId") String factoryId) {
        return factoryService.existsPlanByFactoryId(factoryId);
    }

    @PutMapping("/change-all-status")
    public ResponseEntity<?> changeAllStatusBySemester(){
        return factoryService.changeAllStatusByFactory();
    }
}
