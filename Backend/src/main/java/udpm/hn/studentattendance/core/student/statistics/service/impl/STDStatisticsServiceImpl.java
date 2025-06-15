package udpm.hn.studentattendance.core.student.statistics.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.student.statistics.model.dto.STDStatisticDto;
import udpm.hn.studentattendance.core.student.statistics.model.response.STDStatisticsFactoryChartResponse;
import udpm.hn.studentattendance.core.student.statistics.model.response.STDStatisticsStatResponse;
import udpm.hn.studentattendance.core.student.statistics.repository.STDFactoryLineChartRepository;
import udpm.hn.studentattendance.core.student.statistics.repository.STDStatisticsSemesterRepository;
import udpm.hn.studentattendance.core.student.statistics.service.STDStatisticsService;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class STDStatisticsServiceImpl implements STDStatisticsService {

    private final STDStatisticsSemesterRepository stdStatisticsSemesterRepository;

    private final STDFactoryLineChartRepository factoryLineChartRepository;

    private final SessionHelper sessionHelper;

    @Override
    public ResponseEntity<?> getStatistics(String idSemester) {
        String semesterId = null;
        Long now = new Date().getTime();
        for (Semester semester : stdStatisticsSemesterRepository.getAllSemestersByStatus(EntityStatus.ACTIVE)) {
            if (semester.getFromDate() <= now && now <= semester.getToDate()) {
                semesterId = semester.getId();
                break;
            }
        }


        STDStatisticsStatResponse statisticsStatResponse = stdStatisticsSemesterRepository.getAllStatisticBySemester(sessionHelper.getFacilityId(), sessionHelper.getUserId(), semesterId).orElse(null);
        List<STDStatisticsFactoryChartResponse> factoryChartResponse = factoryLineChartRepository.getAttendancePercentage(sessionHelper.getFacilityId(), sessionHelper.getUserId(), semesterId);

        STDStatisticDto statisticDto = new STDStatisticDto();
        statisticDto.setStdStatisticsStatResponse(statisticsStatResponse);
        statisticDto.setFactoryChartResponse(factoryChartResponse);
        return RouterHelper.responseSuccess("Lấy dữ liệu thống kê thành công", statisticDto);
    }

}
