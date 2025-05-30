package udpm.hn.studentattendance.core.staff.statistics.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import udpm.hn.studentattendance.core.staff.statistics.model.request.SSFilterFactoryStatsRequest;
import udpm.hn.studentattendance.core.staff.statistics.model.request.SSSendMailStatsRequest;
import udpm.hn.studentattendance.core.staff.statistics.services.SSStatisticsService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteStaffConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteStaffConstant.URL_API_STATISTICS_MANAGEMENT)
public class SSStatisticsRestController {

    private final SSStatisticsService SSStatisticsService;

    @GetMapping("/{idSemester}")
    public ResponseEntity<?> getAllStats(@PathVariable String idSemester) {
        return SSStatisticsService.getAllStats(idSemester);
    }

    @GetMapping("/{idSemester}/list-factory")
    public ResponseEntity<?> getListStatsFactory(@PathVariable String idSemester, SSFilterFactoryStatsRequest request) {
        request.setIdSemester(idSemester);
        return SSStatisticsService.getListStatsFactory(request);
    }

    @GetMapping("/{idSemester}/list-user")
    public ResponseEntity<?> getListUser(@PathVariable String idSemester) {
        return SSStatisticsService.getListUser(idSemester);
    }

    @PostMapping("/{idSemester}")
    public ResponseEntity<?> sendMailStats(@PathVariable String idSemester, @RequestBody SSSendMailStatsRequest request) {
        request.setIdSemester(idSemester);
        return SSStatisticsService.sendMailStats(request);
    }

}
