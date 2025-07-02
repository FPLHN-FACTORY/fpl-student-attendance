package udpm.hn.studentattendance.core.admin.facility.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFAddOrUpdateFacilityIPRequest;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFFilterFacilityIPRequest;
import udpm.hn.studentattendance.core.admin.facility.model.response.AFFacilityIPResponse;
import udpm.hn.studentattendance.core.admin.facility.repository.AFFacilityExtendRepository;
import udpm.hn.studentattendance.core.admin.facility.repository.AFFacilityIPRepository;
import udpm.hn.studentattendance.core.admin.facility.service.AFFacilityIPService;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.FacilityIP;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.ValidateHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.IPType;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;

@Service
@RequiredArgsConstructor
public class AFFacilityIPServiceImpl implements AFFacilityIPService {

    private final AFFacilityExtendRepository afFacilityExtendRepository;

    private final AFFacilityIPRepository afFacilityIPRepository;

    private final UserActivityLogHelper userActivityLogHelper;

    private final RedisService redisService;

    private final RedisInvalidationHelper redisInvalidationHelper;

    @Value("${REDIS_TTL}")
    private long redisTTL;

    public PageableObject<AFFacilityIPResponse> getIPList(AFFilterFacilityIPRequest request) {
        // Tạo cache key thủ công
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_FACILITY_IP + "list_" +
                "page=" + request.getPage() +
                "_size=" + request.getSize() +
                "_orderBy=" + request.getOrderBy() +
                "_sortBy=" + request.getSortBy() +
                "_q=" + (request.getQ() != null ? request.getQ() : "");

        // Kiểm tra cache
        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            try {
                // Thử lấy dữ liệu từ cache sử dụng getObject
                return redisService.getObject(cacheKey, PageableObject.class);
            } catch (Exception e) {
                // Nếu lỗi, xóa cache entry và tiếp tục lấy dữ liệu mới
                redisService.delete(cacheKey);
            }
        }

        // Cache miss - fetch from database
        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject<AFFacilityIPResponse> data = PageableObject.of(
                afFacilityIPRepository.getAllByFilter(pageable, request));

        // Store in cache - sử dụng set với thời gian sống cụ thể
        try {
            redisService.set(cacheKey, data, redisTTL);
        } catch (Exception ignored) {
        }

