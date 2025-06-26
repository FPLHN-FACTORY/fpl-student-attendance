package udpm.hn.studentattendance.core.admin.userstaff.service.impl;

import jakarta.persistence.EntityManager;
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
import udpm.hn.studentattendance.core.admin.userstaff.model.request.ADCreateUpdateStaffRequest;
import udpm.hn.studentattendance.core.admin.userstaff.model.request.ADStaffRequest;
import udpm.hn.studentattendance.core.admin.userstaff.model.response.ADStaffDetailResponse;
import udpm.hn.studentattendance.core.admin.userstaff.model.response.ADStaffResponse;
import udpm.hn.studentattendance.core.admin.userstaff.repository.ADStaffExtendRepository;
import udpm.hn.studentattendance.core.admin.userstaff.repository.ADStaffFacilityExtendRepository;
import udpm.hn.studentattendance.core.admin.userstaff.repository.ADStaffRoleExtendRepository;
import udpm.hn.studentattendance.core.notification.model.request.NotificationAddRequest;
import udpm.hn.studentattendance.core.notification.service.NotificationService;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.Notification;
import udpm.hn.studentattendance.entities.Role;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ADStaffServiceImplTest {

    @Mock
    private ADStaffExtendRepository adStaffRepository;

    @Mock
    private ADStaffRoleExtendRepository adStaffRoleRepository;

    @Mock
    private ADStaffFacilityExtendRepository adminStaffFacilityRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SessionHelper sessionHelper;

    @Mock
    private UserActivityLogHelper userActivityLogHelper;

    @Mock
    private RedisService redisService;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private ADStaffServiceImpl adStaffService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adStaffService, "isDisableCheckEmailFpt", "false");
        ReflectionTestUtils.setField(adStaffService, "redisTTL", 3600L);
    }

    @Test
    @DisplayName("Test getAllStaffByFilter should return data from cache if available")
    void testGetAllStaffByFilterFromCache() {
        // Given
        ADStaffRequest request = new ADStaffRequest();
        String cacheKey = "staff:list:" + request.toString();
        PageableObject mockData = mock(PageableObject.class);

        when(redisService.get(cacheKey)).thenReturn(mockData);

        // When
        ResponseEntity<?> response = adStaffService.getAllStaffByFilter(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy tất cả giảng viên thành công (cached)", apiResponse.getMessage());
        assertEquals(mockData, apiResponse.getData());

        // Verify repository was not called
        verify(adStaffRepository, never()).getAllStaff(any(Pageable.class), any(ADStaffRequest.class));
    }

    @Test
    @DisplayName("Test getAllStaffByFilter should fetch and cache data if not in cache")
    void testGetAllStaffByFilterFromRepository() {
        // Given
        ADStaffRequest request = new ADStaffRequest();
        String cacheKey = "staff:list:" + request.toString();

        List<ADStaffResponse> staffList = new ArrayList<>();
        ADStaffResponse staff = mock(ADStaffResponse.class);
        staffList.add(staff);
        Page<ADStaffResponse> page = new PageImpl<>(staffList);

        when(redisService.get(cacheKey)).thenReturn(null);
        when(adStaffRepository.getAllStaff(any(Pageable.class), eq(request))).thenReturn(page);

        // When
        ResponseEntity<?> response = adStaffService.getAllStaffByFilter(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy tất cả giảng viên thành công", apiResponse.getMessage());

        // Verify repository was called and cache was updated
        verify(adStaffRepository).getAllStaff(any(Pageable.class), eq(request));
        verify(redisService).set(eq(cacheKey), any(PageableObject.class), eq(3600L));
    }

    @Test
    @DisplayName("Test createStaff should create staff successfully")
    void testCreateStaffSuccess() {
        // Given
        ADCreateUpdateStaffRequest request = mock(ADCreateUpdateStaffRequest.class);
        when(request.getStaffCode()).thenReturn("ST001");
        when(request.getName()).thenReturn("Nguyen Van Staff");
        when(request.getEmailFe()).thenReturn("staff@fe.edu.vn");
        when(request.getEmailFpt()).thenReturn("staff@fpt.edu.vn");
        when(request.getFacilityId()).thenReturn("facility-1");
        when(request.getRoleCodes()).thenReturn(Arrays.asList("0", "1")); // TEACHER, MANAGER roles

        when(adStaffRepository.findUserStaffByCode("ST001")).thenReturn(Optional.empty());
        when(adStaffRepository.findUserStaffByEmailFe("staff@fe.edu.vn")).thenReturn(Optional.empty());
        when(adStaffRepository.findUserStaffByEmailFpt("staff@fpt.edu.vn")).thenReturn(Optional.empty());

        UserStaff savedStaff = new UserStaff();
        savedStaff.setId("new-staff-id");
        savedStaff.setCode("ST001");
        savedStaff.setName("Nguyen Van Staff");
        savedStaff.setEmailFe("staff@fe.edu.vn");
        savedStaff.setEmailFpt("staff@fpt.edu.vn");
        savedStaff.setStatus(EntityStatus.ACTIVE);

        when(adStaffRepository.save(any(UserStaff.class))).thenReturn(savedStaff);

        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setName("FPT HCM");
        when(entityManager.find(eq(Facility.class), eq("facility-1"))).thenReturn(facility);

        when(sessionHelper.getUserCode()).thenReturn("ADMIN");
        when(sessionHelper.getUserName()).thenReturn("Admin User");
        when(sessionHelper.getUserId()).thenReturn("admin-id");

        Notification notification = new Notification();
        when(notificationService.add(any(NotificationAddRequest.class))).thenReturn(notification);

        // When
        ResponseEntity<?> response = adStaffService.createStaff(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Thêm nhân viên mới thành công", apiResponse.getMessage());

        // Verify repository was called
        verify(adStaffRepository).save(any(UserStaff.class));
        verify(adStaffRoleRepository, times(2)).save(any(Role.class));
        verify(notificationService, times(2)).add(any(NotificationAddRequest.class));
        verify(userActivityLogHelper).saveLog(contains("vừa thêm 1 nhân viên mới"));
        verify(redisService).deletePattern("staff:list:*");
    }

    @Test
    @DisplayName("Test createStaff should return error for invalid code")
    void testCreateStaffInvalidCode() {
        // Given
        ADCreateUpdateStaffRequest request = mock(ADCreateUpdateStaffRequest.class);
        when(request.getStaffCode()).thenReturn("ST 001"); // Invalid code with space
        when(request.getName()).thenReturn("Nguyen Van Staff");
        when(request.getEmailFe()).thenReturn("staff@fe.edu.vn");
        when(request.getEmailFpt()).thenReturn("staff@fpt.edu.vn");

        // When
        ResponseEntity<?> response = adStaffService.createStaff(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.getMessage().contains("Mã nhân viên không hợp lệ"));

        // Verify repository was not called
        verify(adStaffRepository, never()).save(any(UserStaff.class));
    }

    @Test
    @DisplayName("Test createStaff should return error for invalid name")
    void testCreateStaffInvalidName() {
        // Given
        ADCreateUpdateStaffRequest request = mock(ADCreateUpdateStaffRequest.class);
        when(request.getStaffCode()).thenReturn("ST001");
        when(request.getName()).thenReturn("Staff123"); // Invalid name with numbers
        when(request.getEmailFe()).thenReturn("staff@fe.edu.vn");
        when(request.getEmailFpt()).thenReturn("staff@fpt.edu.vn");

        // When
        ResponseEntity<?> response = adStaffService.createStaff(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.getMessage().contains("Tên nhân viên không hợp lệ"));

        // Verify repository was not called
        verify(adStaffRepository, never()).save(any(UserStaff.class));
    }

    @Test
    @DisplayName("Test createStaff should return error for invalid email")
    void testCreateStaffInvalidEmail() {
        // Given
        ADCreateUpdateStaffRequest request = mock(ADCreateUpdateStaffRequest.class);
        when(request.getStaffCode()).thenReturn("ST001");
        when(request.getName()).thenReturn("Nguyen Van Staff");
        when(request.getEmailFe()).thenReturn("staff@gmail.com"); // Invalid FE email
        when(request.getEmailFpt()).thenReturn("staff@fpt.edu.vn");

        // When
        ResponseEntity<?> response = adStaffService.createStaff(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.getMessage().contains("Email FE"));

        // Verify repository was not called
        verify(adStaffRepository, never()).save(any(UserStaff.class));
    }

    @Test
    @DisplayName("Test getStaffById should return staff from cache if available")
    void testGetStaffByIdFromCache() {
        // Given
        String staffId = "staff-1";
        String cacheKey = "staff:detail:" + staffId;
        ADStaffDetailResponse cachedStaff = mock(ADStaffDetailResponse.class);

        when(redisService.get(cacheKey)).thenReturn(cachedStaff);

        // When
        ResponseEntity<?> response = adStaffService.getStaffById(staffId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Xem chi tiết giảng viên thành công (cached)", apiResponse.getMessage());
        assertEquals(cachedStaff, apiResponse.getData());

        // Verify repository was not called
        verify(adStaffRepository, never()).getDetailStaff(staffId);
    }

    @Test
    @DisplayName("Test getStaffById should fetch and cache data if not in cache")
    void testGetStaffByIdFromRepository() {
        // Given
        String staffId = "staff-1";
        String cacheKey = "staff:detail:" + staffId;

        ADStaffDetailResponse staffDetail = mock(ADStaffDetailResponse.class);

        when(redisService.get(cacheKey)).thenReturn(null);
        when(adStaffRepository.getDetailStaff(staffId)).thenReturn(Optional.of(staffDetail));

        // When
        ResponseEntity<?> response = adStaffService.getStaffById(staffId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Xem chi tiết giảng viên thành công", apiResponse.getMessage());

        // Verify repository was called and cache was updated
        verify(adStaffRepository).getDetailStaff(staffId);
        verify(redisService).set(eq(cacheKey), eq(staffDetail), eq(3600L));
    }

    @Test
    @DisplayName("Test getStaffById should return error if staff not found")
    void testGetStaffByIdNotFound() {
        // Given
        String staffId = "non-existent-id";
        String cacheKey = "staff:detail:" + staffId;

        when(redisService.get(cacheKey)).thenReturn(null);
        when(adStaffRepository.getDetailStaff(staffId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = adStaffService.getStaffById(staffId);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Giảng viên không tồn tại", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test changeStaffStatus should toggle staff status successfully")
    void testChangeStaffStatusSuccess() {
        // Given
        String staffId = "staff-1";

        UserStaff staff = new UserStaff();
        staff.setId(staffId);
        staff.setName("Staff User");
        staff.setCode("ST001");
        staff.setStatus(EntityStatus.ACTIVE);

        List<Role> roles = new ArrayList<>();
        Role role = new Role();
        role.setId("role-1");
        role.setCode(RoleConstant.TEACHER);
        role.setStatus(EntityStatus.ACTIVE);
        roles.add(role);

        when(adStaffRepository.findById(staffId)).thenReturn(Optional.of(staff));
        when(adStaffRoleRepository.findAllByUserStaffId(staffId)).thenReturn(roles);
        when(adStaffRepository.save(any(UserStaff.class))).thenReturn(staff);
        when(adStaffRoleRepository.saveAll(anyList())).thenReturn(roles);

        // When
        ResponseEntity<?> response = adStaffService.changeStaffStatus(staffId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Thay đổi trạng thái giảng viên thành công", apiResponse.getMessage());

        // Verify status was changed to INACTIVE
        assertEquals(EntityStatus.INACTIVE, staff.getStatus());

        // Verify repository was called
        verify(adStaffRepository).save(staff);
        verify(adStaffRoleRepository).saveAll(anyList());
        verify(userActivityLogHelper).saveLog(contains("vừa thay đổi trạng thái nhân viên"));
        verify(redisService).delete("staff:detail:" + staffId);
        verify(redisService).deletePattern("staff:list:*");
    }

    @Test
    @DisplayName("Test changeStaffStatus should return error if staff not found")
    void testChangeStaffStatusNotFound() {
        // Given
        String staffId = "non-existent-id";

        when(adStaffRepository.findById(staffId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = adStaffService.changeStaffStatus(staffId);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Nhân viên không tồn tại", apiResponse.getMessage());

        // Verify repository was not called to save
        verify(adStaffRepository, never()).save(any(UserStaff.class));
    }

    @Test
    @DisplayName("Test getAllRole should return roles from cache if available")
    void testGetAllRoleFromCache() {
        // Given
        String cacheKey = "staff:roles:all";
        List<Role> cachedRoles = new ArrayList<>();

        when(redisService.get(cacheKey)).thenReturn(cachedRoles);

        // When
        ResponseEntity<?> response = adStaffService.getAllRole();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy tất cả vai trò thành công (cached)", apiResponse.getMessage());
        assertEquals(cachedRoles, apiResponse.getData());

        // Verify repository was not called
        verify(adStaffRoleRepository, never()).getAllRole();
    }

    @Test
    @DisplayName("Test getAllRole should fetch and cache data if not in cache")
    void testGetAllRoleFromRepository() {
        // Given
        String cacheKey = "staff:roles:all";
        List<Role> roles = new ArrayList<>();
        Role role = new Role();
        role.setId("role-1");
        role.setCode(RoleConstant.TEACHER);
        roles.add(role);

        when(redisService.get(cacheKey)).thenReturn(null);
        when(adStaffRoleRepository.getAllRole()).thenReturn(roles);

        // When
        ResponseEntity<?> response = adStaffService.getAllRole();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy tất cả vai trò thành công", apiResponse.getMessage());
        assertEquals(roles, apiResponse.getData());

        // Verify repository was called and cache was updated
        verify(adStaffRoleRepository).getAllRole();
        verify(redisService).set(eq(cacheKey), eq(roles), eq(3600L * 4));
    }

    @Test
    @DisplayName("Test getAllFacility should return facilities from cache if available")
    void testGetAllFacilityFromCache() {
        // Given
        String cacheKey = "staff:facilities:active";
        List<Facility> cachedFacilities = new ArrayList<>();

        when(redisService.get(cacheKey)).thenReturn(cachedFacilities);

        // When
        ResponseEntity<?> response = adStaffService.getAllFacility();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy tất cả cơ sở thành công (cached)", apiResponse.getMessage());
        assertEquals(cachedFacilities, apiResponse.getData());

        // Verify repository was not called
        verify(adminStaffFacilityRepository, never()).getFacility(any(EntityStatus.class));
    }

    @Test
    @DisplayName("Test getAllFacility should fetch and cache data if not in cache")
    void testGetAllFacilityFromRepository() {
        // Given
        String cacheKey = "staff:facilities:active";
        List<Facility> facilities = new ArrayList<>();
        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setName("FPT HCM");
        facilities.add(facility);

        when(redisService.get(cacheKey)).thenReturn(null);
        when(adminStaffFacilityRepository.getFacility(EntityStatus.ACTIVE)).thenReturn(facilities);

        // When
        ResponseEntity<?> response = adStaffService.getAllFacility();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy tất cả cơ sở thành công", apiResponse.getMessage());
        assertEquals(facilities, apiResponse.getData());

        // Verify repository was called and cache was updated
        verify(adminStaffFacilityRepository).getFacility(EntityStatus.ACTIVE);
        verify(redisService).set(eq(cacheKey), eq(facilities), eq(3600L * 4));
    }
}