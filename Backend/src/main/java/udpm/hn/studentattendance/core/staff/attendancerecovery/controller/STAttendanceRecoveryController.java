package udpm.hn.studentattendance.core.staff.attendancerecovery.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STAttendanceRecoveryRequest;
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STCreateOrUpdateNewEventRequest;
import udpm.hn.studentattendance.core.staff.attendancerecovery.service.STAttendanceRecoveryService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteStaffConstant;

@Controller
@RequiredArgsConstructor
@RequestMapping(RouteStaffConstant.URL_API_ATTENDANCE_RECOVERY_MANAGEMENT)
public class STAttendanceRecoveryController {

    private final STAttendanceRecoveryService service;

    @GetMapping
    ResponseEntity<?> getListAttendanceRecovery(STAttendanceRecoveryRequest request){
        return service.getListAttendanceRecovery(request);
    }

    @GetMapping("/semesters")
    ResponseEntity<?> getAllSemester(){
        return service.getAllSemester();
    }

    @PostMapping
    ResponseEntity<?> createNewEvent(@Valid @RequestBody STCreateOrUpdateNewEventRequest request){
        return service.createNewEventAttendanceRecovery(request);
    }

    @GetMapping("/detail/{id}")
    ResponseEntity<?> getDetailEventAttendanceRecovery(@PathVariable String id){
        return service.getDetailEventAttendanceRecovery(id);
    }

    @PutMapping("{id}")
    ResponseEntity<?> updateEvent(@Valid @RequestBody STCreateOrUpdateNewEventRequest request, @PathVariable String id){
        return service.updateEventAttendanceRecovery(request, id);
    }
}
