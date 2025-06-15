package udpm.hn.studentattendance.core.student.statistics.service;

import org.springframework.http.ResponseEntity;

public interface STDStatisticsService {

    ResponseEntity<?> getStatistics(String idSemester);

}
