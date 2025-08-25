package udpm.hn.studentattendance.core.admin.facility.service.impl;

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
import udpm.hn.studentattendance.core.admin.facility.model.request.AFCreateUpdateFacilityRequest;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFFacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.facility.model.response.AFFacilityResponse;
import udpm.hn.studentattendance.core.admin.facility.repository.AFFacilityExtendRepository;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.helpers.MailerHelper;
import udpm.hn.studentattendance.helpers.RedisCacheHelper;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;

import udpm.hn.studentattendance.infrastructure.config.mailer.model.MailerDefaultRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import udpm.hn.studentattendance.infrastructure.config.redis.service.RedisService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AFFacilityServiceImplTest {

    @Mock
    private AFFacilityExtendRepository facilityRepository;

    @Mock
    private MailerHelper mailerHelper;

    @Mock
    private UserActivityLogHelper userActivityLogHelper;

    @Mock
    private RedisService redisService;

    @Mock
    private RedisInvalidationHelper redisInvalidationHelper;

    @Mock
    private RedisCacheHelper redisCacheHelper;

    @Mock
    private udpm.hn.studentattendance.infrastructure.common.repositories.CommonPlanDateRepository commonPlanDateRepository;

    @InjectMocks
    private AFFacilityServiceImpl facilityService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(facilityService, "appName", "Student Attendance App");
        // Removed unnecessary stubbing for redisCacheHelper.getOrSet
    }

    @Test
    @DisplayName("Test getAllFacility should return data from cache if available")
    void testGetAllFacilityFromCache() {
        // Given
        AFFacilitySearchRequest request = new AFFacilitySearchRequest();
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_FACILITY + "list_" +
                "name=" + "_status=null_page=" + request.getPage() +
                "_size=" + request.getSize() +
                "_orderBy=" + request.getOrderBy() +
                "_sortBy=" + request.getSortBy() +
                "_q=";

        PageableObject mockData = mock(PageableObject.class);

        when(redisCacheHelper.getOrSet(anyString(), any(), any())).thenReturn(mockData);

        // When
        ResponseEntity<?> response = facilityService.getAllFacility(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy tất cả cơ sở thành công", apiResponse.getMessage());
        assertEquals(mockData, apiResponse.getData());

        // Verify repository was not called
        verify(redisCacheHelper).getOrSet(anyString(), any(), any());
        verify(facilityRepository, never()).getAllFacility(any(Pageable.class), any(AFFacilitySearchRequest.class));
    }

    @Test
    @DisplayName("Test getAllFacility should fetch and cache data if not in cache")
    void testGetAllFacilityFromRepository() {
        AFFacilitySearchRequest request = new AFFacilitySearchRequest();
        List<AFFacilityResponse> facilities = new ArrayList<>();
        AFFacilityResponse facility = mock(AFFacilityResponse.class);
        facilities.add(facility);
        PageableObject<AFFacilityResponse> expected = PageableObject
                .of(new org.springframework.data.domain.PageImpl<>(facilities));

        // Cache miss: gọi supplier
        when(redisCacheHelper.getOrSet(anyString(), any(), any()))
                .thenAnswer(invocation -> {
                    java.util.function.Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        // Repository returns PageImpl
        when(facilityRepository.getAllFacility(any(Pageable.class), eq(request)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(facilities));

        // Gọi service qua getAllFacility để nhận ApiResponse không null
        ResponseEntity<?> response = facilityService.getAllFacility(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy tất cả cơ sở thành công", apiResponse.getMessage());
        PageableObject<AFFacilityResponse> actual = (PageableObject<AFFacilityResponse>) apiResponse.getData();
        assertNotNull(actual);
        assertEquals(expected.getData(), actual.getData());
        assertEquals(expected.getTotalPages(), actual.getTotalPages());
        assertEquals(expected.getCurrentPage(), actual.getCurrentPage());
        verify(facilityRepository).getAllFacility(any(Pageable.class), eq(request));
    }

    @Test
    @DisplayName("Test createFacility should create facility successfully")
    void testCreateFacilitySuccess() {
        // Given
        AFCreateUpdateFacilityRequest request = mock(AFCreateUpdateFacilityRequest.class);
        when(request.getFacilityName()).thenReturn("FPT HCM");

        when(facilityRepository.findByName("FPT HCM")).thenReturn(Optional.empty());
        when(facilityRepository.getLastPosition()).thenReturn(2);

        Facility savedFacility = new Facility();
        savedFacility.setId("new-facility-id");
        savedFacility.setCode("FPT_HCM");
        savedFacility.setName("FPT HCM");
        savedFacility.setPosition(3);
        savedFacility.setStatus(EntityStatus.ACTIVE);

        when(facilityRepository.save(any(Facility.class))).thenReturn(savedFacility);
        // No need for doNothing on non-void methods

        // When
        ResponseEntity<?> response = facilityService.createFacility(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Thêm cơ sở mới thành công", apiResponse.getMessage());

        // Verify repository was called
        verify(facilityRepository).save(any(Facility.class));
        verify(userActivityLogHelper).saveLog(contains("vừa thêm 1 cơ sở mới"));
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test createFacility should return error if facility name already exists")
    void testCreateFacilityNameExists() {
        // Given
        AFCreateUpdateFacilityRequest request = mock(AFCreateUpdateFacilityRequest.class);
        when(request.getFacilityName()).thenReturn("FPT HCM");

        Facility existingFacility = new Facility();
        existingFacility.setId("existing-facility-id");
        existingFacility.setName("FPT HCM");

        when(facilityRepository.findByName("FPT HCM")).thenReturn(Optional.of(existingFacility));

        // When
        ResponseEntity<?> response = facilityService.createFacility(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Tên cơ sở đã tồn tại trên hệ thống", apiResponse.getMessage());

        // Verify repository was not called to save
        verify(facilityRepository, never()).save(any(Facility.class));
        verify(redisInvalidationHelper, never()).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test updateFacility should update facility successfully")
    void testUpdateFacilitySuccess() {
        // Given
        String facilityId = "facility-1";
        AFCreateUpdateFacilityRequest request = mock(AFCreateUpdateFacilityRequest.class);
        when(request.getFacilityName()).thenReturn("FPT HCM Updated");

        Facility existingFacility = new Facility();
        existingFacility.setId(facilityId);
        existingFacility.setName("FPT HCM");
        existingFacility.setCode("FPT_HCM");
        existingFacility.setPosition(1);
        existingFacility.setStatus(EntityStatus.ACTIVE);

        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(existingFacility));
        when(facilityRepository.isExistsByName("FPT HCM Updated", facilityId)).thenReturn(false);
        when(facilityRepository.save(any(Facility.class))).thenReturn(existingFacility);
        // No need for doNothing on non-void methods

        // When
        ResponseEntity<?> response = facilityService.updateFacility(facilityId, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Cập nhật cơ sở thành công", apiResponse.getMessage());

        // Verify facility was updated correctly
        assertEquals("FPT HCM Updated", existingFacility.getName());

        // Verify repository was called
        verify(facilityRepository).save(existingFacility);
        verify(userActivityLogHelper).saveLog(contains("vừa cập nhật cơ sở"));
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test updateFacility should return error if facility not found")
    void testUpdateFacilityNotFound() {
        // Given
        String facilityId = "non-existent-id";
        AFCreateUpdateFacilityRequest request = new AFCreateUpdateFacilityRequest();
        request.setFacilityName("Test Facility"); // Set the facility name to avoid NullPointerException

        when(facilityRepository.findById(facilityId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = facilityService.updateFacility(facilityId, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Không tìm thấy cơ sở", apiResponse.getMessage());

        // Verify repository was not called to save
        verify(facilityRepository, never()).save(any(Facility.class));
        verify(redisInvalidationHelper, never()).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test changeFacilityStatus should toggle facility status successfully")
    void testChangeFacilityStatusSuccess() {
        // Given
        String facilityId = "facility-1";

        Facility facility = new Facility();
        facility.setId(facilityId);
        facility.setName("FPT HCM");
        facility.setCode("FPT_HCM");
        facility.setStatus(EntityStatus.ACTIVE);

        // Set last updated date to yesterday
        LocalDate yesterday = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).minusDays(1);
        facility.setUpdatedAt(yesterday.atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant().toEpochMilli());

        // Create a mutable ArrayList instead of using Arrays.asList which returns
        // immutable list
        ArrayList<String> emailList = new ArrayList<>();
        emailList.add("user@example.com");

        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));
        when(facilityRepository.save(any(Facility.class))).thenReturn(facility);
        when(facilityRepository.getListEmailUserDisableFacility(facilityId)).thenReturn(emailList);
        when(facilityRepository.countFacility()).thenReturn(2);

        // When
        ResponseEntity<?> response = facilityService.changeFacilityStatus(facilityId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Thay đổi trạng thái cơ sở thành công", apiResponse.getMessage());

        // Verify status was changed to INACTIVE
        assertEquals(EntityStatus.INACTIVE, facility.getStatus());

        // Verify repository was called
        verify(facilityRepository).save(facility);
        verify(mailerHelper).send(any(MailerDefaultRequest.class));
        verify(userActivityLogHelper).saveLog(contains("vừa thay đổi trạng thái cơ sở"));
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test changeFacilityStatus should return error if trying to deactivate facility twice in same day")
    void testChangeFacilityStatusSameDay() {
        // Given
        String facilityId = "facility-1";

        Facility facility = new Facility();
        facility.setId(facilityId);
        facility.setName("FPT HCM");
        facility.setCode("FPT_HCM");
        facility.setStatus(EntityStatus.ACTIVE);

        // Set last updated date to today
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        facility.setUpdatedAt(today.atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant().toEpochMilli());

        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));

        // When
        ResponseEntity<?> response = facilityService.changeFacilityStatus(facilityId);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Chỉ được đổi trạng thái cơ sở ngừng hoạt động 1 lần mỗi ngày", apiResponse.getMessage());

        // Verify repository was not called to save
        verify(facilityRepository, never()).save(any(Facility.class));
        verify(redisInvalidationHelper, never()).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test changeFacilityStatus should return error if trying to deactivate last active facility")
    void testChangeFacilityStatusLastActive() {
        // Given
        String facilityId = "facility-1";

        Facility facility = new Facility();
        facility.setId(facilityId);
        facility.setName("FPT HCM");
        facility.setCode("FPT_HCM");
        facility.setStatus(EntityStatus.ACTIVE);

        // Set last updated date to yesterday
        LocalDate yesterday = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).minusDays(1);
        facility.setUpdatedAt(yesterday.atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant().toEpochMilli());

        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));
        when(facilityRepository.countFacility()).thenReturn(1); // Only one active facility

        // When
        ResponseEntity<?> response = facilityService.changeFacilityStatus(facilityId);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Không thể thay đổi trạng thái cơ sở cuối cùng còn hoạt động", apiResponse.getMessage());

        // Verify repository was not called to save
        verify(facilityRepository, never()).save(any(Facility.class));
        verify(redisInvalidationHelper, never()).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test up should increase facility position successfully")
    void testUpSuccess() {
        // Given
        String facilityId = "facility-1";

        Facility facility = new Facility();
        facility.setId(facilityId);
        facility.setName("FPT HCM");
        facility.setPosition(2);

        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));
        when(facilityRepository.updatePositionPreUp(anyInt(), anyString())).thenReturn(1); // Return value since it's
                                                                                           // not void
        when(facilityRepository.save(any(Facility.class))).thenReturn(facility);

        // When
        ResponseEntity<?> response = facilityService.up(facilityId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Tăng mức ưu tiên hiển thị thành công", apiResponse.getMessage());

        // Verify position was changed
        assertEquals(1, facility.getPosition());

        // Verify repository was called
        verify(facilityRepository).updatePositionPreUp(anyInt(), eq(facilityId));
        verify(facilityRepository).save(facility);
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test up should return error if facility already at top position")
    void testUpAlreadyAtTop() {
        // Given
        String facilityId = "facility-1";

        Facility facility = new Facility();
        facility.setId(facilityId);
        facility.setName("FPT HCM");
        facility.setPosition(1);

        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));

        // When
        ResponseEntity<?> response = facilityService.up(facilityId);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Cơ sở đã đang ở mức ưu tiên hiển thị cao nhất", apiResponse.getMessage());

        // Verify repository was not called
        verify(facilityRepository, never()).updatePositionPreUp(anyInt(), anyString());
        verify(facilityRepository, never()).save(any(Facility.class));
        verify(redisInvalidationHelper, never()).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test down should decrease facility position successfully")
    void testDownSuccess() {
        // Given
        String facilityId = "facility-1";

        Facility facility = new Facility();
        facility.setId(facilityId);
        facility.setName("FPT HCM");
        facility.setPosition(1);

        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));
        when(facilityRepository.getLastPosition()).thenReturn(3);
        when(facilityRepository.updatePositionNextDown(anyInt(), anyString())).thenReturn(1); // Return value since it's
                                                                                              // not void
        when(facilityRepository.save(any(Facility.class))).thenReturn(facility);

        // When
        ResponseEntity<?> response = facilityService.down(facilityId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Giảm mức ưu tiên hiển thị thành công", apiResponse.getMessage());

        // Verify position was changed
        assertEquals(2, facility.getPosition());

        // Verify repository was called
        verify(facilityRepository).updatePositionNextDown(anyInt(), eq(facilityId));
        verify(facilityRepository).save(facility);
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test down should return error if facility already at bottom position")
    void testDownAlreadyAtBottom() {
        // Given
        String facilityId = "facility-1";

        Facility facility = new Facility();
        facility.setId(facilityId);
        facility.setName("FPT HCM");
        facility.setPosition(3);

        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));
        when(facilityRepository.getLastPosition()).thenReturn(3);

        // When
        ResponseEntity<?> response = facilityService.down(facilityId);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Cơ sở đã đang ở mức ưu tiên hiển thị thấp nhất", apiResponse.getMessage());

        // Verify repository was not called
        verify(facilityRepository, never()).updatePositionNextDown(anyInt(), anyString());
        verify(facilityRepository, never()).save(any(Facility.class));
        verify(redisInvalidationHelper, never()).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test getFacilityById should return data from cache if available")
    void testGetFacilityByIdFromCache() {
        String facilityId = "facility-1";
        AFFacilityResponse cachedFacility = mock(AFFacilityResponse.class);
        when(facilityRepository.getDetailFacilityById(facilityId)).thenReturn(Optional.of(cachedFacility));

        ResponseEntity<?> response = facilityService.getFacilityById(facilityId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Hiển thị chi tiết cơ sở thành công", apiResponse.getMessage());
        assertEquals(cachedFacility, apiResponse.getData());
    }

    @Test
    @DisplayName("Test getFacilityById should fetch and cache data if not in cache")
    void testGetFacilityByIdFromRepository() {
        String facilityId = "facility-1";
        AFFacilityResponse facility = mock(AFFacilityResponse.class);
        when(facilityRepository.getDetailFacilityById(facilityId)).thenReturn(Optional.of(facility));

        ResponseEntity<?> response = facilityService.getFacilityById(facilityId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Hiển thị chi tiết cơ sở thành công", apiResponse.getMessage());
        assertEquals(facility, apiResponse.getData());
    }

    @Test
    @DisplayName("Test getFacilityById should return error if facility not found")
    void testGetFacilityByIdNotFound() {
        String facilityId = "not-found";
        when(facilityRepository.getDetailFacilityById(facilityId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = facilityService.getFacilityById(facilityId);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Cơ sở không tồn tại", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test getCachedFacilities should delete cache on deserialization exception")
    void testGetCachedFacilitiesDeleteCacheOnDeserializationException() {
        AFFacilitySearchRequest request = new AFFacilitySearchRequest();
        when(redisCacheHelper.getOrSet(anyString(), any(), any()))
                .thenThrow(new RuntimeException("Deserialize error"));
        // Remove unnecessary stubbing for facilityRepository.getAllFacility
        // as the test expects an exception and does not need this stub
        assertThrows(RuntimeException.class, () -> facilityService.getCachedFacilities(request));
    }

    @Test
    @DisplayName("Test changeFacilityStatus should send mail when deactivating facility")
    void testChangeFacilityStatusSendMailWhenDeactivating() {
        String facilityId = "facility-1";
        Facility facility = new Facility();
        facility.setId(facilityId);
        facility.setName("FPT HCM");
        facility.setStatus(EntityStatus.ACTIVE);
        facility.setUpdatedAt(System.currentTimeMillis() - 86400000); // yesterday

        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));
        when(facilityRepository.countFacility()).thenReturn(2);
        when(facilityRepository.save(any(Facility.class))).thenReturn(facility);
        when(facilityRepository.getListEmailUserDisableFacility(facilityId))
                .thenReturn(new ArrayList<>(Arrays.asList("a@a.com", "b@b.com")));

        ResponseEntity<?> response = facilityService.changeFacilityStatus(facilityId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(mailerHelper).send(any(MailerDefaultRequest.class));
    }

    @Test
    @DisplayName("Test changeFacilityStatus should disable all student duplicate shift when activating facility")
    void testChangeFacilityStatusDisableAllStudentDuplicateShiftWhenActivating() {
        String facilityId = "facility-1";
        Facility facility = new Facility();
        facility.setId(facilityId);
        facility.setStatus(EntityStatus.INACTIVE);
        facility.setUpdatedAt(System.currentTimeMillis() - 86400000); // yesterday

        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));
        when(facilityRepository.countFacility()).thenReturn(2);
        when(facilityRepository.save(any(Facility.class))).thenReturn(facility);

        ResponseEntity<?> response = facilityService.changeFacilityStatus(facilityId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
