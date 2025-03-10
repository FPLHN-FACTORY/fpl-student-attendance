package udpm.hn.studentattendance.core.admin.staff.service.impl;

import lombok.Generated;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.admin.staff.model.request.AdChangeStaffRoleRequest;
import udpm.hn.studentattendance.core.admin.staff.model.request.AdStaffRoleRequest;
import udpm.hn.studentattendance.core.admin.staff.model.response.AdStaffRoleResponse;
import udpm.hn.studentattendance.core.admin.staff.repository.ADStaffFacilityRepository;
import udpm.hn.studentattendance.core.admin.staff.repository.AdStaffAdminRepository;
import udpm.hn.studentattendance.core.admin.staff.repository.AdStaffRepository;
import udpm.hn.studentattendance.core.admin.staff.repository.AdStaffRoleRepository;
import udpm.hn.studentattendance.core.admin.staff.service.AdStaffRoleService;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.Role;
import udpm.hn.studentattendance.entities.UserAdmin;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.helpers.GenerateNameHelper;
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
public class AdStaffRoleServiceImpl implements AdStaffRoleService {

    private final AdStaffRoleRepository adStaffRoleRepository;

    private final AdStaffRepository adStaffRepository;

    private final ADStaffFacilityRepository adStaffFacilityRepository;

    private final AdStaffAdminRepository adStaffAdminRepository;

    @Override
    public ResponseEntity<?> getAllRole(String staffId) {
        List<AdStaffRoleResponse> list = adStaffRoleRepository.getRolesByStaffId(staffId, EntityStatus.ACTIVE, EntityStatus.ACTIVE);
        if (list.size() <= 0) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.SUCCESS,
                            "Nhân viên chưa có vai trò nào",
                            null
                    ),
                    HttpStatus.OK);
        }
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy vai trò của giảng viên thành công",
                        list
                ),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getRoleChecked(AdStaffRoleRequest adStaffRoleRequest) {
        Pageable pageable = PaginationHelper.createPageable(adStaffRoleRequest, "createdAt");
        PageableObject pageableObject = PageableObject.of(adStaffRoleRepository.getRolesChecked(pageable, adStaffRoleRequest, EntityStatus.ACTIVE, EntityStatus.ACTIVE));
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy vai trò đã tích cho giảng viên thành công",
                        pageableObject
                ),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> changeStaffRole(AdChangeStaffRoleRequest request) {
        Optional<UserStaff> existStaff = adStaffRepository.findById(request.getIdStaff().trim());
        Optional<Facility> existFacility = adStaffFacilityRepository.findById(request.getFacilityId().trim());
        Optional<UserAdmin> existStaffAdmin = adStaffAdminRepository.getUserAdminByCode(request.getStaffCode());
        if (existStaff.isEmpty() || existFacility.isEmpty()) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.ERROR,
                            "Giảng viên hoặc cơ sở không tồn tại",
                            null
                    ),
                    HttpStatus.NOT_FOUND);
        }
        String codeRole = request.getIdRole().trim();
        RoleConstant roleConstant = null;
        if (codeRole.equals("1")) {
            roleConstant = RoleConstant.STAFF;
        } else if (codeRole.equals("3")) {
            roleConstant = RoleConstant.TEACHER;
        } else if (codeRole.equals("0")) {
            roleConstant = RoleConstant.ADMIN;
            UserAdmin userAdmin = existStaffAdmin.get();
            userAdmin.setStatus(EntityStatus.ACTIVE);
            adStaffAdminRepository.save(userAdmin);
        }
        // Tìm tất cả các role của nhân viên dựa trên RoleConstant
        List<Role> listRole = adStaffRoleRepository.findAllByCodeAndUserStaffId(roleConstant, request.getIdStaff());
        if (listRole.isEmpty()) {
            // Nếu chưa có role nào, tạo mới
            Role role = new Role();
            role.setId(CodeGeneratorUtils.generateRandom());
            role.setCode(roleConstant);
            role.setFacility(existFacility.get());
            role.setUserStaff(existStaff.get());
            role.setStatus(EntityStatus.ACTIVE);
            adStaffRoleRepository.save(role);
        } else if (!listRole.isEmpty()){
            Role role = listRole.get(0);
            if (role.getStatus().equals(EntityStatus.INACTIVE)) {
                role.setStatus(EntityStatus.ACTIVE);
            } else {
                role.setStatus(EntityStatus.INACTIVE);
            }
            adStaffRoleRepository.save(role);
        }

        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Cập nhật vai trò cho giảng viên thành công",
                        listRole
                ),
                HttpStatus.CREATED);
    }


    @Override
    public ResponseEntity<?> getFacilities() {
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy cơ sở thành công",
                        adStaffFacilityRepository.getFacilities(EntityStatus.ACTIVE)
                ),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> deleteStaffRole(String roleId) {
        Role existRole = adStaffRoleRepository.findById(roleId).orElse(null);
        if (existRole != null) {
            adStaffRoleRepository.deleteById(roleId);
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.SUCCESS,
                            "Xoá nhân viên thành công",
                            null
                    ),
                    HttpStatus.OK);
        }
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.ERROR,
                        "Nhân viên không tồn tại",
                        null
                ),
                HttpStatus.NOT_FOUND);
    }


}
