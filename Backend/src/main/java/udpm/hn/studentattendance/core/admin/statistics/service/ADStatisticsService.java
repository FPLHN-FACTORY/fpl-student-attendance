package udpm.hn.studentattendance.core.admin.statistics.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.statistics.model.request.ADStatisticRequest;

public interface ADStatisticsService {

    ResponseEntity<?> getAllListStats(ADStatisticRequest request);


}
