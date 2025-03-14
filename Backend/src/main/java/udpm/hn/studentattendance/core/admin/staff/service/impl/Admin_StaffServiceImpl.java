package udpm.hn.studentattendance.core.admin.staff.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.admin.staff.model.request.Admin_CreateUpdateStaffRequest;
import udpm.hn.studentattendance.core.admin.staff.model.request.Admin_StaffRequest;
import udpm.hn.studentattendance.core.admin.staff.repository.Admin_StaffAdminRepository;
import udpm.hn.studentattendance.core.admin.staff.repository.Admin_StaffRepository;
import udpm.hn.studentattendance.core.admin.staff.repository.Admin_StaffRoleRepository;
import udpm.hn.studentattendance.core.admin.staff.service.Admin_StaffService;
import udpm.hn.studentattendance.entities.Role;
import udpm.hn.studentattendance.entities.UserAdmin;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
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

    @Override
    public ResponseEntity<?> createStaff(Admin_CreateUpdateStaffRequest adCreateUpdateStaffRequest) {
        UserStaff staffExist = isStaffExist(
                adCreateUpdateStaffRequest.getStaffCode(),
                adCreateUpdateStaffRequest.getEmailFe(),
                adCreateUpdateStaffRequest.getEmailFpt()
        );
        if (staffExist != null) {
            new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.WARNING,
                            "Nhân viên đã tồn tại",
                            null
                    ),
                    HttpStatus.BAD_REQUEST);
        }
        UserStaff staff = new UserStaff();
        staff.setCode(adCreateUpdateStaffRequest.getStaffCode().trim());
        staff.setName(adCreateUpdateStaffRequest.getName().trim());
        staff.setEmailFe(adCreateUpdateStaffRequest.getEmailFe().trim());
        staff.setEmailFpt(adCreateUpdateStaffRequest.getEmailFpt().trim());
        staff.setStatus(EntityStatus.ACTIVE);
        adStaffRepository.save(staff);

        UserAdmin staffAdmin = new UserAdmin();
        staffAdmin.setId(CodeGeneratorUtils.generateRandom());
        staffAdmin.setCode(adCreateUpdateStaffRequest.getStaffCode().trim());
        staffAdmin.setName(adCreateUpdateStaffRequest.getName().trim());
        staffAdmin.setEmail(adCreateUpdateStaffRequest.getEmailFe().trim());
        staffAdmin.setStatus(EntityStatus.INACTIVE);
        adStaffAdminRepository.save(staffAdmin);

        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Thêm nhân viên mới thành công",
                        null
                ),
                HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<?> updateStaff(Admin_CreateUpdateStaffRequest adCreateUpdateStaffRequest) {
        Optional<UserStaff> existStaff = adStaffRepository.findById(adCreateUpdateStaffRequest.getId());
        if (existStaff.isEmpty()) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.ERROR,
                            "Không tìm thấy giảng viên",
                            null
                    ),
                    HttpStatus.NOT_FOUND);
        }
        UserStaff staff = existStaff.get();
        staff.setId(adCreateUpdateStaffRequest.getId());
        staff.setName(adCreateUpdateStaffRequest.getName().trim());
        staff.setCode(adCreateUpdateStaffRequest.getStaffCode().trim());
        staff.setEmailFe(adCreateUpdateStaffRequest.getEmailFe().trim());
        staff.setEmailFpt(adCreateUpdateStaffRequest.getEmailFpt().trim());
        adStaffRepository.save(staff);
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Cập nhật giảng viên thàn công",
                        null
                ),
                HttpStatus.OK);
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
        Optional<UserStaff> existStaff = adStaffRepository.findById(staffId);
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
