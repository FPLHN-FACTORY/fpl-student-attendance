package udpm.hn.studentattendance.core.teacher.statistics.services;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.teacher.statistics.model.request.TSFilterFactoryStatsRequest;
import udpm.hn.studentattendance.core.teacher.statistics.model.request.TSSendMailStatsRequest;

public interface TSStatisticsService {

    ResponseEntity<?> getAllStats(String idSemester);

    ResponseEntity<?> getListStatsFactory(TSFilterFactoryStatsRequest request);

    ResponseEntity<?> getListUser(String idSemester);

    ResponseEntity<?> sendMailStats(TSSendMailStatsRequest request);

}
