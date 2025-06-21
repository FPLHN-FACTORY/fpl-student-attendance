package udpm.hn.studentattendance.core.student.statistics.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.student.statistics.model.dto.STDStatisticDto;
import udpm.hn.studentattendance.core.student.statistics.model.response.STDStatisticsAttendanceChartResponse;
import udpm.hn.studentattendance.core.student.statistics.model.response.STDStatisticsFactoryChartResponse;
import udpm.hn.studentattendance.core.student.statistics.model.response.STDStatisticsStatResponse;
import udpm.hn.studentattendance.core.student.statistics.repository.STDSFactoryAttendanceBarChartRepository;
import udpm.hn.studentattendance.core.student.statistics.repository.STDSFactoryLineChartRepository;
import udpm.hn.studentattendance.core.student.statistics.repository.STDStatisticsSemesterRepository;
import udpm.hn.studentattendance.core.student.statistics.service.STDStatisticsService;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class STDStatisticsServiceImpl implements STDStatisticsService {

    private final STDStatisticsSemesterRepository stdStatisticsSemesterRepository;

    private final STDSFactoryLineChartRepository factoryLineChartRepository;

    private final STDSFactoryAttendanceBarChartRepository attendanceBarChartRepository;
    private final SessionHelper sessionHelper;



    @Override
    public ResponseEntity<?> getStatistics(String idSemester) {
        STDStatisticsStatResponse statisticsStatResponse = stdStatisticsSemesterRepository.getAllStatisticBySemester(
                sessionHelper.getFacilityId(),
                sessionHelper.getUserId(),
                idSemester).orElse(null);

        List<STDStatisticsFactoryChartResponse> factoryChartResponse = factoryLineChartRepository
                .getAttendancePercentage(
                        sessionHelper.getFacilityId(),
                        sessionHelper.getUserId(),
                        idSemester);

        List<STDStatisticsAttendanceChartResponse> attendanceChartResponses =
                attendanceBarChartRepository.getAttendanceBarChart(sessionHelper.getUserId(), idSemester);

        STDStatisticDto statisticDto = new STDStatisticDto();
        statisticDto.setStdStatisticsStatResponse(statisticsStatResponse);
        statisticDto.setFactoryChartResponse(factoryChartResponse);
        statisticDto.setAttendanceChartResponses(attendanceChartResponses);

        return RouterHelper.responseSuccess("Lấy dữ liệu thống kê thành công", statisticDto);
    }

}
