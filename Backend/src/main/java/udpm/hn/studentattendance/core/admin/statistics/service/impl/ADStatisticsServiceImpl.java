package udpm.hn.studentattendance.core.admin.statistics.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ADStatisticsServiceImpl implements ADStatisticsService {

    private final ADSSubjectFacilityExtendRepository subjectFacilityExtendRepository;

    private final ADStatisticsRepository statisticsRepository;

    private final ADSProjectSubjectFacilityRepository projectSubjectFacilityRepository;

    private final RedisService redisService;

    private final RedisInvalidationHelper redisInvalidationHelper;

    @Value("${spring.cache.redis.time-to-live}")
    private long redisTTL;

    public ADSAllStartsAndChartDTO getCachedStatistics(ADStatisticRequest request, int pageNumber) {
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_STATISTICS + "admin_" +
                "request=" + request.toString() +
                "_page=" + pageNumber;

        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            try {
                return redisService.getObject(cacheKey, ADSAllStartsAndChartDTO.class);
            } catch (Exception e) {
                redisService.delete(cacheKey);
            }
        }

        ADStatisticsStatResponse statResponse = statisticsRepository.getAllStatistics(request).orElse(null);
        ADSTotalProjectAndSubjectResponse totalProjectAndSubjectResponse = statisticsRepository
                .getTotalProjectAndSubject().orElse(null);
        if (statResponse == null || totalProjectAndSubjectResponse == null) {
            return null;
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

        try {
            redisService.set(cacheKey, adsAllStartsAndChartDTO, redisTTL);
        } catch (Exception ignored) {
        }

        return adsAllStartsAndChartDTO;
    }

    @Override
    public ResponseEntity<?> getAllListStats(ADStatisticRequest request, int pageNumber) {
        ADSAllStartsAndChartDTO data = getCachedStatistics(request, pageNumber);
        if (data == null) {
            return RouterHelper.responseError("Không thể lấy dữ liệu thống kê");
        }

        return RouterHelper.responseSuccess("Lấy dữ liệu thống kê thành công", data);
    }

    public void invalidateStatisticsCache(String requestId, int pageNumber) {
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_STATISTICS + "admin_" +
                "request=" + requestId +
                "_page=" + pageNumber;
        redisService.delete(cacheKey);
        invalidateAllStatisticsCaches();
    }

    public void invalidateAllStatisticsCaches() {
        redisInvalidationHelper.invalidateAllCaches();
    }
}
