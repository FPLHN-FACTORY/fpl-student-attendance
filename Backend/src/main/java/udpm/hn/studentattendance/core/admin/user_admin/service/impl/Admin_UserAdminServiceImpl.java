package udpm.hn.studentattendance.core.admin.user_admin.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.admin.user_admin.model.request.Admin_UserAdminCreateOrUpdateRequest;
import udpm.hn.studentattendance.core.admin.user_admin.model.request.Admin_UserAdminRequest;
import udpm.hn.studentattendance.core.admin.user_admin.repository.Admin_UserAdminExtendRepository;
import udpm.hn.studentattendance.core.admin.user_admin.service.Admin_UserAdminService;
import udpm.hn.studentattendance.entities.UserAdmin;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Validated
public class Admin_UserAdminServiceImpl implements Admin_UserAdminService {

    private final Admin_UserAdminExtendRepository userAdminExtendRepository;

    @Override
    public ResponseEntity<?> getAllUserAdmin(Admin_UserAdminRequest request) {
        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject list = PageableObject.of(userAdminExtendRepository.getAllUserAdmin(pageable, request));
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy thành công tất cả tài khoản admin",
                        list
                ),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getUserAdminById(String id) {
        Optional<UserAdmin> optionalUserAdmin = userAdminExtendRepository.findById(id);
        if (optionalUserAdmin.isEmpty()) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.ERROR,
                            "Admin không tồn tại",
                            null
                    ),
                    HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy thành công tài khoản admin",
                        optionalUserAdmin
                ),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> createUserAdmin(Admin_UserAdminCreateOrUpdateRequest createOrUpdateRequest) {
        Optional<UserAdmin> existUserAdmin = userAdminExtendRepository.getUserAdminByCode(createOrUpdateRequest.getStaffCode());
        Optional<UserAdmin> existUserAdmin2 = userAdminExtendRepository.getUserAdminByEmail(createOrUpdateRequest.getEmail());
        if (existUserAdmin.isPresent()) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.ERROR,
                            "Mã của admin đã tồn tại",
                            existUserAdmin
                    ),
                    HttpStatus.BAD_REQUEST);
        }
        if (existUserAdmin2.isPresent()) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.ERROR,
                            "Email của admin đã tồn tại",
                            existUserAdmin
                    ),
                    HttpStatus.BAD_REQUEST);
        }
        UserAdmin userAdmin = new UserAdmin();
        userAdmin.setCode(createOrUpdateRequest.getStaffCode().trim());
        userAdmin.setName(createOrUpdateRequest.getStaffName().trim());
        userAdmin.setEmail(createOrUpdateRequest.getEmail().trim());
        userAdminExtendRepository.save(userAdmin);

        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Thêm admin mới thành công",
                        userAdmin
                ),
                HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<?> updateUserAdmin(Admin_UserAdminCreateOrUpdateRequest createOrUpdateRequest, String id) {
        // Kiểm tra nhân viên tồn tại
        Optional<UserAdmin> opt = userAdminExtendRepository.findById(id);
        if (opt.isEmpty()) {
            return new ResponseEntity<>(
                    new ApiResponse(RestApiStatus.ERROR, "Không tìm thấy nhân viên", null),
                    HttpStatus.BAD_REQUEST
            );
        }
        UserAdmin current = opt.get();

        // 2. Check trùng code
        if (userAdminExtendRepository.isExistCodeUpdate(createOrUpdateRequest.getStaffCode(), current.getCode())) {
            return new ResponseEntity<>(
                    new ApiResponse(RestApiStatus.ERROR, "Mã admin đã tồn tại", null),
                    HttpStatus.BAD_REQUEST
            );
        }
        // 3. Check trùng email FE
        if (userAdminExtendRepository.isExistEmailUpdate(createOrUpdateRequest.getEmail(), current.getEmail())) {
            return new ResponseEntity<>(
                    new ApiResponse(RestApiStatus.ERROR, "Đã có admin khác dùng email fe này", null),
                    HttpStatus.BAD_REQUEST
            );
        }
        UserAdmin userAdmin = opt.get();
        userAdmin.setCode(createOrUpdateRequest.getStaffCode().trim());
        userAdmin.setName(createOrUpdateRequest.getStaffName().trim());
        userAdmin.setEmail(createOrUpdateRequest.getEmail().trim());
        userAdminExtendRepository.save(userAdmin);
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Cập nhật admin thành công",
                        userAdmin
                ),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> changeStatus(String id) {
        Optional<UserAdmin> optionalUserAdmin = userAdminExtendRepository.findById(id);
        if (optionalUserAdmin.isPresent()) {
            UserAdmin userAdmin = optionalUserAdmin.get();
            userAdmin.setStatus(userAdmin.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
            userAdminExtendRepository.save(userAdmin);
        }
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.ERROR,
                        "Admin không tồn tại",
                        null
                ),
                HttpStatus.BAD_REQUEST);
    }
}
