package udpm.hn.studentattendance.core.student.statistics.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.student.statistics.service.impl.STDStatisticsServiceImpl;
import udpm.hn.studentattendance.core.student.statistics.repository.STDStatisticsSemesterRepository;
import udpm.hn.studentattendance.core.student.statistics.repository.STDSFactoryLineChartRepository;
import udpm.hn.studentattendance.core.student.statistics.repository.STDSFactoryAttendanceBarChartRepository;
import udpm.hn.studentattendance.helpers.SessionHelper;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class STDStatisticsServiceTest {
    @Mock
    private STDStatisticsSemesterRepository stdStatisticsSemesterRepository;

    @Mock
    private STDSFactoryLineChartRepository factoryLineChartRepository;

    @Mock
    private STDSFactoryAttendanceBarChartRepository attendanceBarChartRepository;

    @Mock
    private SessionHelper sessionHelper;

    @InjectMocks
    private STDStatisticsServiceImpl stdStatisticsService;

    @Test
    void testSTDStatisticsServiceExists() {
        assertNotNull(stdStatisticsService);
    }

    @Test
    void testGetStatistics() {
        String idSemester = "2024-1";
        when(sessionHelper.getFacilityId()).thenReturn("facility-1");
        when(sessionHelper.getUserId()).thenReturn("user-1");
        when(stdStatisticsSemesterRepository.getAllStatisticBySemester(anyString(), anyString(), anyString()))
                .thenReturn(java.util.Optional.empty());
        when(factoryLineChartRepository.getAttendancePercentage(anyString(), anyString(), anyString()))
                .thenReturn(java.util.Collections.emptyList());
        when(attendanceBarChartRepository.getAttendanceBarChart(anyString(), anyString()))
                .thenReturn(java.util.Optional.empty());

        ResponseEntity<?> response = stdStatisticsService.getStatistics(idSemester);

        assertNotNull(response);
        verify(sessionHelper, times(2)).getFacilityId();
        verify(sessionHelper, times(3)).getUserId();
        verify(stdStatisticsSemesterRepository).getAllStatisticBySemester("facility-1", "user-1", idSemester);
        verify(factoryLineChartRepository).getAttendancePercentage("facility-1", "user-1", idSemester);
        verify(attendanceBarChartRepository).getAttendanceBarChart("user-1", idSemester);
    }
}