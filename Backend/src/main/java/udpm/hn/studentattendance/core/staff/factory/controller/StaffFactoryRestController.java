package udpm.hn.studentattendance.core.staff.factory.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import udpm.hn.studentattendance.core.staff.factory.model.request.StaffFactoryRequest;
import udpm.hn.studentattendance.core.staff.factory.service.StaffFactoryService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteStaffConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteStaffConstant.URL_API_FACTORY_MANAGEMENT)
public class StaffFactoryRestController {

    private final StaffFactoryService staffFactoryService;
    @GetMapping
    public ResponseEntity<?> getAllFactory(StaffFactoryRequest staffFactoryRequest){
        return staffFactoryService.getAllFactory(staffFactoryRequest);
    }

}
