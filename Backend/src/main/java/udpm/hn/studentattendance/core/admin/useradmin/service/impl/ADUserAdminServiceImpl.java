package udpm.hn.studentattendance.core.admin.useradmin.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.admin.useradmin.model.request.ADUserAdminChangePowerShiftRequest;
import udpm.hn.studentattendance.core.admin.useradmin.model.request.ADUserAdminCreateOrUpdateRequest;
import udpm.hn.studentattendance.core.admin.useradmin.model.request.ADUserAdminRequest;
import udpm.hn.studentattendance.core.admin.useradmin.repository.ADUserAdminExtendRepository;
import udpm.hn.studentattendance.core.admin.useradmin.repository.ADUserAdminStaffExtendRepository;
import udpm.hn.studentattendance.core.admin.useradmin.service.ADUserAdminService;
import udpm.hn.studentattendance.core.notification.model.request.NotificationAddRequest;
import udpm.hn.studentattendance.core.notification.service.NotificationService;
import udpm.hn.studentattendance.entities.UserAdmin;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.helpers.*;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.config.mailer.model.MailerDefaultRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Validated
public class ADUserAdminServiceImpl implements ADUserAdminService {

    private final ADUserAdminExtendRepository userAdminExtendRepository;

    private final ADUserAdminStaffExtendRepository userAdminStaffExtendRepository;

    private final NotificationService notificationService;

    private final SessionHelper sessionHelper;

    private final MailerHelper mailerHelper;

    @Value("${app.config.app-name}")
    private String appName;

    @Value("${app.config.disabled-check-email-fpt}")
    private String isDisableCheckEmailFpt;

    @Override
    public ResponseEntity<?> getAllUserAdmin(ADUserAdminRequest request) {
        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject list = PageableObject.of(userAdminExtendRepository.getAllUserAdmin(pageable, request));
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy thành công tất cả tài khoản admin",
                        list),
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
                            null),
                    HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy thành công tài khoản admin",
                        optionalUserAdmin),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> createUserAdmin(ADUserAdminCreateOrUpdateRequest createOrUpdateRequest) {
        Optional<UserAdmin> existUserAdmin = userAdminExtendRepository
                .getUserAdminByCode(createOrUpdateRequest.getStaffCode());
        Optional<UserAdmin> existUserAdmin2 = userAdminExtendRepository
                .getUserAdminByEmail(createOrUpdateRequest.getEmail());

        if (!ValidateHelper.isValidEmail(createOrUpdateRequest.getEmail())) {
            return RouterHelper.responseError("Định dạng email không hợp lệ");
        }

        if (!ValidateHelper.isValidFullname(createOrUpdateRequest.getStaffName())) {
            return RouterHelper.responseError("Tên ban đào tạo không hợp lệ");
        }

        if(!isDisableCheckEmailFpt.equalsIgnoreCase("true")) {
            if (!ValidateHelper.isValidEmailFE(createOrUpdateRequest.getEmail().trim()) && !ValidateHelper.isValidEmailFPT(createOrUpdateRequest.getEmail().trim())) {
                return RouterHelper.responseError("Email không chứa khoảng trắng và phải kết thúc bằng edu.vn");
            }
        }

        if (existUserAdmin.isPresent()) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.ERROR,
                            "Mã của admin đã tồn tại",
                            existUserAdmin),
                    HttpStatus.BAD_REQUEST);
        }
        if (existUserAdmin2.isPresent()) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.ERROR,
                            "Email của admin đã tồn tại",
                            existUserAdmin),
                    HttpStatus.BAD_REQUEST);
        }
