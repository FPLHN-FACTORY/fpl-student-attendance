package udpm.hn.studentattendance.core.student.statistics.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import udpm.hn.studentattendance.core.student.statistics.service.STDStatisticsService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteStudentConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping(RouteStudentConstant.URL_API_STATISTICS)
public class STDStatisticsRestController {

    private final STDStatisticsService service;

    @GetMapping()
    public ResponseEntity<?> getStatistics(@Param("idSemester") String idSemester) {
        return service.getStatistics(idSemester);
    }
}
