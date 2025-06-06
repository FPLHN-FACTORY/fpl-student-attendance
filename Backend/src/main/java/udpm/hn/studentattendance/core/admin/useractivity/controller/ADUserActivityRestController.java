package udpm.hn.studentattendance.core.admin.useractivity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import udpm.hn.studentattendance.core.admin.useractivity.service.ADUserActivityService;
import udpm.hn.studentattendance.infrastructure.common.model.request.UALFilterRequest;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAdminConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteAdminConstant.URL_API_USER_ACTIVITY_MANAGEMENT)
public class ADUserActivityRestController {

    private final ADUserActivityService service;

    @GetMapping()
    public ResponseEntity<?> getAllUserLogActivity(UALFilterRequest request) {
        return service.getAllUserActivity(request);
    }

    @GetMapping("/user-staff")
    public ResponseEntity<?> getAllStaff() {
        return service.getAllUserStaff();
    }

    @GetMapping("/user-admin")
    public ResponseEntity<?> getAllAdmin() {
        return service.getAllUserAdmin();
    }

    @GetMapping("/facility")
    public ResponseEntity<?> getAllFacility() {
        return service.getAllFacility();
    }
}
