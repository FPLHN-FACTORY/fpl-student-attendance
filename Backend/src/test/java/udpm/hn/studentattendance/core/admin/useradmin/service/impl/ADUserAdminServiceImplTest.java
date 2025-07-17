package udpm.hn.studentattendance.core.admin.useradmin.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import udpm.hn.studentattendance.helpers.RedisCacheHelper;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.config.mailer.model.MailerDefaultRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import udpm.hn.studentattendance.template.BaseServiceTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ADUserAdminServiceImplTest extends BaseServiceTest {

    @Mock
    private ADUserAdminExtendRepository userAdminExtendRepository;

    @Mock
    private ADUserAdminStaffExtendRepository userAdminStaffExtendRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private MailerHelper mailerHelper;

    @Mock
    private RedisCacheHelper redisCacheHelper;

    @InjectMocks
    private ADUserAdminServiceImpl userAdminService;

    @BeforeEach
    void setUp() {
        setupCommonFields(userAdminService);
        setupCommonMocks();
        // Removed unnecessary stubbing for redisCacheHelper.getOrSet
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

        when(redisCacheHelper.getOrSet(eq(cacheKey), any(), any(), anyLong())).thenReturn(mockData);

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
        List<ADUserAdminResponse> responses = new ArrayList<>();
        ADUserAdminResponse mockResponse = mock(ADUserAdminResponse.class);
        responses.add(mockResponse);
        Page<ADUserAdminResponse> page = new PageImpl<>(responses);
        PageableObject<ADUserAdminResponse> pageableObject = PageableObject.of(page);
        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong()))
                .thenAnswer(invocation -> {
                    java.util.function.Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        when(userAdminExtendRepository.getAllUserAdmin(any(Pageable.class), eq(request))).thenReturn(page);

        // When
        ResponseEntity<?> response = userAdminService.getAllUserAdmin(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy thành công tất cả tài khoản admin", apiResponse.getMessage());
        PageableObject<ADUserAdminResponse> actual = (PageableObject<ADUserAdminResponse>) apiResponse.getData();
        assertNotNull(actual);
        assertEquals(pageableObject.getData(), actual.getData());
        assertEquals(pageableObject.getTotalPages(), actual.getTotalPages());
        assertEquals(pageableObject.getCurrentPage(), actual.getCurrentPage());
        // Verify repository was called and cache was updated
        verify(userAdminExtendRepository).getAllUserAdmin(any(Pageable.class), eq(request));
    }

    @Test
    @DisplayName("Test getUserAdminById should return admin if found")
    void testGetUserAdminByIdFromCache() {
        // Given
        String adminId = "admin-1";
        UserAdmin admin = new UserAdmin();
        admin.setId(adminId);
        when(userAdminExtendRepository.findById(adminId)).thenReturn(Optional.of(admin));

        // When
        ResponseEntity<?> response = userAdminService.getUserAdminById(adminId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy thành công tài khoản admin", apiResponse.getMessage());
        assertEquals(admin, apiResponse.getData());
        // Verify repository was called
        verify(userAdminExtendRepository).findById(adminId);
    }

    @Test
    @DisplayName("Test getUserAdminById should fetch and cache data if not in cache")
    void testGetUserAdminByIdFromRepository() {
        // Given
        String adminId = "admin-1";
        UserAdmin admin = new UserAdmin();
        admin.setId(adminId);
        admin.setName("Admin User");
        admin.setCode("AD001");
        when(userAdminExtendRepository.findById(adminId)).thenReturn(Optional.of(admin));

        // When
        ResponseEntity<?> response = userAdminService.getUserAdminById(adminId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy thành công tài khoản admin", apiResponse.getMessage());
        assertEquals(admin, apiResponse.getData());
        // Verify repository was called
        verify(userAdminExtendRepository).findById(adminId);
    }

    @Test
    @DisplayName("Test getUserAdminById should return error if admin not found")
    void testGetUserAdminByIdNotFound() {
        // Given
        String adminId = "non-existent-id";
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

        when(redisCacheHelper.getOrSet(eq(cacheKey), any(), any(), anyLong())).thenReturn(cachedStaff);

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
        List<UserStaff> userStaffList = new ArrayList<>();
        UserStaff staff = new UserStaff();
        staff.setId("staff-1");
        userStaffList.add(staff);
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_ADMIN + "staff_list";
        when(redisCacheHelper.getOrSet(eq(cacheKey), any(), any(), anyLong()))
                .thenAnswer(invocation -> {
                    java.util.function.Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        when(userAdminStaffExtendRepository.getAllUserStaff()).thenReturn(userStaffList);

        // When
        ResponseEntity<?> response = userAdminService.getAllUserStaff();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy thành công danh sách nhân viên", apiResponse.getMessage());
        assertEquals(userStaffList, apiResponse.getData());
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
    @DisplayName("Test deleteUserAdmin should return error when admin not found")
    void testDeleteUserAdminNotFound() {
        when(userAdminExtendRepository.findById("admin-1")).thenReturn(Optional.empty());

        ResponseEntity<?> response = userAdminService.deleteUserAdmin("admin-1");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userAdminExtendRepository, never()).deleteById(anyString());
        verify(redisInvalidationHelper, never()).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test deleteUserAdmin should return error when trying to delete own account")
    void testDeleteUserAdminSelf() {
        UserAdmin admin = new UserAdmin();
        admin.setId("admin-1");
        admin.setName("Admin User");
        admin.setStatus(EntityStatus.ACTIVE);

        when(userAdminExtendRepository.findById("admin-1")).thenReturn(Optional.of(admin));
        when(sessionHelper.getUserId()).thenReturn("admin-1");

        ResponseEntity<?> response = userAdminService.deleteUserAdmin("admin-1");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Không thể xóa tài khoản của chính mình", apiResponse.getMessage());
        verify(userAdminExtendRepository, never()).deleteById(anyString());
        verify(redisInvalidationHelper, never()).invalidateAllCaches();
    }

}