package udpm.hn.studentattendance.core.staff.factory.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.staff.factory.model.request.USFactoryCreateUpdateRequest;
import udpm.hn.studentattendance.core.staff.factory.model.request.USFactoryRequest;
import udpm.hn.studentattendance.core.staff.factory.service.USFactoryService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteStaffConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteStaffConstant.URL_API_FACTORY_MANAGEMENT)
public class USFactoryRestController {

    private final USFactoryService factoryService;

    @GetMapping
    public ResponseEntity<?> getAllFactory(@Valid USFactoryRequest staffFactoryRequest) {
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
    public ResponseEntity<?> createFactory(
            @Valid @RequestBody USFactoryCreateUpdateRequest factoryCreateUpdateRequest) {
        return factoryService.createFactory(factoryCreateUpdateRequest);
    }

    @PutMapping
    public ResponseEntity<?> updateFactory(
            @Valid @RequestBody USFactoryCreateUpdateRequest factoryCreateUpdateRequest) {
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
    public ResponseEntity<?> changeAllStatusBySemester() {
        return factoryService.changeAllStatusByFactory();
    }

    @GetMapping("/semesters")
    public ResponseEntity<?> getAllSemester() {
        return factoryService.getAllSemester();
    }
}
