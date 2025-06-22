package udpm.hn.studentattendance.core.admin.userstaff.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.admin.userstaff.model.request.ADCreateUpdateStaffRequest;
import udpm.hn.studentattendance.core.admin.userstaff.model.request.ADStaffRequest;
import udpm.hn.studentattendance.core.admin.userstaff.model.response.ADStaffDetailResponse;
import udpm.hn.studentattendance.core.admin.userstaff.repository.ADStaffFacilityExtendRepository;
import udpm.hn.studentattendance.core.admin.userstaff.repository.ADStaffExtendRepository;
import udpm.hn.studentattendance.core.admin.userstaff.repository.ADStaffRoleExtendRepository;
import udpm.hn.studentattendance.core.admin.userstaff.service.ADStaffService;
import udpm.hn.studentattendance.core.notification.model.request.NotificationAddRequest;
import udpm.hn.studentattendance.core.notification.service.NotificationService;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.Role;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.helpers.NotificationHelper;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.helpers.ValidateHelper;

import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Validated
public class ADStaffServiceImpl implements ADStaffService {
    private final ADStaffExtendRepository adStaffRepository;

    private final ADStaffRoleExtendRepository adStaffRoleRepository;

    private final ADStaffFacilityExtendRepository adminStaffFacilityRepository;

    private final NotificationService notificationService;

    private final SessionHelper sessionHelper;

    private final UserActivityLogHelper userActivityLogHelper;
    private final RedisService redisService;

    @Value("${app.config.disabled-check-email-fpt}")
    private String isDisableCheckEmailFpt;

    @Value("${spring.cache.redis.time-to-live:3600}")
    private long redisTTL;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public ResponseEntity<?> getAllStaffByFilter(ADStaffRequest adStaffRequest) {
        String cacheKey = "staff:list:" + adStaffRequest.toString();

        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            return RouterHelper.responseSuccess("Lấy tất cả giảng viên thành công (cached)", cachedData);
        }

        Pageable pageable = PaginationHelper.createPageable(adStaffRequest, "createdAt");
        PageableObject staffs = PageableObject.of(adStaffRepository.getAllStaff(pageable, adStaffRequest));

        redisService.set(cacheKey, staffs, redisTTL);

