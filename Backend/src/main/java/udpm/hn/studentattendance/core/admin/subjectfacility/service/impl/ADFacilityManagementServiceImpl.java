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
import com.fasterxml.jackson.core.type.TypeReference;
import udpm.hn.studentattendance.helpers.RedisCacheHelper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ADFacilityManagementServiceImpl implements ADFacilityManagementService {

    private final ADFacilityRepository repository;
    private final RedisCacheHelper redisCacheHelper;
    private final RedisInvalidationHelper redisInvalidationHelper;

    @Value("${spring.cache.redis.time-to-live}")
    private long redisTTL;

    private List<?> getCachedFacilityCombobox(String idSubject) {
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_FACILITY + "combobox_" + idSubject;
        return redisCacheHelper.getOrSet(
                cacheKey,
                () -> repository.getFacility(idSubject),
                new TypeReference<List<?>>() {
                },
                redisTTL);
    }

    private List<?> getCachedActiveFacilities() {
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_FACILITY + "active_list";
        return redisCacheHelper.getOrSet(
                cacheKey,
                () -> repository.getFacilities(EntityStatus.ACTIVE),
                new TypeReference<List<?>>() {
                },
                redisTTL);
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
