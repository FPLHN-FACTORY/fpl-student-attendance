package udpm.hn.studentattendance.core.admin.statistics.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.admin.statistics.model.dto.ADSAllStartsAndChartDTO;
import udpm.hn.studentattendance.core.admin.statistics.model.request.ADStatisticRequest;
import udpm.hn.studentattendance.core.admin.statistics.model.response.ADSProjectSubjectFacilityResponse;
import udpm.hn.studentattendance.core.admin.statistics.model.response.ADSSubjectFacilityChartResponse;
import udpm.hn.studentattendance.core.admin.statistics.model.response.ADStatisticsStatResponse;
import udpm.hn.studentattendance.core.admin.statistics.model.response.ADSTotalProjectAndSubjectResponse;
import udpm.hn.studentattendance.core.admin.statistics.repository.*;
import udpm.hn.studentattendance.core.admin.statistics.service.ADStatisticsService;
import udpm.hn.studentattendance.helpers.RouterHelper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ADStatisticsServiceImpl implements ADStatisticsService {

    private final ADSSubjectFacilityExtendRepository subjectFacilityExtendRepository;

    private final ADStatisticsRepository statisticsRepository;

    private final ADSProjectSubjectFacilityRepository projectSubjectFacilityRepository;

    @Override
    public ResponseEntity<?> getAllListStats(ADStatisticRequest request, int pageNumber) {
        ADStatisticsStatResponse statResponse = statisticsRepository.getAllStatistics(request).orElse(null);
        ADSTotalProjectAndSubjectResponse totalProjectAndSubjectResponse = statisticsRepository
                .getTotalProjectAndSubject().orElse(null);
        if (statResponse == null || totalProjectAndSubjectResponse == null) {
            return RouterHelper.responseError("Không thể lấy dữ liệu thống kê");
        }

        List<ADSSubjectFacilityChartResponse> subjectFacilityChartResponseList = subjectFacilityExtendRepository
                .getSubjectByFacility();
        Pageable pageable = PageRequest.of(pageNumber, 5);
        Page<ADSProjectSubjectFacilityResponse> projectSubjectFacilityResponseList = projectSubjectFacilityRepository
                .getProjectSubjectFacilityResponses(pageable);

        ADSAllStartsAndChartDTO adsAllStartsAndChartDTO = new ADSAllStartsAndChartDTO();
        adsAllStartsAndChartDTO.setStatisticsStatResponse(statResponse);
        adsAllStartsAndChartDTO.setSubjectFacilityChartResponse(subjectFacilityChartResponseList);
        adsAllStartsAndChartDTO.setProjectSubjectFacilityResponses(projectSubjectFacilityResponseList);
        adsAllStartsAndChartDTO.setTotalProjectAndSubjectResponse(totalProjectAndSubjectResponse);
        return RouterHelper.responseSuccess(" Lấy dữ liệu thống kê thành công", adsAllStartsAndChartDTO);
    }
}
