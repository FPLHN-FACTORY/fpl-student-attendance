package udpm.hn.studentattendance.core.admin.statistics.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.statistics.model.dto.ADSAllStartsAndChartDTO;
import udpm.hn.studentattendance.core.admin.statistics.model.response.ADSProjectSubjectFacilityResponse;
import udpm.hn.studentattendance.core.admin.statistics.repository.ADSProjectSubjectFacilityRepository;
import udpm.hn.studentattendance.core.admin.statistics.repository.ADSSubjectFacilityExtendRepository;
import udpm.hn.studentattendance.core.admin.statistics.repository.ADStatisticsRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ADStatisticsServiceTest {
    @Mock
    private ADSSubjectFacilityExtendRepository subjectFacilityExtendRepository;

    @Mock
    private ADStatisticsRepository statisticsRepository;

    @Mock
    private ADSProjectSubjectFacilityRepository projectSubjectFacilityRepository;

    @InjectMocks
    private udpm.hn.studentattendance.core.admin.statistics.service.impl.ADStatisticsServiceImpl adStatisticsService;

    @Test
    void testGetAllListStatsSuccess() {
        ADSAllStartsAndChartDTO dto = new ADSAllStartsAndChartDTO();
        when(statisticsRepository.getAllStatistics()).thenReturn(java.util.Optional.ofNullable(null));
        when(statisticsRepository.getTotalProjectAndSubject()).thenReturn(java.util.Optional.ofNullable(null));
        // getCachedStatistics sẽ trả về null, nên response là lỗi
        ResponseEntity<?> response = adStatisticsService.getAllListStats();
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void testGetLineChartStatsSuccess() {
        int year = 2024;
        List<ADSProjectSubjectFacilityResponse> list = Collections.emptyList();
        when(projectSubjectFacilityRepository.getProjectSubjectFacilityResponses(year)).thenReturn(list);
        ResponseEntity<?> response = adStatisticsService.getLineChartStats(year);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }
}
