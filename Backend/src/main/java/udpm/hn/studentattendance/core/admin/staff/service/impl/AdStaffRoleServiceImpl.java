package udpm.hn.studentattendance.core.admin.staff.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.admin.staff.model.request.AdChangeStaffRoleRequest;
import udpm.hn.studentattendance.core.admin.staff.model.request.AdStaffRoleRequest;
import udpm.hn.studentattendance.core.admin.staff.repository.ADStaffFacilityRepository;
import udpm.hn.studentattendance.core.admin.staff.repository.AdStaffRepository;
import udpm.hn.studentattendance.core.admin.staff.repository.AdStaffRoleRepository;
import udpm.hn.studentattendance.core.admin.staff.service.AdStaffRoleService;
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
public class AdStaffRoleServiceImpl implements AdStaffRoleService {

    private final AdStaffRoleRepository adStaffRoleRepository;

    private final AdStaffRepository adStaffRepository;

    private final ADStaffFacilityRepository adStaffFacilityRepository;

    @Override
    public ResponseEntity<?> getAllRole(String staffId) {
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy vai trò của giảng viên thành công",
                        adStaffRoleRepository.getRolesByStaffId(staffId)
                ),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getRoleChecked(AdStaffRoleRequest adStaffRoleRequest) {
        Pageable pageable = PaginationHelper.createPageable(adStaffRoleRequest, "createdAt");
        PageableObject pageableObject = PageableObject.of(adStaffRoleRepository.getRolesChecked(pageable, adStaffRoleRequest));
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy vai trò đã tích cho giảng viên thành công",
                        pageableObject
                ),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> changeStaffRole(AdChangeStaffRoleRequest adChangeStaffRoleRequest) {
        Optional<Role> existRole = adStaffRoleRepository.findById(adChangeStaffRoleRequest.getIdRole().trim());
        Optional<UserStaff> existStaff = adStaffRepository.findById(adChangeStaffRoleRequest.getIdStaff().trim());
        if (existStaff.isEmpty() || existRole.isEmpty()) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.ERROR,
                            "Vai trò hoặc giảng viên không tồn tại",
                            null
                    ),
                    HttpStatus.NOT_FOUND);
        }
        List<Role> listRole = adStaffRoleRepository.findAllByIdAndUserStaffId(adChangeStaffRoleRequest.getIdRole(), adChangeStaffRoleRequest.getIdStaff());
        if (listRole.isEmpty()) {
            Role role = existRole.get();
            role.setUserStaff(existStaff.get());
            adStaffRoleRepository.save(role);
        } else {
            listRole.get(0).setStatus(listRole.get(0).getStatus().equals(EntityStatus.INACTIVE) ? EntityStatus.ACTIVE : EntityStatus.INACTIVE);
            adStaffRoleRepository.save(listRole.get(0));
        }
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Cập nhật vai trò cho giảng viên thành công",
                        null
                ),
                HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<?> getFacilities() {
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy cơ sở thành công",
                        adStaffFacilityRepository.getFacilities()
                ),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getFacilitiesSelect(String idStaff) { // dùng để lấy bộ môn theo cơ sở
        return null;
    }
}