//        boolean isMaxAdmin = userAdminExtendRepository.isMaxAdmin();
//        if (isMaxAdmin) {
//            return new ResponseEntity<>(
//                    new ApiResponse(
//                            RestApiStatus.ERROR,
//                            "Chỉ cho phép tối đa 5 tài khoản ban đào tạo",
//                            existUserAdmin),
//                    HttpStatus.BAD_REQUEST);
//        }
        UserAdmin userAdmin = new UserAdmin();
        userAdmin.setCode(createOrUpdateRequest.getStaffCode().trim());
        userAdmin.setName(createOrUpdateRequest.getStaffName().trim());
        userAdmin.setEmail(createOrUpdateRequest.getEmail().trim());
        userAdminExtendRepository.save(userAdmin);

        Map<String, Object> dataNotification = new HashMap<>();
        dataNotification.put(NotificationHelper.KEY_USER_STAFF, createOrUpdateRequest.getStaffName());
        NotificationAddRequest notificationAddRequest = new NotificationAddRequest();
        notificationAddRequest.setIdUser(sessionHelper.getUserId());
        notificationAddRequest.setType(NotificationHelper.TYPE_ADD_ADMIN);
        notificationAddRequest.setData(dataNotification);
        notificationService.add(notificationAddRequest);
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Thêm admin mới thành công",
                        userAdmin),
                HttpStatus.CREATED);

    }

    @Override
    public ResponseEntity<?> updateUserAdmin(ADUserAdminCreateOrUpdateRequest createOrUpdateRequest,
                                             String id) {

        if (!ValidateHelper.isValidEmail(createOrUpdateRequest.getEmail())) {
            return RouterHelper.responseError("Định dạng email không hợp lệ");
        }

        if (!ValidateHelper.isValidFullname(createOrUpdateRequest.getStaffName())) {
            return RouterHelper.responseError("Tên ban đào tạo không hợp lệ");
        }

        // Kiểm tra nhân viên tồn tại
        Optional<UserAdmin> opt = userAdminExtendRepository.findById(id);
        if (opt.isEmpty()) {
            return new ResponseEntity<>(
                    new ApiResponse(RestApiStatus.ERROR, "Không tìm thấy nhân viên", null),
                    HttpStatus.BAD_REQUEST);
        }
        UserAdmin current = opt.get();

        // 2. Check trùng code
        if (userAdminExtendRepository.isExistCodeUpdate(createOrUpdateRequest.getStaffCode(),
                current.getCode())) {
            return new ResponseEntity<>(
                    new ApiResponse(RestApiStatus.ERROR, "Mã admin đã tồn tại", null),
                    HttpStatus.BAD_REQUEST);
        }
        // 3. Check trùng email FE
        if (userAdminExtendRepository.isExistEmailUpdate(createOrUpdateRequest.getEmail(),
                current.getEmail())) {
            return new ResponseEntity<>(
                    new ApiResponse(RestApiStatus.ERROR, "Đã có admin khác dùng email fe này",
                            null),
                    HttpStatus.BAD_REQUEST);
        }

        UserAdmin userAdmin = opt.get();
        userAdmin.setCode(createOrUpdateRequest.getStaffCode().trim());
        userAdmin.setName(createOrUpdateRequest.getStaffName().trim());
        userAdmin.setEmail(createOrUpdateRequest.getEmail().trim());
        userAdminExtendRepository.save(userAdmin);

        Map<String, Object> dataNotification = new HashMap<>();
        dataNotification.put(NotificationHelper.KEY_USER_ADMIN,
                createOrUpdateRequest.getStaffCode() + " - " + createOrUpdateRequest.getStaffName());
        NotificationAddRequest notificationAddRequest = new NotificationAddRequest();
        notificationAddRequest.setIdUser(sessionHelper.getUserId());
        notificationAddRequest.setType(NotificationHelper.TYPE_UPDATE_ADMIN);
        notificationAddRequest.setData(dataNotification);
        notificationService.add(notificationAddRequest);
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Cập nhật admin thành công",
                        userAdmin),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> changeStatus(String id) {
        Optional<UserAdmin> optionalUserAdmin = userAdminExtendRepository.findById(id);

        if (optionalUserAdmin.get().getId().equalsIgnoreCase(sessionHelper.getUserId())) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.ERROR,
                            "Không được sửa trạng thái của chính bản thân",
                            null),
                    HttpStatus.BAD_REQUEST);
        } else if (optionalUserAdmin.isPresent()) {
            UserAdmin userAdmin = optionalUserAdmin.get();
            userAdmin.setStatus(userAdmin.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE
                    : EntityStatus.ACTIVE);
            UserAdmin saveAdmin = userAdminExtendRepository.save(userAdmin);
            if (saveAdmin.getStatus() == EntityStatus.INACTIVE) {
                MailerDefaultRequest mailerDefaultRequest = new MailerDefaultRequest();
                mailerDefaultRequest.setTo(saveAdmin.getEmail());
                mailerDefaultRequest.setTemplate(null);
                mailerDefaultRequest.setTitle("Thông báo quan trọng về xoá quyền từ:  " + appName);

                Map<String, Object> vars = Map.of(
                        "ADMIN_NAME", saveAdmin.getCode() + " - " + saveAdmin.getName(),
                        "MY_NAME", sessionHelper.getUserCode() + " - " + sessionHelper.getUserName()
                );
                mailerDefaultRequest.setContent(MailerHelper.loadTemplate(MailerHelper.TEMPLATE_CHANGE_STATUS_ADMIN, vars));
                mailerHelper.send(mailerDefaultRequest);
            }
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.SUCCESS,
                            "Thay đổi trạng thái thành công",
                            saveAdmin),
                    HttpStatus.OK);
        }

        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.ERROR,
                        "Ban đào tạo không tồn tại",
                        null),
                HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<?> isMySelf(String userAdminId) {
        String currentUserId = sessionHelper.getUserId();
        boolean isSelf = currentUserId.equalsIgnoreCase(userAdminId);

        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Kiểm tra thành công",
                        isSelf //sẽ trả về true hoặc false
                ),
                HttpStatus.OK
        );
    }


    @Override
    public ResponseEntity<?> changePowerShift(ADUserAdminChangePowerShiftRequest userAdminChangePowerShiftRequest) {
        Optional<UserStaff> userStaff = userAdminStaffExtendRepository.findById(userAdminChangePowerShiftRequest.getUserStaffId());
        List<UserAdmin> userAdminList = userAdminExtendRepository.findAll();

        boolean emailFeExists = userAdminList.stream()
                .anyMatch(a -> a.getEmail().equalsIgnoreCase(userStaff.get().getEmailFe()));

        boolean emailFptExists = userAdminList.stream()
                .anyMatch(a -> a.getEmail().equalsIgnoreCase(userStaff.get().getEmailFpt()));
        if (emailFeExists || emailFptExists) {
            return new ResponseEntity<>(
                    new ApiResponse(RestApiStatus.ERROR, "Nhân viên này đã có quyền admin", null),
                    HttpStatus.BAD_REQUEST
            );
        }
        if (userStaff.isPresent()) {
            UserAdmin userAdmin = new UserAdmin();
            userAdmin.setCode(userStaff.get().getCode());
            userAdmin.setName(userStaff.get().getName());
            userAdmin.setEmail(userStaff.get().getEmailFe());
            userAdmin.setStatus(EntityStatus.ACTIVE);
            userAdminExtendRepository.save(userAdmin);

            userAdminExtendRepository.deleteById(userAdminChangePowerShiftRequest.getUserAdminId());
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.SUCCESS,
                            "Kiểm tra thành công",
                            userAdmin
                    ),
                    HttpStatus.CREATED
            );
        }
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.ERROR,
                        "Giảng viên hoặc phụ trách xưởng không tồn tại",
                        null),
                HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<?> getAllUserStaff() {
        List<UserStaff> userStaffList = userAdminStaffExtendRepository.getAllUserStaff();
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy thành công danh sách nhân viên",
                        userStaffList
                ),
                HttpStatus.OK
        );
    }

    @Override
    public ResponseEntity<?> deleteUserAdmin(String userAdminId) {
        Optional<UserAdmin> existUserAdmin = userAdminExtendRepository.findById(userAdminId);
        userAdminExtendRepository.deleteById(userAdminId);
        MailerDefaultRequest mailerDefaultRequest = new MailerDefaultRequest();
        mailerDefaultRequest.setTo(existUserAdmin.get().getEmail());
        mailerDefaultRequest.setTemplate(null);
        mailerDefaultRequest.setTitle("Thông báo quan trọng về xoá quyền từ:  " + appName);

        Map<String, Object> vars = Map.of(
                "ADMIN_NAME", existUserAdmin.get().getCode() + " - " + existUserAdmin.get().getName(),
                "MY_NAME", sessionHelper.getUserCode() + " - " + sessionHelper.getUserName()
        );
        mailerDefaultRequest.setContent(MailerHelper.loadTemplate(MailerHelper.TEMPLATE_CHANGE_STATUS_ADMIN, vars));
        mailerHelper.send(mailerDefaultRequest);
        return RouterHelper.responseSuccess("Xóa tài khoản ban đào tạo thành công", userAdminId);
    }
}