        return data;
    }

    private ResponseEntity<ApiResponse> checkIP(IPType type, String ip) {
        if (type == IPType.DNSSUFFIX) {
            if (!ValidateHelper.isValidDnsSuffix(ip)) {
                return RouterHelper.responseError("DNS Suffix không hợp lệ");
            }
        } else if (type == IPType.IPV4) {
            if (!ip.contains("/")) {
                if (!ValidateHelper.isValidIPv4(ip)) {
                    return RouterHelper.responseError("IPv4 không hợp lệ");
                }
            } else {
                if (!ValidateHelper.isValidIPv4CIDR(ip)) {
                    return RouterHelper.responseError("Dải IPv4 không hợp lệ");
                }
            }
        } else {
            if (!ip.contains("/")) {
                if (!ValidateHelper.isValidIPv6(ip)) {
                    return RouterHelper.responseError("IPv6 không hợp lệ");
                }
            } else {
                if (!ValidateHelper.isValidIPv6CIDR(ip)) {
                    return RouterHelper.responseError("Dải IPv6 không hợp lệ");
                }
            }
        }
        return null;
    }

    @Override
    public ResponseEntity<?> getAllList(AFFilterFacilityIPRequest request) {
        PageableObject<AFFacilityIPResponse> data = getIPList(request);
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    @Override
    public ResponseEntity<?> addIP(AFAddOrUpdateFacilityIPRequest request) {

        Facility facility = afFacilityExtendRepository.findById(request.getIdFacility()).orElse(null);

        if (facility == null || facility.getStatus() != EntityStatus.ACTIVE) {
            return RouterHelper.responseError("Không tìm cơ sở");
        }

        boolean isDnsSuffix = request.getType() == IPType.DNSSUFFIX.getKey();

        if (afFacilityIPRepository.isExistsIP(request.getIp(), request.getType(), request.getIdFacility(), null)) {
            return RouterHelper
                    .responseError((isDnsSuffix ? "DNS Suffix " : "IP ") + request.getIp() + " đã tồn tại trong cơ sở "
                            + facility.getName());
        }

        IPType type = IPType.fromKey(request.getType());
        String ip = request.getIp().trim();

        ResponseEntity<ApiResponse> checkIP = checkIP(type, ip);
        if (checkIP != null) {
            return checkIP;
        }

        FacilityIP facilityIP = new FacilityIP();
        facilityIP.setFacility(facility);
        facilityIP.setIp(request.getIp());
        facilityIP.setType(type);
        userActivityLogHelper.saveLog("vừa thêm " + (isDnsSuffix ? "DNS Suffix " : "IP ") + request.getIp()
                + " cho cơ sở " + facility.getName());

        FacilityIP savedIP = afFacilityIPRepository.save(facilityIP);

        // Invalidate all caches
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess(
                "Thêm mới " + (isDnsSuffix ? "DNS Suffix " : "IP ") + request.getIp() + " thành công",
                savedIP);
    }

    @Override
    public ResponseEntity<?> updateIP(AFAddOrUpdateFacilityIPRequest request) {
        FacilityIP facilityIP = afFacilityIPRepository.findById(request.getId()).orElse(null);
        if (facilityIP == null) {
            return RouterHelper.responseError("Không tìm thấy IP/DNS Suffix muốn cập nhật");
        }

        Facility facility = afFacilityExtendRepository.findById(request.getIdFacility()).orElse(null);

        if (facility == null || facility.getStatus() != EntityStatus.ACTIVE) {
            return RouterHelper.responseError("Không tìm cơ sở");
        }

        boolean isDnsSuffix = request.getType() == IPType.DNSSUFFIX.getKey();

        if (afFacilityIPRepository.isExistsIP(request.getIp(), request.getType(), request.getIdFacility(),
                facilityIP.getId())) {
            return RouterHelper
                    .responseError((isDnsSuffix ? "DNS Suffix " : "IP ") + request.getIp() + " đã tồn tại trong cơ sở "
                            + facility.getName());
        }

        IPType type = IPType.fromKey(request.getType());
        String ip = request.getIp().trim();

        ResponseEntity<ApiResponse> checkIP = checkIP(type, ip);
        if (checkIP != null) {
            return checkIP;
        }

        facilityIP.setIp(request.getIp());
        facilityIP.setType(type);
        userActivityLogHelper.saveLog("vừa cập nhật " + (isDnsSuffix ? "DNS Suffix " : "IP ") + request.getIp()
                + " của cơ sở " + facility.getName());

        FacilityIP savedIP = afFacilityIPRepository.save(facilityIP);

        // Invalidate all caches
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Cập nhật " + (isDnsSuffix ? "DNS Suffix" : "IP") + " thành công",
                savedIP);
    }

    @Override
    public ResponseEntity<?> deleteIP(String id) {
        FacilityIP facilityIP = afFacilityIPRepository.findById(id).orElse(null);
        if (facilityIP == null) {
            return RouterHelper.responseError("Không tìm thấy IP/DNS Suffix");
        }

        afFacilityIPRepository.delete(facilityIP);
        userActivityLogHelper.saveLog("vừa xóa " + (facilityIP.getType() == IPType.DNSSUFFIX ? "DNS Suffix " : "IP ")
                + facilityIP.getIp() + " của cơ sở " + facilityIP.getFacility().getName());

        // Invalidate all caches
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Xoá thành công IP/DNS Suffix: " + facilityIP.getIp());
    }

    @Override
    public ResponseEntity<?> changeStatus(String id) {
        FacilityIP facilityIP = afFacilityIPRepository.findById(id).orElse(null);
        if (facilityIP == null) {
            return RouterHelper.responseError("Không tìm thấy IP/DNS Suffix");
        }

        boolean isDnsSuffix = facilityIP.getType() == IPType.DNSSUFFIX;
        if (facilityIP.getStatus() == EntityStatus.INACTIVE && afFacilityIPRepository.isExistsIP(facilityIP.getIp(),
                facilityIP.getType().getKey(), facilityIP.getFacility().getId(), facilityIP.getId())) {
            return RouterHelper.responseError(
                    (isDnsSuffix ? "DNS Suffix " : "IP ") + facilityIP.getIp() + " đã được áp dụng trong cơ sở");
        }

        facilityIP
                .setStatus(facilityIP.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        FacilityIP updatedIP = afFacilityIPRepository.save(facilityIP);
        userActivityLogHelper.saveLog(
                "vừa thay đổi trạng thái " + (isDnsSuffix ? "DNS Suffix " : "IP ") + facilityIP.getIp() + " của cơ sở "
                        + facilityIP.getFacility().getName() + " thành " + updatedIP.getStatus().name());

        // Invalidate all caches
        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess(
                "Thay đổi trạng thái " + (isDnsSuffix ? "DNS Suffix" : "IP") + " thành công", updatedIP);
    }

}
