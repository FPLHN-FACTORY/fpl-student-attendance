package udpm.hn.studentattendance.core.admin.statistics.service.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.admin.statistics.model.dto.ADSAllStartsAndChartDTO;
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

    public ADSAllStartsAndChartDTO getCachedStatistics() {

        ADStatisticsStatResponse statResponse = statisticsRepository.getAllStatistics().orElse(null);
        ADSTotalProjectAndSubjectResponse totalProjectAndSubjectResponse = statisticsRepository
                .getTotalProjectAndSubject().orElse(null);
        if (statResponse == null || totalProjectAndSubjectResponse == null) {
            return null;
        }

        List<ADSSubjectFacilityChartResponse> subjectFacilityChartResponseList = subjectFacilityExtendRepository
                .getSubjectByFacility();


        ADSAllStartsAndChartDTO adsAllStartsAndChartDTO = new ADSAllStartsAndChartDTO();
        adsAllStartsAndChartDTO.setStatisticsStatResponse(statResponse);
        adsAllStartsAndChartDTO.setSubjectFacilityChartResponse(subjectFacilityChartResponseList);
        adsAllStartsAndChartDTO.setTotalProjectAndSubjectResponse(totalProjectAndSubjectResponse);


        return adsAllStartsAndChartDTO;
    }

    @Override
    public ResponseEntity<?> getAllListStats() {
        ADSAllStartsAndChartDTO data = getCachedStatistics();
        if (data == null) {
            return RouterHelper.responseError("Không thể lấy dữ liệu thống kê");
        }

        return RouterHelper.responseSuccess("Lấy dữ liệu thống kê thành công", data);
    }

    @Override
    public ResponseEntity<?> getLineChartStats(int year) {
        List<ADSProjectSubjectFacilityResponse> list = projectSubjectFacilityRepository.getProjectSubjectFacilityResponses(year);
        return RouterHelper.responseSuccess(" Lấy thống kê năm " + year + " thành công", list);
    }

}
