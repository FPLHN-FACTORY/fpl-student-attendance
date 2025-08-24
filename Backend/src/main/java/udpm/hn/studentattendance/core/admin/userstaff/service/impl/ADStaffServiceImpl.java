package udpm.hn.studentattendance.core.admin.userstaff.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
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
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.SettingHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.helpers.ValidateHelper;
import udpm.hn.studentattendance.helpers.RedisCacheHelper;

import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;
import udpm.hn.studentattendance.infrastructure.constants.SettingKeys;

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

    private final RedisCacheHelper redisCacheHelper;

    private final RedisInvalidationHelper redisInvalidationHelper;

    private final SettingHelper settingHelper;

    @PersistenceContext
    private EntityManager entityManager;

    public PageableObject getStaffList(ADStaffRequest request) {
        String key = RedisPrefixConstant.REDIS_PREFIX_STAFF + "list_" + request.toString();
        return redisCacheHelper.getOrSet(
                key,
                () -> PageableObject.of(
                        adStaffRepository.getAllStaff(PaginationHelper.createPageable(request, "createdAt"), request)),
                new TypeReference<>() {
                });
    }

    public ADStaffDetailResponse getStaffDetail(String id) {
        Optional<ADStaffDetailResponse> staffDetail = adStaffRepository.getDetailStaff(id);
        ADStaffDetailResponse result = staffDetail.orElse(null);
        return result;
    }

    public List<Role> getAllRoleList() {
        String key = RedisPrefixConstant.REDIS_PREFIX_STAFF + "roles";
        return redisCacheHelper.getOrSet(
                key,
                () -> adStaffRoleRepository.getAllRole(),
                new TypeReference<>() {
                });
    }

    public List<Facility> getAllActiveFacilities() {
        String key = RedisPrefixConstant.REDIS_PREFIX_STAFF + "facilities";
        return redisCacheHelper.getOrSet(
                key,
                () -> adminStaffFacilityRepository.getFacility(EntityStatus.ACTIVE),
                new TypeReference<>() {
                });
    }

    @Override
    public ResponseEntity<?> getAllStaffByFilter(ADStaffRequest adStaffRequest) {
        PageableObject staffs = getStaffList(adStaffRequest);
        return RouterHelper.responseSuccess("Lấy tất cả nhân sự thành công", staffs);
    }

    @Override
    public ResponseEntity<?> createStaff(ADCreateUpdateStaffRequest adCreateUpdateStaffRequest) {

        if (!ValidateHelper.isValidCode(adCreateUpdateStaffRequest.getStaffCode())) {
            return RouterHelper.responseError(
                    "Mã nhân sự không hợp lệ: Không có khoảng trắng, không có ký tự đặc biệt ngoài dấu chấm . và dấu gạch dưới _.");
        }

        if (!ValidateHelper.isValidFullname(adCreateUpdateStaffRequest.getName())) {
            return RouterHelper.responseError(
                    "Tên nhân sự không hợp lệ: Tối thiểu 2 từ, cách nhau bởi khoảng trắng và Chỉ gồm ký tự chữ không chứa số hay ký tự đặc biệt.");
        }

        if (!settingHelper.getSetting(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STAFF, Boolean.class)) {
            if (!ValidateHelper.isValidEmailFE(adCreateUpdateStaffRequest.getEmailFe().trim())) {
                return RouterHelper
                        .responseError("Email FE không được chứa khoảng trắng và phải kết thúc bằng @fe.edu.vn");
            }
            if (!ValidateHelper.isValidEmailFPT(adCreateUpdateStaffRequest.getEmailFpt().trim())) {
                return RouterHelper
                        .responseError("Email FPT không được chứa khoảng trắng và phải kết thúc bằng @fpt.edu.vn");
            }
        } else {
            if (!ValidateHelper.isValidEmail(adCreateUpdateStaffRequest.getEmailFe().trim())) {
                return RouterHelper
                        .responseError("Email FE không hợp lệ");
            }
            if (!ValidateHelper.isValidEmail(adCreateUpdateStaffRequest.getEmailFpt().trim())) {
                return RouterHelper
                        .responseError("Email FPT không hợp lệ");
            }
        }



        UserStaff staffExist = isStaffExist(
                adCreateUpdateStaffRequest.getStaffCode(),
                adCreateUpdateStaffRequest.getEmailFe(),
                adCreateUpdateStaffRequest.getEmailFpt());
        if (staffExist != null) {
            return RouterHelper.responseError("Nhân sự đã tồn tại: " + staffExist.getName() + " - " + staffExist.getCode());
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

        userActivityLogHelper.saveLog("vừa thêm 1 nhân sự mới: " + staff.getName() + " (" + staff.getCode() + ")");

        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Thêm nhân sự mới thành công: " + staff.getName() + " - " + staff.getCode());
    }

    @Override
    public ResponseEntity<?> updateStaff(ADCreateUpdateStaffRequest adCreateUpdateStaffRequest, String id) {

        if (!ValidateHelper.isValidCode(adCreateUpdateStaffRequest.getStaffCode())) {
            return RouterHelper.responseError(
                    "Mã nhân sự không hợp lệ: Không có khoảng trắng, không có ký tự đặc biệt ngoài dấu chấm . và dấu gạch dưới _.");
        }

        if (!ValidateHelper.isValidFullname(adCreateUpdateStaffRequest.getName())) {
            return RouterHelper.responseError(
                    "Tên nhân sự không hợp lệ: Tối thiểu 2 từ, cách nhau bởi khoảng trắng và chỉ gồm ký tự chữ không chứa số hay ký tự đặc biệt.");
        }

        Optional<UserStaff> opt = adStaffRepository.findById(id);
        if (opt.isEmpty()) {
            return RouterHelper.responseError("Không tìm thấy nhân sự");
        }
        UserStaff current = opt.get();

        if (adStaffRepository.isExistCodeUpdate(adCreateUpdateStaffRequest.getStaffCode(), current.getCode())) {
            return RouterHelper.responseError("Mã nhân sự đã tồn tại");
        }
        if (adStaffRepository.isExistEmailFeUpdate(adCreateUpdateStaffRequest.getEmailFe(), current.getEmailFe())) {
            return RouterHelper.responseError("Đã có nhân sự khác dùng email fe này");
        }
        if (adStaffRepository.isExistEmailFptUpdate(adCreateUpdateStaffRequest.getEmailFpt(), current.getEmailFpt())) {
            return RouterHelper.responseError("Đã có nhân sự khác dùng email fpt này");
        }

        if (!settingHelper.getSetting(SettingKeys.DISABLED_CHECK_EMAIL_FPT_STAFF, Boolean.class)) {
            if (!ValidateHelper.isValidEmailFE(adCreateUpdateStaffRequest.getEmailFe().trim())) {
                return RouterHelper.responseError("Không chứa khoảng trắng và kết thúc bằng @fe.edu.vn");
            }
            if (!ValidateHelper.isValidEmailFPT(adCreateUpdateStaffRequest.getEmailFpt().trim())) {
                return RouterHelper.responseError("Không chứa khoảng trắng và kết thúc bằng @fpt.edu.vn");
            }
        } else {
            if (!ValidateHelper.isValidEmail(adCreateUpdateStaffRequest.getEmailFe().trim())) {
                return RouterHelper
                        .responseError("Email FE không hợp lệ");
            }
            if (!ValidateHelper.isValidEmail(adCreateUpdateStaffRequest.getEmailFpt().trim())) {
                return RouterHelper
                        .responseError("Email FPT không hợp lệ");
            }
        }

        UserStaff staff = opt.get();
        staff.setName(adCreateUpdateStaffRequest.getName().trim());
        staff.setCode(adCreateUpdateStaffRequest.getStaffCode().trim());
        staff.setEmailFe(adCreateUpdateStaffRequest.getEmailFe().trim());
        staff.setEmailFpt(adCreateUpdateStaffRequest.getEmailFpt().trim());
        adStaffRepository.save(staff);

        Optional<Facility> existFacility = adminStaffFacilityRepository
                .findById(adCreateUpdateStaffRequest.getFacilityId().trim());
        if (existFacility.isEmpty()) {
            return RouterHelper.responseError("Cơ sở không tồn tại");
        }
        Facility facility = existFacility.get();

        List<Role> currentRoles = adStaffRoleRepository.findAllByUserStaffId(staff.getId());
        List<String> newRoleCodes = adCreateUpdateStaffRequest.getRoleCodes();
        RoleConstant[] roleConstants = RoleConstant.values();

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

        userActivityLogHelper.saveLog("vừa cập nhật nhân sự: " + staff.getName() + " (" + staff.getCode() + ")");

        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Cập nhật nhân sự thành công");
    }

    @Override
    public ResponseEntity<?> changeStaffStatus(String staffId) {
        Optional<UserStaff> existStaff = adStaffRepository.findById(staffId);
        if (existStaff.isEmpty()) {
            return RouterHelper.responseError("Nhân sự không tồn tại");
        }

        UserStaff staff = existStaff.get();
        String oldStatus = staff.getStatus() == EntityStatus.ACTIVE ? "Hoạt động" : "Không hoạt động";
        staff.setStatus(staff.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        String newStatus = staff.getStatus() == EntityStatus.ACTIVE ? "Hoạt động" : "Không hoạt động";

        // List<Role> staffRoles = adStaffRoleRepository.findAllByUserStaffId(staffId);
        // staffRoles.forEach(staffRole -> staffRole
        // .setStatus(staff.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE :
        // EntityStatus.ACTIVE));
        // adStaffRoleRepository.saveAll(staffRoles);

        adStaffRepository.save(staff);
        userActivityLogHelper.saveLog("vừa thay đổi trạng thái nhân sự " + staff.getName() + " (" + staff.getCode()
                + ") từ " + oldStatus + " thành " + newStatus);

        redisInvalidationHelper.invalidateAllCaches();

        return RouterHelper.responseSuccess("Thay đổi trạng thái nhân sự thành công");
    }

    @Override
    public ResponseEntity<?> getStaffById(String staffId) {
        Optional<ADStaffDetailResponse> staffDetail = adStaffRepository.getDetailStaff(staffId);
        if (staffDetail.isPresent()) {
            return RouterHelper.responseSuccess("Xem chi tiết nhân sự thành công", staffDetail.get());
        }
        return RouterHelper.responseError("Nhân sự không tồn tại");
    }

    @Override
    public ResponseEntity<?> getAllRole() {
        List<Role> roles = getAllRoleList();
        return RouterHelper.responseSuccess("Lấy tất cả vai trò thành công", roles);
    }

    @Override
    public ResponseEntity<?> getAllFacility() {
        List<Facility> facilities = getAllActiveFacilities();
        return RouterHelper.responseSuccess("Lấy tất cả cơ sở thành công", facilities);
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

}
