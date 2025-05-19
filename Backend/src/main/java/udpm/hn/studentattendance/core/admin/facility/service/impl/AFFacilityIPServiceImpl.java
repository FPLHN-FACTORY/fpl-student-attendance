package udpm.hn.studentattendance.core.admin.facility.service.impl;

import lombok.RequiredArgsConstructor;
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
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.ValidateHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.IPType;

@Service
@RequiredArgsConstructor
public class AFFacilityIPServiceImpl implements AFFacilityIPService {

    private final AFFacilityExtendRepository afFacilityExtendRepository;

    private final AFFacilityIPRepository afFacilityIPRepository;

    private ResponseEntity<ApiResponse> checkIP(IPType type, String ip) {
        if (type == IPType.DNSSUFFIX) {
            if (!ValidateHelper.isValidDnsSuffix(ip)) {
                return RouterHelper.responseError("DNS Suffix không hợp lệ");
            }
        }
        else if (type == IPType.IPV4) {
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
        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject<AFFacilityIPResponse> data = PageableObject
                .of(afFacilityIPRepository.getAllByFilter(pageable, request));
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
                    .responseError((isDnsSuffix ? "DNS Suffix " : "IP ") + request.getIp() + " đã tồn tại trong cơ sở " + facility.getName());
        }

        IPType type = IPType.fromKey(request.getType());
        String ip = request.getIp();

        ResponseEntity<ApiResponse> checkIP = checkIP(type, ip);
        if (checkIP != null) {
            return checkIP;
        }

        FacilityIP facilityIP = new FacilityIP();
        facilityIP.setFacility(facility);
        facilityIP.setIp(request.getIp());
        facilityIP.setType(type);

        return RouterHelper.responseSuccess("Thêm mới " + (isDnsSuffix ? "DNS Suffix " : "IP ") + request.getIp() + " thành công", afFacilityIPRepository.save(facilityIP));
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
                    .responseError((isDnsSuffix ? "DNS Suffix " : "IP ") + request.getIp() + " đã tồn tại trong cơ sở " + facility.getName());
        }

        IPType type = IPType.fromKey(request.getType());
        String ip = request.getIp();

        ResponseEntity<ApiResponse> checkIP = checkIP(type, ip);
        if (checkIP != null) {
            return checkIP;
        }

        facilityIP.setIp(request.getIp());
        facilityIP.setType(type);

        return RouterHelper.responseSuccess("Cập nhật " + (isDnsSuffix ? "DNS Suffix" : "IP") + " thành công", afFacilityIPRepository.save(facilityIP));
    }

    @Override
    public ResponseEntity<?> deleteIP(String id) {
        FacilityIP facilityIP = afFacilityIPRepository.findById(id).orElse(null);
        if (facilityIP == null) {
            return RouterHelper.responseError("Không tìm thấy IP/DNS Suffix");
        }

        afFacilityIPRepository.delete(facilityIP);
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
            return RouterHelper.responseError((isDnsSuffix ? "DNS Suffix " : "IP ") + facilityIP.getIp() + " đã được áp dụng trong cơ sở");
        }

        facilityIP
                .setStatus(facilityIP.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        return RouterHelper.responseSuccess("Thay đổi trạng thái " + (isDnsSuffix ? "DNS Suffix" : "IP") + " thành công",
                afFacilityIPRepository.save(facilityIP));
    }

}
