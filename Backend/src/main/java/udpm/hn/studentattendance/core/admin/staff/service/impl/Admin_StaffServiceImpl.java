package udpm.hn.studentattendance.core.admin.staff.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.admin.role.model.response.Admin_RoleFacilityResponse;
import udpm.hn.studentattendance.core.admin.staff.model.request.Admin_CreateUpdateStaffRequest;
import udpm.hn.studentattendance.core.admin.staff.model.request.Admin_StaffRequest;
import udpm.hn.studentattendance.core.admin.staff.model.response.Admin_StaffDetailResponse;
import udpm.hn.studentattendance.core.admin.staff.repository.Admin_StaffAdminRepository;
import udpm.hn.studentattendance.core.admin.staff.repository.Admin_StaffFacilityRepository;
import udpm.hn.studentattendance.core.admin.staff.repository.Admin_StaffRepository;
import udpm.hn.studentattendance.core.admin.staff.repository.Admin_StaffRoleRepository;
import udpm.hn.studentattendance.core.admin.staff.service.Admin_StaffService;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.Role;
import udpm.hn.studentattendance.entities.UserAdmin;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;
import udpm.hn.studentattendance.utils.CodeGeneratorUtils;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Validated
public class Admin_StaffServiceImpl implements Admin_StaffService {
    private final Admin_StaffRepository adStaffRepository;

    private final Admin_StaffRoleRepository adStaffRoleRepository;

    private final Admin_StaffAdminRepository adStaffAdminRepository;

    private final Admin_StaffFacilityRepository adminStaffFacilityRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public ResponseEntity<?> getAllStaffByFilter(Admin_StaffRequest adStaffRequest) {
        Pageable pageable = PaginationHelper.createPageable(adStaffRequest, "createdAt");
        PageableObject staffs = PageableObject.of(adStaffRepository.getAllStaff(pageable, adStaffRequest));
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy tất cả giảng viên thành công",
                        staffs
                ),
                HttpStatus.OK);
    }

    //    @Override
//    public ResponseEntity<?> createStaff(Admin_CreateUpdateStaffRequest adCreateUpdateStaffRequest) {
//        UserStaff staffExist = isStaffExist(
//                adCreateUpdateStaffRequest.getStaffCode(),
//                adCreateUpdateStaffRequest.getEmailFe(),
//                adCreateUpdateStaffRequest.getEmailFpt()
//        );
//        if (staffExist != null) {
//            new ResponseEntity<>(
//                    new ApiResponse(
//                            RestApiStatus.WARNING,
//                            "Nhân viên đã tồn tại",
//                            null
//                    ),
//                    HttpStatus.BAD_REQUEST);
//        }
//        UserStaff staff = new UserStaff();
//        staff.setCode(adCreateUpdateStaffRequest.getStaffCode().trim());
//        staff.setName(adCreateUpdateStaffRequest.getName().trim());
//        staff.setEmailFe(adCreateUpdateStaffRequest.getEmailFe().trim());
//        staff.setEmailFpt(adCreateUpdateStaffRequest.getEmailFpt().trim());
//        staff.setStatus(EntityStatus.ACTIVE);
//        adStaffRepository.save(staff);
//
//        UserAdmin staffAdmin = new UserAdmin();
//        staffAdmin.setId(CodeGeneratorUtils.generateRandom());
//        staffAdmin.setCode(adCreateUpdateStaffRequest.getStaffCode().trim());
//        staffAdmin.setName(adCreateUpdateStaffRequest.getName().trim());
//        staffAdmin.setEmail(adCreateUpdateStaffRequest.getEmailFe().trim());
//        staffAdmin.setStatus(EntityStatus.INACTIVE);
//        adStaffAdminRepository.save(staffAdmin);
//
//        return new ResponseEntity<>(
//                new ApiResponse(
//                        RestApiStatus.SUCCESS,
//                        "Thêm nhân viên mới thành công",
//                        null
//                ),
//                HttpStatus.CREATED);
//    }
    @Override
    @Transactional
    public ResponseEntity<?> createStaff(Admin_CreateUpdateStaffRequest adCreateUpdateStaffRequest) {
        // Kiểm tra nhân viên đã tồn tại chưa
        UserStaff staffExist = isStaffExist(
                adCreateUpdateStaffRequest.getStaffCode(),
                adCreateUpdateStaffRequest.getEmailFe(),
                adCreateUpdateStaffRequest.getEmailFpt()
        );
        if (staffExist != null) {
            return new ResponseEntity<>(
                    new ApiResponse(RestApiStatus.WARNING, "Nhân viên đã tồn tại", null),
                    HttpStatus.BAD_REQUEST
            );
        }

        // Tạo mới UserStaff
        UserStaff staff = new UserStaff();
        staff.setCode(adCreateUpdateStaffRequest.getStaffCode().trim());
        staff.setName(adCreateUpdateStaffRequest.getName().trim());
        staff.setEmailFe(adCreateUpdateStaffRequest.getEmailFe().trim());
        staff.setEmailFpt(adCreateUpdateStaffRequest.getEmailFpt().trim());
        staff.setStatus(EntityStatus.ACTIVE);
        staff = adStaffRepository.save(staff);

        Optional<Facility> existFacility = adminStaffFacilityRepository.findById(adCreateUpdateStaffRequest.getFacilityId());
        if (existFacility.isEmpty()) {
            return new ResponseEntity<>(
                    new ApiResponse(RestApiStatus.ERROR, "Cơ sở không tồn tại", null),
                    HttpStatus.BAD_REQUEST
            );
        }
        Facility facility = existFacility.get();
        if (!entityManager.contains(facility)) {
            facility = entityManager.merge(facility);
            System.out.println("After merge, is Facility managed? " + entityManager.contains(facility));
        }

        // Tạo các vai trò
        List<String> roleCodes = adCreateUpdateStaffRequest.getRoleCodes();
        if (roleCodes == null || roleCodes.isEmpty()) {
            return new ResponseEntity<>(
                    new ApiResponse(RestApiStatus.ERROR, "Phải chọn ít nhất một vai trò", null),
                    HttpStatus.BAD_REQUEST
            );
        }

        boolean isAdmin = false;
        for (String roleCode : roleCodes) {
            RoleConstant roleConstant;
            switch (roleCode.trim()) {
                case "0":
                    roleConstant = RoleConstant.ADMIN;
                    isAdmin = true;
                    break;
                case "1":
                    roleConstant = RoleConstant.STAFF;
                    break;
                case "3":
                    roleConstant = RoleConstant.TEACHER;
                    break;
                default:
                    return new ResponseEntity<>(
                            new ApiResponse(RestApiStatus.ERROR, "Vai trò không hợp lệ: " + roleCode, null),
                            HttpStatus.BAD_REQUEST
                    );
            }

            Role role = new Role();
            role.setId(CodeGeneratorUtils.generateRandom());
            role.setCode(roleConstant);
            role.setFacility(facility);
            role.setUserStaff(staff);
            role.setStatus(EntityStatus.ACTIVE);
            adStaffRoleRepository.save(role);
        }

        // Tạo UserAdmin nếu có vai trò ADMIN
        if (isAdmin) {
            UserAdmin staffAdmin = new UserAdmin();
            staffAdmin.setId(CodeGeneratorUtils.generateRandom());
            staffAdmin.setCode(adCreateUpdateStaffRequest.getStaffCode().trim());
            staffAdmin.setName(adCreateUpdateStaffRequest.getName().trim());
            staffAdmin.setEmail(adCreateUpdateStaffRequest.getEmailFe().trim());
            staffAdmin.setStatus(EntityStatus.ACTIVE);
            adStaffAdminRepository.save(staffAdmin);
        }

        return new ResponseEntity<>(
                new ApiResponse(RestApiStatus.SUCCESS, "Thêm nhân viên mới thành công", null),
                HttpStatus.CREATED
        );
    }

    //    @Override
