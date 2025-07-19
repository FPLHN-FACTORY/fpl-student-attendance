package udpm.hn.studentattendance.core.teacher.factory.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCFilterShiftFactoryRequest;
import udpm.hn.studentattendance.core.teacher.factory.service.TCShiftFactoryService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteTeacherConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteTeacherConstant.URL_API_SHIFT_FACTORY_MANAGEMENT)
public class TCShiftFactoryRestController {

    private final TCShiftFactoryService tcShiftFactoryService;

    @GetMapping("/{idFactory}")
    public ResponseEntity<?> getDetail(@PathVariable("idFactory") String idFactory) {
        return tcShiftFactoryService.getDetail(idFactory);
    }

    @GetMapping("/{idFactory}/list")
    public ResponseEntity<?> getAllList(@PathVariable("idFactory") String idFactory,
            @Valid TCFilterShiftFactoryRequest request) {
        request.setIdFactory(idFactory);
        return tcShiftFactoryService.getAllList(request);
    }

    @GetMapping("/list/shift")
    public ResponseEntity<?> getAllShift() {
        return tcShiftFactoryService.getListShift();
    }

}
