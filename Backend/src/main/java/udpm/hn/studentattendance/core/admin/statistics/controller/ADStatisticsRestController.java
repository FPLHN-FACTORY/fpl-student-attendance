package udpm.hn.studentattendance.core.admin.statistics.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import udpm.hn.studentattendance.core.admin.statistics.model.request.ADStatisticRequest;
import udpm.hn.studentattendance.core.admin.statistics.service.ADStatisticsService;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAdminConstant;

@RestController
@RequiredArgsConstructor
@RequestMapping({RouteAdminConstant.URL_API_STATISTICS_MANAGEMENT})
public class ADStatisticsRestController {

    private final ADStatisticsService adStatisticsService;

    @GetMapping
    public ResponseEntity<?> getAllListStatistics(ADStatisticRequest request){
        return adStatisticsService.getAllListStats(request);
    }

}