//    public ResponseEntity<?> updateStaff(Admin_CreateUpdateStaffRequest adCreateUpdateStaffRequest) {
//        Optional<UserStaff> existStaff = adStaffRepository.findById(adCreateUpdateStaffRequest.getId());
//        if (existStaff.isEmpty()) {
//            return new ResponseEntity<>(
//                    new ApiResponse(
//                            RestApiStatus.ERROR,
//                            "Không tìm thấy giảng viên",
//                            null
//                    ),
//                    HttpStatus.NOT_FOUND);
//        }
//        UserStaff staff = existStaff.get();
//        staff.setId(adCreateUpdateStaffRequest.getId());
//        staff.setName(adCreateUpdateStaffRequest.getName().trim());
//        staff.setCode(adCreateUpdateStaffRequest.getStaffCode().trim());
//        staff.setEmailFe(adCreateUpdateStaffRequest.getEmailFe().trim());
//        staff.setEmailFpt(adCreateUpdateStaffRequest.getEmailFpt().trim());
//        adStaffRepository.save(staff);
//        return new ResponseEntity<>(
//                new ApiResponse(
//                        RestApiStatus.SUCCESS,
//                        "Cập nhật giảng viên thàn công",
//                        null
//                ),
//                HttpStatus.OK);
//    }
    @Override
    public ResponseEntity<?> updateStaff(Admin_CreateUpdateStaffRequest adCreateUpdateStaffRequest) {
        Optional<UserStaff> existStaff = adStaffRepository.findById(adCreateUpdateStaffRequest.getId());
        if (existStaff.isEmpty()) {
            return new ResponseEntity<>(
                    new ApiResponse(RestApiStatus.ERROR, "Không tìm thấy nhân viên", null),
                    HttpStatus.NOT_FOUND
            );
        }

        UserStaff staff = existStaff.get();
        staff.setName(adCreateUpdateStaffRequest.getName().trim());
        staff.setCode(adCreateUpdateStaffRequest.getStaffCode().trim());
        staff.setEmailFe(adCreateUpdateStaffRequest.getEmailFe().trim());
        staff.setEmailFpt(adCreateUpdateStaffRequest.getEmailFpt().trim());
        adStaffRepository.save(staff);

        Optional<Facility> existFacility = adminStaffFacilityRepository.findById(adCreateUpdateStaffRequest.getFacilityId().trim());
        if (existFacility.isEmpty()) {
            return new ResponseEntity<>(
                    new ApiResponse(RestApiStatus.ERROR, "Cơ sở không tồn tại", null),
                    HttpStatus.BAD_REQUEST
            );
        }
        Facility facility = existFacility.get();

        List<Role> currentRoles = adStaffRoleRepository.findAllByUserStaffId(staff.getId());
        List<String> newRoleCodes = adCreateUpdateStaffRequest.getRoleCodes();

        // Xóa những vai trò hiện tại không có trong danh sách mới
        for (Role role : currentRoles) {
            String currentRoleString;
            switch (role.getCode()) {
                case ADMIN:
                    currentRoleString = "0";
                    break;
                case STAFF:
                    currentRoleString = "1";
                    break;
                case TEACHER:
                    currentRoleString = "3";
                    break;
                default:
                    currentRoleString = "";
            }
            if (!newRoleCodes.contains(currentRoleString)) {
                adStaffRoleRepository.delete(role);
            }
        }

        // Cập nhật hoặc tạo mới vai trò theo danh sách mới
        for (String roleName : newRoleCodes) {
            RoleConstant roleConstant;
            switch (roleName.trim()) {
                case "0":
                    roleConstant = RoleConstant.ADMIN;
                    break;
                case "1":
                    roleConstant = RoleConstant.STAFF;
                    break;
                case "3":
                    roleConstant = RoleConstant.TEACHER;
                    break;
                default:
                    return new ResponseEntity<>(
                            new ApiResponse(RestApiStatus.ERROR, "Vai trò không hợp lệ: " + roleName, null),
                            HttpStatus.BAD_REQUEST
                    );
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
                role.setId(CodeGeneratorUtils.generateRandom());
                role.setCode(roleConstant);
                role.setFacility(facility);
                role.setUserStaff(staff);
            }
            role.setStatus(EntityStatus.ACTIVE);
            adStaffRoleRepository.save(role);
        }

        // Kiểm tra nếu có vai trò ADMIN ("0")
        boolean isAdmin = newRoleCodes.contains("0");
        Optional<UserAdmin> existStaffAdmin = adStaffAdminRepository.getUserAdminByCode(staff.getCode());
        if (isAdmin) {
            if (existStaffAdmin.isPresent()) {
                UserAdmin staffAdmin = existStaffAdmin.get();
                staffAdmin.setName(staff.getName());
                staffAdmin.setEmail(staff.getEmailFe());
                staffAdmin.setStatus(EntityStatus.ACTIVE);
                adStaffAdminRepository.save(staffAdmin);
            } else {
                UserAdmin staffAdmin = new UserAdmin();
                staffAdmin.setId(CodeGeneratorUtils.generateRandom());
                staffAdmin.setCode(staff.getCode());
                staffAdmin.setName(staff.getName());
                staffAdmin.setEmail(staff.getEmailFe());
                staffAdmin.setStatus(EntityStatus.ACTIVE);
                adStaffAdminRepository.save(staffAdmin);
            }
        } else {
            existStaffAdmin.ifPresent(adStaffAdminRepository::delete);
        }

        return new ResponseEntity<>(
                new ApiResponse(RestApiStatus.SUCCESS, "Cập nhật nhân viên thành công", null),
                HttpStatus.OK
        );
    }



    @Override
    public ResponseEntity<?> changeStaffStatus(String staffId) {
        Optional<UserStaff> existStaff = adStaffRepository.findById(staffId);
        if (existStaff.isEmpty()) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.ERROR,
                            "Nhân viên không tồn tại",
                            null
                    ),
                    HttpStatus.NOT_FOUND);
        }
        UserStaff staff = existStaff.get();
        staff.setStatus(staff.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);

        List<Role> staffRoles = adStaffRoleRepository.findAllByUserStaffId(staffId);
        staffRoles.forEach(staffRole -> staffRole.setStatus(staff.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE));
        adStaffRoleRepository.saveAll(staffRoles);

        adStaffRepository.save(staff);
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Thay đổi trạng thái giảng viên thành công",
                        null
                ),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getStaffById(String staffId) {
        Optional<Admin_StaffDetailResponse> existStaff = adStaffRepository.getDetailStaff(staffId);
        if (existStaff.isPresent()) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.SUCCESS,
                            "Xem chi tiết giảng viên thành công",
                            existStaff
                    ),
                    HttpStatus.OK);
        }
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.WARNING,
                        "Giảng viên không tồn tại",
                        existStaff
                ),
                HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<?> getAllRole() {
        List<Role> getAllRole = adStaffRoleRepository.getAllRole();
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy tất cả vai trò thành công",
                        getAllRole
                ),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getAllFacility() {
        List<Facility> getAllFacility = adminStaffFacilityRepository.getFacility(EntityStatus.ACTIVE);
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy tất cả cơ sở thành công",
                        getAllFacility
                ),
                HttpStatus.OK);
    }

    private UserStaff isStaffExist(
            String staffCode,
            String accountFe,
            String accountFpt
    ) {
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
