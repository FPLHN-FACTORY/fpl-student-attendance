package udpm.hn.studentattendance.core.staff.statistics.services;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.statistics.model.request.SSFilterFactoryStatsRequest;
import udpm.hn.studentattendance.core.staff.statistics.model.request.SSSendMailStatsRequest;

public interface SSStatisticsService {

    ResponseEntity<?> getAllStats(String idSemester);

    ResponseEntity<?> getListStatsFactory(SSFilterFactoryStatsRequest request);

    ResponseEntity<?> getListUser(String idSemester);

    ResponseEntity<?> sendMailStats(SSSendMailStatsRequest request);

}
