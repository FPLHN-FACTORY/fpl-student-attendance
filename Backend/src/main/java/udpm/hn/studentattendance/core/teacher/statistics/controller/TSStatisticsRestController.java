package udpm.hn.studentattendance.core.teacher.statistics.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import udpm.hn.studentattendance.core.teacher.statistics.model.request.TSFilterFactoryStatsRequest;
import udpm.hn.studentattendance.core.teacher.statistics.model.request.TSSendMailStatsRequest;
import udpm.hn.studentattendance.core.teacher.statistics.services.TSStatisticsService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteStaffConstant;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteTeacherConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteTeacherConstant.URL_API_STATISTICS_MANAGEMENT)
public class TSStatisticsRestController {

    private final TSStatisticsService TSStatisticsService;

    @GetMapping("/{idSemester}")
    public ResponseEntity<?> getAllStats(@PathVariable String idSemester) {
        return TSStatisticsService.getAllStats(idSemester);
    }

    @GetMapping("/{idSemester}/list-factory")
    public ResponseEntity<?> getListStatsFactory(@PathVariable String idSemester, TSFilterFactoryStatsRequest request) {
        request.setIdSemester(idSemester);
        return TSStatisticsService.getListStatsFactory(request);
    }

    @GetMapping("/{idSemester}/list-user")
    public ResponseEntity<?> getListUser(@PathVariable String idSemester) {
        return TSStatisticsService.getListUser(idSemester);
    }

    @PostMapping("/{idSemester}")
    public ResponseEntity<?> sendMailStats(@PathVariable String idSemester, @RequestBody TSSendMailStatsRequest request) {
        request.setIdSemester(idSemester);
        return TSStatisticsService.sendMailStats(request);
    }

}
