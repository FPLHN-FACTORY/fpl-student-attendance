package udpm.hn.studentattendance.core.admin.subjectfacility.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.admin.subjectfacility.repository.ADFacilityRepository;
import udpm.hn.studentattendance.core.admin.subjectfacility.service.ADFacilityManagementService;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ADFacilityManagementServiceImpl implements ADFacilityManagementService {

    private final ADFacilityRepository repository;
    private final RedisService redisService;
    private final RedisInvalidationHelper redisInvalidationHelper;

    @Value("${spring.cache.redis.time-to-live}")
    private long redisTTL;

    private List<?> getCachedFacilityCombobox(String idSubject) {
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_FACILITY + "combobox_" + idSubject;

        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            try {
                return redisService.getObject(cacheKey, List.class);
            } catch (Exception e) {
                redisService.delete(cacheKey);
            }
        }

        List<?> facilities = repository.getFacility(idSubject);

        try {
            redisService.set(cacheKey, facilities, redisTTL);
        } catch (Exception ignored) {
        }

        return facilities;
    }

    private List<?> getCachedActiveFacilities() {
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_FACILITY + "active_list";

        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            try {
                return redisService.getObject(cacheKey, List.class);
            } catch (Exception e) {
                redisService.delete(cacheKey);
            }
        }

        List<?> facilities = repository.getFacilities(EntityStatus.ACTIVE);

        try {
            redisService.set(cacheKey, facilities, redisTTL);
        } catch (Exception ignored) {
        }

        return facilities;
    }

    @Override
    public ResponseEntity<?> getComboboxFacility(String idSubject) {
        List<?> facilities = getCachedFacilityCombobox(idSubject);
        return RouterHelper.responseSuccess("Lấy danh sách cơ sở thành công", facilities);
    }

    @Override
    public ResponseEntity<?> getListFacility() {
        List<?> facilities = getCachedActiveFacilities();
        return RouterHelper.responseSuccess("Lấy danh sách cơ sở để lọc thành công", facilities);
    }

}
