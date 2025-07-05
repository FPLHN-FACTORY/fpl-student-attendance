package udpm.hn.studentattendance.core.admin.useradmin.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import udpm.hn.studentattendance.core.admin.useradmin.model.request.ADUserAdminChangePowerShiftRequest;
import udpm.hn.studentattendance.core.admin.useradmin.model.request.ADUserAdminCreateOrUpdateRequest;
import udpm.hn.studentattendance.core.admin.useradmin.model.request.ADUserAdminRequest;
import udpm.hn.studentattendance.core.admin.useradmin.model.response.ADUserAdminResponse;
import udpm.hn.studentattendance.core.admin.useradmin.repository.ADUserAdminExtendRepository;
import udpm.hn.studentattendance.core.admin.useradmin.repository.ADUserAdminStaffExtendRepository;
import udpm.hn.studentattendance.core.notification.model.request.NotificationAddRequest;
import udpm.hn.studentattendance.core.notification.service.NotificationService;
import udpm.hn.studentattendance.entities.Notification;
import udpm.hn.studentattendance.entities.UserAdmin;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.helpers.MailerHelper;
import udpm.hn.studentattendance.helpers.NotificationHelper;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.config.mailer.model.MailerDefaultRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ADUserAdminServiceImplTest {

    @Mock
    private ADUserAdminExtendRepository userAdminExtendRepository;

    @Mock
    private ADUserAdminStaffExtendRepository userAdminStaffExtendRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SessionHelper sessionHelper;

    @Mock
    private MailerHelper mailerHelper;

    @Mock
    private UserActivityLogHelper userActivityLogHelper;

    @Mock
    private RedisService redisService;

    @Mock
    private RedisInvalidationHelper redisInvalidationHelper;

    @InjectMocks
    private ADUserAdminServiceImpl userAdminService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userAdminService, "appName", "Student Attendance App");
        ReflectionTestUtils.setField(userAdminService, "isDisableCheckEmailFpt", "false");
        ReflectionTestUtils.setField(userAdminService, "redisTTL", 3600L);
    }

    @Test
    @DisplayName("Test getAllUserAdmin should return data from cache if available")
    void testGetAllUserAdminFromCache() {
        // Given
        ADUserAdminRequest request = new ADUserAdminRequest();
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_ADMIN + "list_" +
                "page=" + request.getPage() +
                "_size=" + request.getSize() +
                "_orderBy=" + request.getOrderBy() +
                "_sortBy=" + request.getSortBy() +
                "_q=" +
                "_searchQuery=" +
                "_status=";

        PageableObject<ADUserAdminResponse> mockData = mock(PageableObject.class);

        when(redisService.get(cacheKey)).thenReturn(mockData);
        when(redisService.getObject(eq(cacheKey), eq(PageableObject.class))).thenReturn(mockData);

        // When
        ResponseEntity<?> response = userAdminService.getAllUserAdmin(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy thành công tất cả tài khoản admin", apiResponse.getMessage());
        assertEquals(mockData, apiResponse.getData());

        // Verify repository was not called
        verify(userAdminExtendRepository, never()).getAllUserAdmin(any(Pageable.class), any(ADUserAdminRequest.class));
    }

    @Test
    @DisplayName("Test getAllUserAdmin should fetch and cache data if not in cache")
    void testGetAllUserAdminFromRepository() {
        // Given
        ADUserAdminRequest request = new ADUserAdminRequest();
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_ADMIN + "list_" +
                "page=" + request.getPage() +
                "_size=" + request.getSize() +
                "_orderBy=" + request.getOrderBy() +
                "_sortBy=" + request.getSortBy() +
                "_q=" +
                "_searchQuery=" +
                "_status=";

        // Create a Page with a mocked ADUserAdminResponse
        List<ADUserAdminResponse> responses = new ArrayList<>();
        ADUserAdminResponse mockResponse = mock(ADUserAdminResponse.class);
        responses.add(mockResponse);

        Page<ADUserAdminResponse> page = new PageImpl<>(responses);

        when(redisService.get(cacheKey)).thenReturn(null);
        when(userAdminExtendRepository.getAllUserAdmin(any(Pageable.class), eq(request))).thenReturn(page);

        // When
        ResponseEntity<?> response = userAdminService.getAllUserAdmin(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy thành công tất cả tài khoản admin", apiResponse.getMessage());

        // Verify repository was called and cache was updated
        verify(userAdminExtendRepository).getAllUserAdmin(any(Pageable.class), eq(request));
        verify(redisService).set(eq(cacheKey), any(PageableObject.class), eq(3600L));
    }

    @Test
    @DisplayName("Test getUserAdminById should return admin from cache if available")
    void testGetUserAdminByIdFromCache() {
        // Given
        String adminId = "admin-1";
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_ADMIN + adminId;
        UserAdmin cachedAdmin = new UserAdmin();
        cachedAdmin.setId(adminId);

        when(redisService.get(cacheKey)).thenReturn(cachedAdmin);
        when(redisService.getObject(eq(cacheKey), eq(UserAdmin.class))).thenReturn(cachedAdmin);

        // When
        ResponseEntity<?> response = userAdminService.getUserAdminById(adminId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy thành công tài khoản admin", apiResponse.getMessage());
        assertEquals(cachedAdmin, apiResponse.getData());

        // Verify repository was not called
        verify(userAdminExtendRepository, never()).findById(adminId);
    }

    @Test
    @DisplayName("Test getUserAdminById should fetch and cache data if not in cache")
    void testGetUserAdminByIdFromRepository() {
        // Given
        String adminId = "admin-1";
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_ADMIN + adminId;

        UserAdmin admin = new UserAdmin();
        admin.setId(adminId);
        admin.setName("Admin User");
        admin.setCode("AD001");

        when(redisService.get(cacheKey)).thenReturn(null);
        when(userAdminExtendRepository.findById(adminId)).thenReturn(Optional.of(admin));

        // When
        ResponseEntity<?> response = userAdminService.getUserAdminById(adminId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy thành công tài khoản admin", apiResponse.getMessage());

        // Verify repository was called and cache was updated
        verify(userAdminExtendRepository).findById(adminId);
        verify(redisService).set(eq(cacheKey), eq(admin), eq(3600L));
    }

    @Test
    @DisplayName("Test getUserAdminById should return error if admin not found")
    void testGetUserAdminByIdNotFound() {
        // Given
        String adminId = "non-existent-id";
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_ADMIN + adminId;

        when(redisService.get(cacheKey)).thenReturn(null);
        when(userAdminExtendRepository.findById(adminId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = userAdminService.getUserAdminById(adminId);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Admin không tồn tại", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test createUserAdmin should create admin successfully")
    void testCreateUserAdminSuccess() {
        // Given
        ADUserAdminCreateOrUpdateRequest request = mock(ADUserAdminCreateOrUpdateRequest.class);
        when(request.getStaffCode()).thenReturn("AD001");
        when(request.getStaffName()).thenReturn("Nguyen Van Admin");
        when(request.getEmail()).thenReturn("admin@fpt.edu.vn");

        String userId = "current-user-id";

        when(userAdminExtendRepository.getUserAdminByCode("AD001")).thenReturn(Optional.empty());
        when(userAdminExtendRepository.getUserAdminByEmail("admin@fpt.edu.vn")).thenReturn(Optional.empty());
        when(sessionHelper.getUserId()).thenReturn(userId);

        UserAdmin savedAdmin = new UserAdmin();
        savedAdmin.setId("new-admin-id");
        savedAdmin.setCode("AD001");
        savedAdmin.setName("Nguyen Van Admin");
        savedAdmin.setEmail("admin@fpt.edu.vn");

        when(userAdminExtendRepository.save(any(UserAdmin.class))).thenReturn(savedAdmin);

        Notification notification = new Notification();
        when(notificationService.add(any(NotificationAddRequest.class))).thenReturn(notification);

        // When
        ResponseEntity<?> response = userAdminService.createUserAdmin(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Thêm admin mới thành công", apiResponse.getMessage());

        // Verify repository was called
        verify(userAdminExtendRepository).save(any(UserAdmin.class));
        verify(notificationService).add(any(NotificationAddRequest.class));
        verify(userActivityLogHelper).saveLog(contains("vừa thêm 1 tài khoản admin mới"));
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test createUserAdmin should return error for invalid name")
    void testCreateUserAdminInvalidName() {
        // Given
        ADUserAdminCreateOrUpdateRequest request = mock(ADUserAdminCreateOrUpdateRequest.class);
        when(request.getStaffCode()).thenReturn("AD001");
        when(request.getStaffName()).thenReturn("Admin123"); // Invalid name with numbers
        when(request.getEmail()).thenReturn("admin@fpt.edu.vn");

        // When
        ResponseEntity<?> response = userAdminService.createUserAdmin(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.getMessage().contains("Tên admin không hợp lệ"));

        // Verify repository was not called
        verify(userAdminExtendRepository, never()).save(any(UserAdmin.class));
        verify(redisInvalidationHelper, never()).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test createUserAdmin should return error for invalid email")
    void testCreateUserAdminInvalidEmail() {
        // Given
        ADUserAdminCreateOrUpdateRequest request = mock(ADUserAdminCreateOrUpdateRequest.class);
        when(request.getStaffCode()).thenReturn("AD001");
        when(request.getStaffName()).thenReturn("Nguyen Van Admin");
        when(request.getEmail()).thenReturn("admin@example.com"); // Invalid domain

        // When
        ResponseEntity<?> response = userAdminService.createUserAdmin(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.getMessage().contains("Email phải"));

        // Verify repository was not called
        verify(userAdminExtendRepository, never()).save(any(UserAdmin.class));
        verify(redisInvalidationHelper, never()).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test updateUserAdmin should update admin successfully")
    void testUpdateUserAdminSuccess() {
        // Given
        String adminId = "admin-1";
        ADUserAdminCreateOrUpdateRequest request = mock(ADUserAdminCreateOrUpdateRequest.class);
        when(request.getStaffCode()).thenReturn("AD001");
        when(request.getStaffName()).thenReturn("Nguyen Van Admin Updated");
        when(request.getEmail()).thenReturn("admin_updated@fpt.edu.vn");

        UserAdmin existingAdmin = new UserAdmin();
        existingAdmin.setId(adminId);
        existingAdmin.setCode("AD001_OLD");
        existingAdmin.setName("Nguyen Van Admin");
        existingAdmin.setEmail("admin@fpt.edu.vn");

        when(userAdminExtendRepository.findById(adminId)).thenReturn(Optional.of(existingAdmin));
        when(userAdminExtendRepository.isExistCodeUpdate("AD001", "AD001_OLD")).thenReturn(false);
        when(userAdminExtendRepository.isExistEmailUpdate("admin_updated@fpt.edu.vn", "admin@fpt.edu.vn"))
                .thenReturn(false);
        when(userAdminExtendRepository.save(any(UserAdmin.class))).thenReturn(existingAdmin);
        when(sessionHelper.getUserId()).thenReturn("current-user-id");
        when(notificationService.add(any(NotificationAddRequest.class))).thenReturn(new Notification());

        // When
        ResponseEntity<?> response = userAdminService.updateUserAdmin(request, adminId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Cập nhật admin thành công", apiResponse.getMessage());

        // Verify repository was called
        verify(userAdminExtendRepository).save(any(UserAdmin.class));
        verify(notificationService).add(any(NotificationAddRequest.class));
        verify(userActivityLogHelper).saveLog(contains("vừa cập nhật tài khoản admin"));
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test changeStatus should toggle admin status successfully")
    void testChangeStatusSuccess() {
        // Given
        String adminId = "admin-1";
        String currentUserId = "current-user-id";

        UserAdmin admin = new UserAdmin();
        admin.setId(adminId);
        admin.setName("Admin User");
        admin.setCode("AD001");
        admin.setEmail("admin@fpt.edu.vn");
        admin.setStatus(EntityStatus.ACTIVE);

        when(userAdminExtendRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(sessionHelper.getUserId()).thenReturn(currentUserId);
        when(sessionHelper.getUserCode()).thenReturn("CURRENT_USER");
        when(sessionHelper.getUserName()).thenReturn("Current User");
        when(userAdminExtendRepository.save(any(UserAdmin.class))).thenReturn(admin);

        // When
        ResponseEntity<?> response = userAdminService.changeStatus(adminId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Thay đổi trạng thái thành công", apiResponse.getMessage());

        // Verify status was changed to INACTIVE
        assertEquals(EntityStatus.INACTIVE, admin.getStatus());

        // Verify repository was called
        verify(userAdminExtendRepository).save(admin);
        verify(mailerHelper).send(any(MailerDefaultRequest.class));
        verify(userActivityLogHelper).saveLog(contains("vừa thay đổi trạng thái tài khoản admin"));
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test changeStatus should return error when trying to change own status")
    void testChangeStatusSelf() {
        // Given
        String adminId = "admin-1";

        UserAdmin admin = new UserAdmin();
        admin.setId(adminId);
        admin.setName("Admin User");
        admin.setCode("AD001");
        admin.setStatus(EntityStatus.ACTIVE);

        when(userAdminExtendRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(sessionHelper.getUserId()).thenReturn(adminId); // Same user ID

        // When
        ResponseEntity<?> response = userAdminService.changeStatus(adminId);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Không được sửa trạng thái của chính bản thân", apiResponse.getMessage());

        // Verify repository was not called
        verify(userAdminExtendRepository, never()).save(any(UserAdmin.class));
        verify(redisInvalidationHelper, never()).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test isMySelf should return true when IDs match")
    void testIsMySelfTrue() {
        // Given
        String adminId = "admin-1";
        String currentUserId = "admin-1";

        when(sessionHelper.getUserId()).thenReturn(currentUserId);

        // When
        ResponseEntity<?> response = userAdminService.isMySelf(adminId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Kiểm tra thành công", apiResponse.getMessage());
        assertEquals(true, apiResponse.getData());
    }

    @Test
    @DisplayName("Test isMySelf should return false when IDs don't match")
    void testIsMySelfFalse() {
        // Given
        String adminId = "admin-1";
        String currentUserId = "admin-2";

        when(sessionHelper.getUserId()).thenReturn(currentUserId);

        // When
        ResponseEntity<?> response = userAdminService.isMySelf(adminId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Kiểm tra thành công", apiResponse.getMessage());
        assertEquals(false, apiResponse.getData());
    }

    @Test
    @DisplayName("Test getAllUserStaff should return staff from cache if available")
    void testGetAllUserStaffFromCache() {
        // Given
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_ADMIN + "staff_list";
        List<UserStaff> cachedStaff = new ArrayList<>();

        when(redisService.get(cacheKey)).thenReturn(cachedStaff);
        when(redisService.getObject(eq(cacheKey), eq(List.class))).thenReturn(cachedStaff);

        // When
        ResponseEntity<?> response = userAdminService.getAllUserStaff();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy thành công danh sách nhân viên", apiResponse.getMessage());
        assertEquals(cachedStaff, apiResponse.getData());

        // Verify repository was not called
        verify(userAdminStaffExtendRepository, never()).getAllUserStaff();
    }

    @Test
    @DisplayName("Test getAllUserStaff should fetch and cache data if not in cache")
    void testGetAllUserStaffFromRepository() {
        // Given
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_ADMIN + "staff_list";
        List<UserStaff> staffList = new ArrayList<>();
        UserStaff staff = new UserStaff();
        staff.setId("staff-1");
        staff.setName("Staff User");
        staff.setCode("ST001");
        staffList.add(staff);

        when(redisService.get(cacheKey)).thenReturn(null);
        when(userAdminStaffExtendRepository.getAllUserStaff()).thenReturn(staffList);

        // When
        ResponseEntity<?> response = userAdminService.getAllUserStaff();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy thành công danh sách nhân viên", apiResponse.getMessage());
        assertEquals(staffList, apiResponse.getData());

        // Verify repository was called and cache was updated
        verify(userAdminStaffExtendRepository).getAllUserStaff();
        verify(redisService).set(eq(cacheKey), eq(staffList), eq(3600L));
    }

    @Test
    @DisplayName("Test deleteUserAdmin should delete admin successfully")
    void testDeleteUserAdminSuccess() {
        // Given
        String adminId = "admin-1";

        UserAdmin admin = new UserAdmin();
        admin.setId(adminId);
        admin.setName("Admin User");
        admin.setCode("AD001");
        admin.setEmail("admin@fpt.edu.vn");

        when(userAdminExtendRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(sessionHelper.getUserCode()).thenReturn("CURRENT_USER");
        when(sessionHelper.getUserName()).thenReturn("Current User");
        doNothing().when(userAdminExtendRepository).deleteById(adminId);

        // When
        ResponseEntity<?> response = userAdminService.deleteUserAdmin(adminId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Xóa tài khoản admin thành công", apiResponse.getMessage());

        // Verify repository was called
        verify(userAdminExtendRepository).deleteById(adminId);
        verify(mailerHelper).send(any(MailerDefaultRequest.class));
        verify(userActivityLogHelper).saveLog(contains("vừa xóa tài khoản admin"));
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test deleteUserAdmin should return error if admin not found")
    void testDeleteUserAdminNotFound() {
        // Given
        String adminId = "non-existent-id";

        when(userAdminExtendRepository.findById(adminId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = userAdminService.deleteUserAdmin(adminId);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Không tìm thấy tài khoản admin", apiResponse.getMessage());

        // Verify repository was not called to delete
        verify(userAdminExtendRepository, never()).deleteById(anyString());
        verify(redisInvalidationHelper, never()).invalidateAllCaches();
    }
}