package udpm.hn.studentattendance.core.staff.factory.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.staff.factory.model.request.FactoryCreateUpdateRequest;
import udpm.hn.studentattendance.core.staff.factory.model.request.FactoryRequest;
import udpm.hn.studentattendance.core.staff.factory.service.FactoryService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteStaffConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteStaffConstant.URL_API_FACTORY_MANAGEMENT)
public class FactoryRestController {

    private final FactoryService factoryService;

    @GetMapping
    public ResponseEntity<?> getAllFactory(FactoryRequest staffFactoryRequest) {
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
    public ResponseEntity<?> createFacility(@Valid @RequestBody FactoryCreateUpdateRequest factoryCreateUpdateRequest) {
        return factoryService.createFactory(factoryCreateUpdateRequest);
    }

    @PutMapping
    public ResponseEntity<?> updateFacility(@Valid @RequestBody FactoryCreateUpdateRequest factoryCreateUpdateRequest) {
        return factoryService.updateFactory(factoryCreateUpdateRequest);
    }

    @PutMapping("/status/{factoryId}")
    public ResponseEntity<?> changeStatus(@PathVariable("factoryId") String factoryId) {
        return factoryService.changeStatus(factoryId);
    }

}
