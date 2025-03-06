package udpm.hn.studentattendance.core.staff.factory.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import udpm.hn.studentattendance.core.staff.factory.model.request.FactoryRequest;
import udpm.hn.studentattendance.core.staff.factory.service.FactoryService;
import udpm.hn.studentattendance.infrastructure.constants.RoutesConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RoutesConstant.URL_API_STAFF_FACTORY_MANAGEMENT)
public class FactoryRestController {

    private final FactoryService factoryService;
    @GetMapping
    public ResponseEntity<?> getAllFactory(FactoryRequest factoryRequest){
        return factoryService.getAllFactory(factoryRequest);
    }
}
