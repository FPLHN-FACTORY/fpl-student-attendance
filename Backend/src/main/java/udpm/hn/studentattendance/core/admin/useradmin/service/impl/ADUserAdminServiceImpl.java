package udpm.hn.studentattendance.core.admin.useradmin.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
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
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.config.mailer.model.MailerDefaultRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;

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

        private final UserActivityLogHelper userActivityLogHelper;
        private final RedisService redisService;

        @Value("${app.config.app-name}")
        private String appName;

        @Value("${app.config.disabled-check-email-fpt}")
        private String isDisableCheckEmailFpt;

        @Value("${spring.cache.redis.time-to-live}")
        private long redisTTL;

        /**
         * Lấy danh sách admin từ cache hoặc DB
         */
        public PageableObject getUserAdminList(ADUserAdminRequest request) {
                // Tạo cache key thủ công
                String cacheKey = RedisPrefixConstant.REDIS_PREFIX_ADMIN + "list_" +
                                "page=" + request.getPage() +
                                "_size=" + request.getSize() +
                                "_orderBy=" + request.getOrderBy() +
                                "_sortBy=" + request.getSortBy() +
                                "_q=" + (request.getQ() != null ? request.getQ() : "") +
                                "_searchQuery=" + (request.getSearchQuery() != null ? request.getSearchQuery() : "") +
                                "_status=" + (request.getStatus() != null ? request.getStatus() : "");

                // Kiểm tra cache
                Object cachedData = redisService.get(cacheKey);
                if (cachedData != null) {
                        try {
                                return redisService.getObject(cacheKey, PageableObject.class);
                        } catch (Exception e) {
                                redisService.delete(cacheKey);
                        }
                }

                // Cache miss - fetch from database
                Pageable pageable = PaginationHelper.createPageable(request);
                PageableObject list = PageableObject.of(userAdminExtendRepository.getAllUserAdmin(pageable, request));

                // Store in cache
                try {
                        redisService.set(cacheKey, list, redisTTL);
                } catch (Exception ignored) {
                }

                return list;
        }

        /**
         * Lấy thông tin chi tiết admin từ cache hoặc DB
         */
        public UserAdmin getCachedUserAdminById(String id) {
                String cacheKey = RedisPrefixConstant.REDIS_PREFIX_ADMIN + id;

                // Kiểm tra cache
                Object cachedData = redisService.get(cacheKey);
                if (cachedData != null) {
                        try {
                                return redisService.getObject(cacheKey, UserAdmin.class);
                        } catch (Exception e) {
                                redisService.delete(cacheKey);
                        }
                }

                // Cache miss - fetch from database
                Optional<UserAdmin> optionalUserAdmin = userAdminExtendRepository.findById(id);
                UserAdmin userAdmin = optionalUserAdmin.orElse(null);

                // Store in cache if found
                if (userAdmin != null) {
                        try {
                                redisService.set(cacheKey, userAdmin, redisTTL);
                        } catch (Exception ignored) {
                        }
                }

                return userAdmin;
        }

        /**
         * Lấy danh sách nhân viên từ cache hoặc DB
         */
        public List<UserStaff> getAllUserStaffList() {
                String cacheKey = RedisPrefixConstant.REDIS_PREFIX_ADMIN + "staff_list";

                // Kiểm tra cache
                Object cachedData = redisService.get(cacheKey);
                if (cachedData != null) {
                        try {
                                return redisService.getObject(cacheKey, List.class);
                        } catch (Exception e) {
                                redisService.delete(cacheKey);
                        }
                }

                // Cache miss - fetch from database
                List<UserStaff> userStaffList = userAdminStaffExtendRepository.getAllUserStaff();

                // Store in cache
                try {
                        redisService.set(cacheKey, userStaffList, redisTTL);
                } catch (Exception ignored) {
                }

                return userStaffList;
        }

        @Override
        public ResponseEntity<?> getAllUserAdmin(ADUserAdminRequest request) {
                PageableObject list = getUserAdminList(request);
                return RouterHelper.responseSuccess("Lấy thành công tất cả tài khoản admin", list);
        }

        @Override
        public ResponseEntity<?> getUserAdminById(String id) {
                UserAdmin userAdmin = getCachedUserAdminById(id);
                if (userAdmin == null) {
                        return RouterHelper.responseError("Admin không tồn tại");
                }
                return RouterHelper.responseSuccess("Lấy thành công tài khoản admin", userAdmin);
        }

        @Override
        public ResponseEntity<?> createUserAdmin(ADUserAdminCreateOrUpdateRequest createOrUpdateRequest) {
                Optional<UserAdmin> existUserAdmin = userAdminExtendRepository
                                .getUserAdminByCode(createOrUpdateRequest.getStaffCode());
                Optional<UserAdmin> existUserAdmin2 = userAdminExtendRepository
                                .getUserAdminByEmail(createOrUpdateRequest.getEmail());

                if (!ValidateHelper.isValidFullname(createOrUpdateRequest.getStaffName())) {
                        return RouterHelper.responseError(
                                        "Tên admin không hợp lệ: Tối thiểu 2 từ, cách nhau bởi khoảng trắng và Chỉ gồm ký tự chữ không chứa số hay ký tự đặc biệt.");
                }

                String email = createOrUpdateRequest.getEmail().trim();
                boolean isValidEmail = false;

                if (ValidateHelper.isValidEmailGmail(email) ||
                                ValidateHelper.isValidEmailFE(email) ||
                                ValidateHelper.isValidEmailFPT(email)) {
                        isValidEmail = true;
                }

                if (!isValidEmail) {
                        return RouterHelper
                                        .responseError("Email phải có định dạng @gmail.com hoặc kết thúc bằng edu.vn");
                }

                if (!isDisableCheckEmailFpt.equalsIgnoreCase("true")) {
                        if (!ValidateHelper.isValidEmailFE(email) && !ValidateHelper.isValidEmailFPT(email)) {
                                return RouterHelper.responseError("Email phải kết thúc bằng edu.vn");
                        }
                }

                if (existUserAdmin.isPresent()) {
                        return RouterHelper.responseError("Mã của admin đã tồn tại");
                }
                if (existUserAdmin2.isPresent()) {
                        return RouterHelper.responseError("Email của admin đã tồn tại");
                }

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

                userActivityLogHelper.saveLog("vừa thêm 1 tài khoản admin mới: " + userAdmin.getCode() + " - "
                                + userAdmin.getName());

                // Invalidate related caches
                invalidateAdminCaches();

                return RouterHelper.responseSuccess("Thêm admin mới thành công", userAdmin);
        }

        @Override
        public ResponseEntity<?> updateUserAdmin(ADUserAdminCreateOrUpdateRequest createOrUpdateRequest, String id) {
                if (!ValidateHelper.isValidFullname(createOrUpdateRequest.getStaffName())) {
                        return RouterHelper.responseError(
                                        "Tên admin không hợp lệ: Tối thiểu 2 từ, cách nhau bởi khoảng trắng và Chỉ gồm ký tự chữ không chứa số hay ký tự đặc biệt.");
                }

                Optional<UserAdmin> opt = userAdminExtendRepository.findById(id);
                if (opt.isEmpty()) {
                        return RouterHelper.responseError("Không tìm thấy nhân viên");
                }
                UserAdmin current = opt.get();

                if (userAdminExtendRepository.isExistCodeUpdate(createOrUpdateRequest.getStaffCode(),
                                current.getCode())) {
                        return RouterHelper.responseError("Mã admin đã tồn tại");
                }

                if (userAdminExtendRepository.isExistEmailUpdate(createOrUpdateRequest.getEmail(),
                                current.getEmail())) {
                        return RouterHelper.responseError("Đã có admin khác dùng email fe này");
                }

                String email = createOrUpdateRequest.getEmail().trim();
                boolean isValidEmail = false;

                if (ValidateHelper.isValidEmailGmail(email) ||
                                ValidateHelper.isValidEmailFE(email) ||
                                ValidateHelper.isValidEmailFPT(email)) {
                        isValidEmail = true;
                }

                if (!isValidEmail) {
                        return RouterHelper
                                        .responseError("Email phải có định dạng @gmail.com hoặc kết thúc bằng edu.vn");
                }

                if (!isDisableCheckEmailFpt.equalsIgnoreCase("true")) {
                        if (!ValidateHelper.isValidEmailFE(email) && !ValidateHelper.isValidEmailFPT(email)) {
                                return RouterHelper.responseError("Email phải kết thúc bằng edu.vn");
                        }
                }

                if (!ValidateHelper.isValidCode(createOrUpdateRequest.getStaffCode())) {
                        return RouterHelper.responseError(
                                        "Mã admin không hợp lệ: không có khoảng trắng, không có ký tự đặc biệt ngoài dấu chấm . và dấu gạch dưới _.");
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

                userActivityLogHelper.saveLog("vừa cập nhật tài khoản admin: " + userAdmin.getCode() + " - "
                                + userAdmin.getName());

                // Invalidate specific cache for this admin
                invalidateAdminCache(id);

                return RouterHelper.responseSuccess("Cập nhật admin thành công", userAdmin);
        }

        @Override
        public ResponseEntity<?> changeStatus(String id) {
                Optional<UserAdmin> optionalUserAdmin = userAdminExtendRepository.findById(id);

                if (optionalUserAdmin.get().getId().equalsIgnoreCase(sessionHelper.getUserId())) {
                        return RouterHelper.responseError("Không được sửa trạng thái của chính bản thân");
                } else if (optionalUserAdmin.isPresent()) {
                        UserAdmin userAdmin = optionalUserAdmin.get();
                        String oldStatus = userAdmin.getStatus() == EntityStatus.ACTIVE ? "Hoạt động"
                                        : "Không hoạt động";
                        userAdmin.setStatus(userAdmin.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE
                                        : EntityStatus.ACTIVE);
                        String newStatus = userAdmin.getStatus() == EntityStatus.ACTIVE ? "Hoạt động"
                                        : "Không hoạt động";
                        UserAdmin saveAdmin = userAdminExtendRepository.save(userAdmin);
                        if (saveAdmin.getStatus() == EntityStatus.INACTIVE) {
                                MailerDefaultRequest mailerDefaultRequest = new MailerDefaultRequest();
                                mailerDefaultRequest.setTo(saveAdmin.getEmail());
                                mailerDefaultRequest.setTemplate(null);
                                mailerDefaultRequest.setTitle("Thông báo quan trọng về xoá quyền từ:  " + appName);

                                Map<String, Object> vars = Map.of(
                                                "ADMIN_NAME", saveAdmin.getCode() + " - " + saveAdmin.getName(),
                                                "MY_NAME",
                                                sessionHelper.getUserCode() + " - " + sessionHelper.getUserName());
                                mailerDefaultRequest.setContent(MailerHelper
                                                .loadTemplate(MailerHelper.TEMPLATE_CHANGE_STATUS_ADMIN, vars));
                                mailerHelper.send(mailerDefaultRequest);
                        }

                        userActivityLogHelper.saveLog("vừa thay đổi trạng thái tài khoản admin " + saveAdmin.getCode()
                                        + " - " + saveAdmin.getName() + " từ " + oldStatus + " thành " + newStatus);

                        // Invalidate specific cache for this admin
                        invalidateAdminCache(id);

                        return RouterHelper.responseSuccess("Thay đổi trạng thái thành công", saveAdmin);
                }

                return RouterHelper.responseError("Admin không tồn tại");
        }

        @Override
        public ResponseEntity<?> isMySelf(String userAdminId) {
                String currentUserId = sessionHelper.getUserId();
                boolean isSelf = currentUserId.equalsIgnoreCase(userAdminId);
                return RouterHelper.responseSuccess("Kiểm tra thành công", isSelf);
        }

        @Override
        public ResponseEntity<?> changePowerShift(ADUserAdminChangePowerShiftRequest userAdminChangePowerShiftRequest) {
                Optional<UserStaff> userStaff = userAdminStaffExtendRepository
                                .findById(userAdminChangePowerShiftRequest.getUserStaffId());
                List<UserAdmin> userAdminList = userAdminExtendRepository.findAll();

                boolean emailFeExists = userAdminList.stream()
                                .anyMatch(a -> a.getEmail().equalsIgnoreCase(userStaff.get().getEmailFe()));

                boolean emailFptExists = userAdminList.stream()
                                .anyMatch(a -> a.getEmail().equalsIgnoreCase(userStaff.get().getEmailFpt()));
                if (emailFeExists || emailFptExists) {
                        return RouterHelper.responseError("Nhân viên này đã có quyền admin");
                }
                if (userStaff.isPresent()) {
                        UserAdmin oldAdmin = userAdminExtendRepository
                                        .findById(userAdminChangePowerShiftRequest.getUserAdminId()).orElse(null);

                        UserAdmin userAdmin = new UserAdmin();
                        userAdmin.setCode(userStaff.get().getCode());
                        userAdmin.setName(userStaff.get().getName());
                        userAdmin.setEmail(userStaff.get().getEmailFe());
                        userAdmin.setStatus(EntityStatus.ACTIVE);
                        userAdminExtendRepository.save(userAdmin);

                        userAdminExtendRepository.deleteById(userAdminChangePowerShiftRequest.getUserAdminId());

                        String oldAdminName = oldAdmin != null ? oldAdmin.getName() + " (" + oldAdmin.getCode() + ")"
                                        : "Unknown";
                        userActivityLogHelper.saveLog("vừa chuyển quyền admin từ " + oldAdminName + " sang "
                                        + userAdmin.getName() + " (" + userAdmin.getCode() + ")");

                        // Invalidate related caches
                        invalidateAdminCaches();
                        if (oldAdmin != null) {
                                redisService.delete(RedisPrefixConstant.REDIS_PREFIX_ADMIN + oldAdmin.getId());
                        }

                        return RouterHelper.responseSuccess("Kiểm tra thành công", userAdmin);
                }
                return RouterHelper.responseError("Giảng viên hoặc phụ trách xưởng không tồn tại");
        }

        @Override
        public ResponseEntity<?> getAllUserStaff() {
                List<UserStaff> userStaffList = getAllUserStaffList();
                return RouterHelper.responseSuccess("Lấy thành công danh sách nhân viên", userStaffList);
        }

        @Override
        public ResponseEntity<?> deleteUserAdmin(String userAdminId) {
                Optional<UserAdmin> existUserAdmin = userAdminExtendRepository.findById(userAdminId);
                if (existUserAdmin.isEmpty()) {
                        return RouterHelper.responseError("Không tìm thấy tài khoản admin");
                }

                UserAdmin adminToDelete = existUserAdmin.get();
                userAdminExtendRepository.deleteById(userAdminId);
                MailerDefaultRequest mailerDefaultRequest = new MailerDefaultRequest();
                mailerDefaultRequest.setTo(adminToDelete.getEmail());
                mailerDefaultRequest.setTemplate(null);
                mailerDefaultRequest.setTitle("Thông báo quan trọng về xoá quyền từ:  " + appName);

                Map<String, Object> vars = Map.of(
                                "ADMIN_NAME", adminToDelete.getCode() + " - " + adminToDelete.getName(),
                                "MY_NAME", sessionHelper.getUserCode() + " - " + sessionHelper.getUserName());
                mailerDefaultRequest
                                .setContent(MailerHelper.loadTemplate(MailerHelper.TEMPLATE_CHANGE_STATUS_ADMIN, vars));
                mailerHelper.send(mailerDefaultRequest);

                userActivityLogHelper.saveLog("vừa xóa tài khoản admin: " + adminToDelete.getCode() + " - "
                                + adminToDelete.getName());

                // Invalidate specific cache for this admin
                invalidateAdminCache(userAdminId);

                return RouterHelper.responseSuccess("Xóa tài khoản admin thành công", userAdminId);
        }

        /**
         * Xóa cache liên quan đến một admin cụ thể
         */
        private void invalidateAdminCache(String adminId) {
                redisService.delete(RedisPrefixConstant.REDIS_PREFIX_ADMIN + adminId);
                invalidateAdminCaches();
        }

        /**
         * Xóa cache liên quan đến admin
         */
        private void invalidateAdminCaches() {
                // Invalidate all admin user-related cache using pattern matching
                redisService.deletePattern(RedisPrefixConstant.REDIS_PREFIX_ADMIN + "list_*");
                redisService.delete(RedisPrefixConstant.REDIS_PREFIX_ADMIN + "staff_list");
        }
}
