package udpm.hn.studentattendance.core.admin.role.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.admin.role.model.request.AdRoleRequest;
import udpm.hn.studentattendance.core.admin.role.repository.AdRoleFacilityRepository;
import udpm.hn.studentattendance.core.admin.role.repository.AdRoleRepository;
import udpm.hn.studentattendance.core.admin.role.repository.AdRoleStaffRepository;
import udpm.hn.studentattendance.core.admin.role.service.AdRoleService;
import udpm.hn.studentattendance.core.admin.staff.repository.AdStaffRepository;
import udpm.hn.studentattendance.entities.Role;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Validated
public class AdRoleServiceImpl implements AdRoleService {

    private final AdRoleRepository adRoleRepository;

    private final AdRoleFacilityRepository adRoleFacilityRepository;

    private final AdRoleStaffRepository adRoleStaffRepository;

    private final AdStaffRepository adStaffRepository;


    @Override
    public ResponseEntity<?> getAllRole(AdRoleRequest adRoleRequest) {
        Pageable pageable = PaginationHelper.createPageable(adRoleRequest, "createdAt");
        PageableObject pageableObject = PageableObject.of(adRoleRepository.getAllRole(pageable, adRoleRequest));
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy thành công tất cả chức vụ",
                        pageableObject
                ),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getDetailRole(String roleId) {
        Optional<Role> detailRole = adRoleRepository.findById(roleId.trim());
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy chi tiết chức vụ thành công",
                        detailRole
                ),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getFacilities() {
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy tất cả cơ sở thành công",
                        adRoleFacilityRepository.getFacilities()
                ),
                HttpStatus.OK);
    }


    @Override
    public ResponseEntity<?> deleteRole(String roleId) {
        Optional<Role> existRole = adRoleRepository.findById(roleId.trim());
        if (existRole.isEmpty()) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.ERROR,
                            "Vai trò không tồn tại",
                            null
                    ),
                    HttpStatus.NOT_FOUND);
        }

        Role role = existRole.get();
        role.setStatus(role.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        adRoleRepository.save(role);

        List<UserStaff> userStaffList = adRoleStaffRepository.findAllByIdAndStatus(role.getId().trim(),EntityStatus.ACTIVE);
        userStaffList.stream().forEach(staffRole -> staffRole.setStatus(EntityStatus.INACTIVE));
        adStaffRepository.saveAll(userStaffList);

        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Đổi trạng thái vai trò thành công",
                        null
                ),
                HttpStatus.OK);
    }
}