        return RouterHelper.responseSuccess("Lấy tất cả giảng viên thành công", staffs);
    }

    @Override
    public ResponseEntity<?> createStaff(ADCreateUpdateStaffRequest adCreateUpdateStaffRequest) {

        if (!ValidateHelper.isValidCode(adCreateUpdateStaffRequest.getStaffCode())) {
            return RouterHelper.responseError(
                    "Mã nhân viên không hợp lệ: Không có khoảng trắng, không có ký tự đặc biệt ngoài dấu chấm . và dấu gạch dưới _.");
        }

        if (!ValidateHelper.isValidFullname(adCreateUpdateStaffRequest.getName())) {
            return RouterHelper.responseError(
                    "Tên nhân viên không hợp lệ: Tối thiểu 2 từ, cách nhau bởi khoảng trắng và Chỉ gồm ký tự chữ không chứa số hay ký tự đặc biệt.");
        }

        if (!isDisableCheckEmailFpt.equalsIgnoreCase("true")) {
            if (!ValidateHelper.isValidEmailFE(adCreateUpdateStaffRequest.getEmailFe().trim())) {
                return RouterHelper
                        .responseError("Email FE không được chứa khoảng trắng và phải kết thúc bằng @fe.edu.vn");
            }
            if (!ValidateHelper.isValidEmailFPT(adCreateUpdateStaffRequest.getEmailFpt().trim())) {
                return RouterHelper
                        .responseError("Email FPT không được chứa khoảng trắng và phải kết thúc bằng @fpt.edu.vn");
            }
        }

        UserStaff staffExist = isStaffExist(
                adCreateUpdateStaffRequest.getStaffCode(),
                adCreateUpdateStaffRequest.getEmailFe(),
                adCreateUpdateStaffRequest.getEmailFpt());
        if (staffExist != null) {
            return RouterHelper.responseError("Nhân viên đã tồn tại");
        }

        UserStaff staff = new UserStaff();
        staff.setCode(adCreateUpdateStaffRequest.getStaffCode().trim());
        staff.setName(adCreateUpdateStaffRequest.getName().trim());
        staff.setEmailFe(adCreateUpdateStaffRequest.getEmailFe().trim());
        staff.setEmailFpt(adCreateUpdateStaffRequest.getEmailFpt().trim());
        staff.setStatus(EntityStatus.ACTIVE);
        staff = adStaffRepository.save(staff);

        Facility facility = entityManager.find(Facility.class, adCreateUpdateStaffRequest.getFacilityId());
        if (facility == null) {
            return RouterHelper.responseError("Cơ sở không tồn tại");
        }

        List<String> roleCodes = adCreateUpdateStaffRequest.getRoleCodes();
        if (roleCodes == null || roleCodes.isEmpty()) {
            return RouterHelper.responseError("Phải chọn ít nhất một vai trò");
        }

        RoleConstant[] roleConstants = RoleConstant.values(); // Mảng các giá trị enum
        for (String roleCode : roleCodes) {
            RoleConstant roleConstant;
            try {
                int ordinal = Integer.parseInt(roleCode.trim());
                if (ordinal < 0 || ordinal >= roleConstants.length) {
                    throw new IllegalArgumentException("Ordinal out of range: " + roleCode);
                }
                roleConstant = roleConstants[ordinal];
            } catch (IllegalArgumentException e) {
                return RouterHelper.responseError("Vai trò không hợp lệ: " + roleCode);
            }

            Role role = new Role();
            role.setCode(roleConstant);
            role.setFacility(facility);
            role.setUserStaff(staff);
            role.setStatus(EntityStatus.ACTIVE);
            adStaffRoleRepository.save(role);

            Map<String, Object> dataNotification = new HashMap<>();
            dataNotification.put(NotificationHelper.KEY_USER_ADMIN,
                    sessionHelper.getUserCode() + " - " + sessionHelper.getUserName());
            dataNotification.put(NotificationHelper.KEY_ROLE, roleConstant.name());
            NotificationAddRequest notificationAddRequest = new NotificationAddRequest();
            notificationAddRequest.setIdUser(staff.getId());
            notificationAddRequest.setType(NotificationHelper.TYPE_ADD_ROLE);
            notificationAddRequest.setData(dataNotification);
            notificationService.add(notificationAddRequest);
        }

        userActivityLogHelper.saveLog("vừa thêm 1 nhân viên mới: " + staff.getName() + " (" + staff.getCode() + ")");

        // Invalidate staff-related caches
        invalidateStaffCaches();

        return RouterHelper.responseSuccess("Thêm nhân viên mới thành công");
    }

    @Override
    public ResponseEntity<?> updateStaff(ADCreateUpdateStaffRequest adCreateUpdateStaffRequest, String id) {

        if (!ValidateHelper.isValidCode(adCreateUpdateStaffRequest.getStaffCode())) {
            return RouterHelper.responseError(
                    "Mã nhân viên không hợp lệ: Không có khoảng trắng, không có ký tự đặc biệt ngoài dấu chấm . và dấu gạch dưới _.");
        }

        if (!ValidateHelper.isValidFullname(adCreateUpdateStaffRequest.getName())) {
            return RouterHelper.responseError(
                    "Tên nhân viên không hợp lệ: Tối thiểu 2 từ, cách nhau bởi khoảng trắng và Chỉ gồm ký tự chữ không chứa số hay ký tự đặc biệt.");
        }

        // Kiểm tra nhân viên tồn tại
        Optional<UserStaff> opt = adStaffRepository.findById(id);
        if (opt.isEmpty()) {
            return RouterHelper.responseError("Không tìm thấy nhân viên");
        }
        UserStaff current = opt.get();

        // 2. Check trùng code
        if (adStaffRepository.isExistCodeUpdate(adCreateUpdateStaffRequest.getStaffCode(), current.getCode())) {
            return RouterHelper.responseError("Mã nhân viên đã tồn tại");
        }
        // 3. Check trùng email FE
        if (adStaffRepository.isExistEmailFeUpdate(adCreateUpdateStaffRequest.getEmailFe(), current.getEmailFe())) {
            return RouterHelper.responseError("Đã có nhân viên khác dùng email fe này");
        }
        // 4. Check trùng email FPT
        if (adStaffRepository.isExistEmailFptUpdate(adCreateUpdateStaffRequest.getEmailFpt(), current.getEmailFpt())) {
            return RouterHelper.responseError("Đã có nhân viên khác dùng email fpt này");
        }

        // Kiểm tra định dạng email
        if (!isDisableCheckEmailFpt.equalsIgnoreCase("true")) {
            if (!ValidateHelper.isValidEmailFE(adCreateUpdateStaffRequest.getEmailFe().trim())) {
                return RouterHelper.responseError("Không chứa khoảng trắng và kết thúc bằng @fe.edu.vn");
            }
            if (!ValidateHelper.isValidEmailFPT(adCreateUpdateStaffRequest.getEmailFpt().trim())) {
                return RouterHelper.responseError("Không chứa khoảng trắng và kết thúc bằng @fpt.edu.vn");
            }
        }

        // Cập nhật thông tin nhân viên
        UserStaff staff = opt.get();
        staff.setName(adCreateUpdateStaffRequest.getName().trim());
        staff.setCode(adCreateUpdateStaffRequest.getStaffCode().trim());
        staff.setEmailFe(adCreateUpdateStaffRequest.getEmailFe().trim());
        staff.setEmailFpt(adCreateUpdateStaffRequest.getEmailFpt().trim());
        adStaffRepository.save(staff);

        // Kiểm tra cơ sở
        Optional<Facility> existFacility = adminStaffFacilityRepository
                .findById(adCreateUpdateStaffRequest.getFacilityId().trim());
        if (existFacility.isEmpty()) {
            return RouterHelper.responseError("Cơ sở không tồn tại");
        }
        Facility facility = existFacility.get();

        // Xử lý vai trò
        List<Role> currentRoles = adStaffRoleRepository.findAllByUserStaffId(staff.getId());
        List<String> newRoleCodes = adCreateUpdateStaffRequest.getRoleCodes();
        RoleConstant[] roleConstants = RoleConstant.values();

        // Xóa vai trò không còn trong danh sách mới
        for (Role role : currentRoles) {
            int currentRoleOrdinal = role.getCode().ordinal();
            String currentRoleString = String.valueOf(currentRoleOrdinal);
            if (!newRoleCodes.contains(currentRoleString)) {
                adStaffRoleRepository.delete(role);

                Map<String, Object> dataNotification = new HashMap<>();
                dataNotification.put(NotificationHelper.KEY_USER_ADMIN,
                        sessionHelper.getUserCode() + " - " + sessionHelper.getUserName());
                dataNotification.put(NotificationHelper.KEY_ROLE, role.getCode().name());
                NotificationAddRequest notificationAddRequest = new NotificationAddRequest();
                notificationAddRequest.setIdUser(staff.getId());
                notificationAddRequest.setType(NotificationHelper.TYPE_REMOVE_ROLE);
                notificationAddRequest.setData(dataNotification);
                notificationService.add(notificationAddRequest);
            }
        }

        // Cập nhật hoặc tạo mới vai trò
        for (String roleCode : newRoleCodes) {
            RoleConstant roleConstant;
            try {
                int ordinal = Integer.parseInt(roleCode.trim());
                if (ordinal < 0 || ordinal >= roleConstants.length) {
                    throw new IllegalArgumentException("Ordinal out of range: " + roleCode);
                }
                roleConstant = roleConstants[ordinal];
            } catch (IllegalArgumentException e) {
                return RouterHelper.responseError("Vai trò không hợp lệ: " + roleCode);
            }

            Optional<Role> existingRole = currentRoles.stream()
                    .filter(r -> r.getCode() == roleConstant)
                    .findFirst();

            Role role;
            if (existingRole.isPresent()) {
                role = existingRole.get();
                role.setFacility(facility);
            } else {
                role = new Role();
                role.setCode(roleConstant);
                role.setFacility(facility);
                role.setUserStaff(staff);
            }
            role.setStatus(EntityStatus.ACTIVE);
            adStaffRoleRepository.save(role);

            Map<String, Object> dataNotification = new HashMap<>();
            dataNotification.put(NotificationHelper.KEY_USER_ADMIN,
                    sessionHelper.getUserCode() + " - " + sessionHelper.getUserName());
            dataNotification.put(NotificationHelper.KEY_ROLE, role.getCode().name());
            NotificationAddRequest notificationAddRequest = new NotificationAddRequest();
            notificationAddRequest.setIdUser(staff.getId());
            notificationAddRequest.setType(NotificationHelper.TYPE_ADD_ROLE);
            notificationAddRequest.setData(dataNotification);
            notificationService.add(notificationAddRequest);
        }

        userActivityLogHelper.saveLog("vừa cập nhật nhân viên: " + staff.getName() + " (" + staff.getCode() + ")");

        // Invalidate specific cache for this staff
        redisService.delete("staff:detail:" + id);
        // Invalidate related caches
        invalidateStaffCaches();

        return RouterHelper.responseSuccess("Cập nhật nhân viên thành công");
    }

    @Override
    public ResponseEntity<?> changeStaffStatus(String staffId) {
        Optional<UserStaff> existStaff = adStaffRepository.findById(staffId);
        if (existStaff.isEmpty()) {
            return RouterHelper.responseError("Nhân viên không tồn tại");
        }
        UserStaff staff = existStaff.get();
        String oldStatus = staff.getStatus() == EntityStatus.ACTIVE ? "Hoạt động" : "Không hoạt động";
        staff.setStatus(staff.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        String newStatus = staff.getStatus() == EntityStatus.ACTIVE ? "Hoạt động" : "Không hoạt động";

        List<Role> staffRoles = adStaffRoleRepository.findAllByUserStaffId(staffId);
        staffRoles.forEach(staffRole -> staffRole
                .setStatus(staff.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE));
        adStaffRoleRepository.saveAll(staffRoles);

        adStaffRepository.save(staff);
        userActivityLogHelper.saveLog("vừa thay đổi trạng thái nhân viên " + staff.getName() + " (" + staff.getCode()
                + ") từ " + oldStatus + " thành " + newStatus);

        // Invalidate specific cache for this staff
        redisService.delete("staff:detail:" + staffId);
        // Invalidate related caches
        invalidateStaffCaches();

        return RouterHelper.responseSuccess("Thay đổi trạng thái giảng viên thành công");
    }

    @Override
    public ResponseEntity<?> getStaffById(String staffId) {
        String cacheKey = "staff:detail:" + staffId;

        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            return RouterHelper.responseSuccess("Xem chi tiết giảng viên thành công (cached)", cachedData);
        }

        Optional<ADStaffDetailResponse> existStaff = adStaffRepository.getDetailStaff(staffId);
        if (existStaff.isPresent()) {
            redisService.set(cacheKey, existStaff.get(), redisTTL);
            return RouterHelper.responseSuccess("Xem chi tiết giảng viên thành công", existStaff);
        }
        return RouterHelper.responseError("Giảng viên không tồn tại");
    }

    @Override
    public ResponseEntity<?> getAllRole() {
        String cacheKey = "staff:roles:all";

        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            return RouterHelper.responseSuccess("Lấy tất cả vai trò thành công (cached)", cachedData);
        }

        List<Role> getAllRole = adStaffRoleRepository.getAllRole();

        redisService.set(cacheKey, getAllRole, redisTTL * 4);

        return RouterHelper.responseSuccess("Lấy tất cả vai trò thành công", getAllRole);
    }

    @Override
    public ResponseEntity<?> getAllFacility() {
        String cacheKey = "staff:facilities:active";

        Object cachedData = redisService.get(cacheKey);
        if (cachedData != null) {
            return RouterHelper.responseSuccess("Lấy tất cả cơ sở thành công (cached)", cachedData);
        }

        List<Facility> getAllFacility = adminStaffFacilityRepository.getFacility(EntityStatus.ACTIVE);

        redisService.set(cacheKey, getAllFacility, redisTTL * 4);

        return RouterHelper.responseSuccess("Lấy tất cả cơ sở thành công", getAllFacility);
    }

    private UserStaff isStaffExist(
            String staffCode,
            String accountFe,
            String accountFpt) {
        Optional<UserStaff> staffs = adStaffRepository.findUserStaffByCode(staffCode);
        if (staffs.isPresent()) {
            return staffs.get();
        }

        Optional<UserStaff> staffFe = adStaffRepository.findUserStaffByEmailFe(accountFe);
        if (staffFe.isPresent()) {
            return staffFe.get();
        }

        Optional<UserStaff> staffFpt = adStaffRepository.findUserStaffByEmailFpt(accountFpt);
        return staffFpt.orElse(null);
    }

    /**
     * Helper method to invalidate staff-related caches
     */
    private void invalidateStaffCaches() {
        redisService.deletePattern("staff:list:*");
    }
}
