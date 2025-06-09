package udpm.hn.studentattendance.core.staff.useractivity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import udpm.hn.studentattendance.core.staff.useractivity.service.STUserActivityService;
import udpm.hn.studentattendance.infrastructure.common.model.request.UALFilterRequest;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteStaffConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteStaffConstant.URL_API_USER_ACTIVITY_MANAGEMENT)
public class STUserActivityRestController {

    private final STUserActivityService service;

    @GetMapping()
    public ResponseEntity<?> getAllUserLogActivity(UALFilterRequest request) {
        return service.getAllUserActivity(request);
    }


    @GetMapping("/user-staff")
    public ResponseEntity<?> getAllStaffByFacility(){
        return service.getAllUserStaff();
    }

}
