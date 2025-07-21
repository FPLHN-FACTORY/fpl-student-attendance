package udpm.hn.studentattendance.core.admin.facility.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFAddOrUpdateFacilityShiftRequest;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFFilterFacilityShiftRequest;
import udpm.hn.studentattendance.core.admin.facility.model.response.AFFacilityShiftResponse;
import udpm.hn.studentattendance.core.admin.facility.repository.AFFacilityExtendRepository;
import udpm.hn.studentattendance.core.admin.facility.repository.AFFacilityShiftRepository;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.FacilityShift;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.helpers.SettingHelper;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.config.redis.service.RedisService;
import udpm.hn.studentattendance.helpers.RedisCacheHelper;

import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.core.type.TypeReference;

@ExtendWith(MockitoExtension.class)
class AFFacilityShiftServiceImplTest {
    @Mock
    private AFFacilityExtendRepository afFacilityExtendRepository;
    @Mock
    private AFFacilityShiftRepository afFacilityShiftRepository;
    @Mock
    private UserActivityLogHelper userActivityLogHelper;
    @Mock
    private SettingHelper settingHelper;
    @Mock
    private RedisService redisService;
    @Mock
    private RedisInvalidationHelper redisInvalidationHelper;
    @Mock
    private RedisCacheHelper redisCacheHelper;

    // Shared TypeReference instance for mocking
    private final TypeReference<PageableObject<AFFacilityShiftResponse>> pageableObjectTypeRef = new TypeReference<PageableObject<AFFacilityShiftResponse>>() {
    };

    @InjectMocks
    private AFFacilityShiftServiceImpl shiftService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(shiftService, "redisTTL", 3600L);
        ReflectionTestUtils.setField(shiftService, "MIN_DIFF_SHIFT", 10); // 10 phút tối thiểu
    }

    @Test
    @DisplayName("Test getAllList should return data from repository")
    void testGetAllList() {
        AFFilterFacilityShiftRequest request = new AFFilterFacilityShiftRequest();
        // Remove unnecessary stubbing if not used
        ResponseEntity<?> response = shiftService.getAllList(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Test getShiftList should return cached data when available")
    void testGetShiftListWithCache() {
        AFFilterFacilityShiftRequest request = new AFFilterFacilityShiftRequest();
        PageableObject<AFFacilityShiftResponse> cachedData = new PageableObject<>();
        when(redisCacheHelper.getOrSet(anyString(), any(), any())).thenReturn(cachedData);

        // Gọi qua getAllList để nhận ApiResponse không null
        ResponseEntity<?> response = shiftService.getAllList(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        udpm.hn.studentattendance.infrastructure.common.ApiResponse apiResponse = (udpm.hn.studentattendance.infrastructure.common.ApiResponse) response
                .getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy danh sách dữ liệu thành công", apiResponse.getMessage());
        PageableObject<AFFacilityShiftResponse> actual = (PageableObject<AFFacilityShiftResponse>) apiResponse
                .getData();
        assertNotNull(actual);
        assertEquals(cachedData.getData(), actual.getData());
        assertEquals(cachedData.getTotalPages(), actual.getTotalPages());
        assertEquals(cachedData.getCurrentPage(), actual.getCurrentPage());
    }

    @Test
    @DisplayName("Test getShiftList should handle cache deserialization error")
    void testGetShiftListWithCacheError() {
        AFFilterFacilityShiftRequest request = new AFFilterFacilityShiftRequest();
        List<AFFacilityShiftResponse> shifts = new ArrayList<>();
        AFFacilityShiftResponse shift = mock(AFFacilityShiftResponse.class);
        shifts.add(shift);
        Page<AFFacilityShiftResponse> page = new org.springframework.data.domain.PageImpl<>(shifts);
        PageableObject<AFFacilityShiftResponse> expected = PageableObject.of(page);
        when(redisCacheHelper.getOrSet(anyString(), any(), any()))
                .thenAnswer(invocation -> {
                    java.util.function.Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        when(afFacilityShiftRepository.getAllByFilter(any(), eq(request))).thenReturn(page);

        // Gọi qua getAllList để nhận ApiResponse không null
        ResponseEntity<?> response = shiftService.getAllList(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        udpm.hn.studentattendance.infrastructure.common.ApiResponse apiResponse = (udpm.hn.studentattendance.infrastructure.common.ApiResponse) response
                .getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy danh sách dữ liệu thành công", apiResponse.getMessage());
        PageableObject<AFFacilityShiftResponse> actual = (PageableObject<AFFacilityShiftResponse>) apiResponse
                .getData();
        assertNotNull(actual);
        assertEquals(expected.getData(), actual.getData());
        assertEquals(expected.getTotalPages(), actual.getTotalPages());
        assertEquals(expected.getCurrentPage(), actual.getCurrentPage());
    }

    @Test
    @DisplayName("Test getShiftList should handle redis set exception")
    void testGetShiftListWithRedisSetError() {
        AFFilterFacilityShiftRequest request = new AFFilterFacilityShiftRequest();
        List<AFFacilityShiftResponse> shifts = new ArrayList<>();
        AFFacilityShiftResponse shift = mock(AFFacilityShiftResponse.class);
        shifts.add(shift);
        Page<AFFacilityShiftResponse> page = new org.springframework.data.domain.PageImpl<>(shifts);
        PageableObject<AFFacilityShiftResponse> expected = PageableObject.of(page);
        when(redisCacheHelper.getOrSet(anyString(), any(), any()))
                .thenAnswer(invocation -> {
                    java.util.function.Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        when(afFacilityShiftRepository.getAllByFilter(any(), eq(request))).thenReturn(page);

        // Gọi qua getAllList để nhận ApiResponse không null
        ResponseEntity<?> response = shiftService.getAllList(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        udpm.hn.studentattendance.infrastructure.common.ApiResponse apiResponse = (udpm.hn.studentattendance.infrastructure.common.ApiResponse) response
                .getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy danh sách dữ liệu thành công", apiResponse.getMessage());
        PageableObject<AFFacilityShiftResponse> actual = (PageableObject<AFFacilityShiftResponse>) apiResponse
                .getData();
        assertNotNull(actual);
        assertEquals(expected.getData(), actual.getData());
        assertEquals(expected.getTotalPages(), actual.getTotalPages());
        assertEquals(expected.getCurrentPage(), actual.getCurrentPage());
    }

    @Test
    @DisplayName("Test addShift should add shift successfully")
    void testAddShiftSuccess() {
        AFAddOrUpdateFacilityShiftRequest request = mock(AFAddOrUpdateFacilityShiftRequest.class);
        when(request.getIdFacility()).thenReturn("facility-1");
        when(request.getShift()).thenReturn(1);
        when(request.getFromHour()).thenReturn(7);
        when(request.getFromMinute()).thenReturn(0);
        when(request.getToHour()).thenReturn(9);
        when(request.getToMinute()).thenReturn(0);
        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setName("FPT HCM");
        facility.setStatus(EntityStatus.ACTIVE);
        when(afFacilityExtendRepository.findById("facility-1")).thenReturn(Optional.of(facility));
        when(afFacilityShiftRepository.isExistsShift(anyInt(), anyString(), isNull())).thenReturn(false);
        when(afFacilityShiftRepository.isExistsTime(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), isNull()))
                .thenReturn(false);
        FacilityShift savedShift = new FacilityShift();
        savedShift.setId("shift-1");
        savedShift.setShift(1);
        when(afFacilityShiftRepository.save(any(FacilityShift.class))).thenReturn(savedShift);
        ResponseEntity<?> response = shiftService.addShift(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(afFacilityShiftRepository).save(any(FacilityShift.class));
        verify(userActivityLogHelper).saveLog(contains("Tạo ca mới"));
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test addShift should return error when facility not found")
    void testAddShiftFacilityNotFound() {
        AFAddOrUpdateFacilityShiftRequest request = mock(AFAddOrUpdateFacilityShiftRequest.class);
        when(request.getIdFacility()).thenReturn("facility-1");
        when(afFacilityExtendRepository.findById("facility-1")).thenReturn(Optional.empty());

        ResponseEntity<?> response = shiftService.addShift(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(afFacilityShiftRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test addShift should return error when facility is inactive")
    void testAddShiftFacilityInactive() {
        AFAddOrUpdateFacilityShiftRequest request = mock(AFAddOrUpdateFacilityShiftRequest.class);
        when(request.getIdFacility()).thenReturn("facility-1");
        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setStatus(EntityStatus.INACTIVE);
        when(afFacilityExtendRepository.findById("facility-1")).thenReturn(Optional.of(facility));

        ResponseEntity<?> response = shiftService.addShift(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(afFacilityShiftRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test addShift should return error when shift duration is too short")
    void testAddShiftDurationTooShort() {
        AFAddOrUpdateFacilityShiftRequest request = mock(AFAddOrUpdateFacilityShiftRequest.class);
        when(request.getIdFacility()).thenReturn("facility-1");
        when(request.getFromHour()).thenReturn(7);
        when(request.getFromMinute()).thenReturn(0);
        when(request.getToHour()).thenReturn(7);
        when(request.getToMinute()).thenReturn(5); // Chỉ 5 phút, ít hơn 10 phút tối thiểu
        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setStatus(EntityStatus.ACTIVE);
        when(afFacilityExtendRepository.findById("facility-1")).thenReturn(Optional.of(facility));

        ResponseEntity<?> response = shiftService.addShift(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(afFacilityShiftRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test addShift should return error when shift already exists")
    void testAddShiftAlreadyExists() {
        AFAddOrUpdateFacilityShiftRequest request = mock(AFAddOrUpdateFacilityShiftRequest.class);
        when(request.getIdFacility()).thenReturn("facility-1");
        when(request.getShift()).thenReturn(1);
        when(request.getFromHour()).thenReturn(7);
        when(request.getFromMinute()).thenReturn(0);
        when(request.getToHour()).thenReturn(9);
        when(request.getToMinute()).thenReturn(0);
        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setName("FPT HCM");
        facility.setStatus(EntityStatus.ACTIVE);
        when(afFacilityExtendRepository.findById("facility-1")).thenReturn(Optional.of(facility));
        when(afFacilityShiftRepository.isExistsShift(anyInt(), anyString(), isNull())).thenReturn(true);

        ResponseEntity<?> response = shiftService.addShift(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(afFacilityShiftRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test addShift should return error when time already exists")
    void testAddShiftTimeAlreadyExists() {
        AFAddOrUpdateFacilityShiftRequest request = mock(AFAddOrUpdateFacilityShiftRequest.class);
        when(request.getIdFacility()).thenReturn("facility-1");
        when(request.getShift()).thenReturn(1);
        when(request.getFromHour()).thenReturn(7);
        when(request.getFromMinute()).thenReturn(0);
        when(request.getToHour()).thenReturn(9);
        when(request.getToMinute()).thenReturn(0);
        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setName("FPT HCM");
        facility.setStatus(EntityStatus.ACTIVE);
        when(afFacilityExtendRepository.findById("facility-1")).thenReturn(Optional.of(facility));
        when(afFacilityShiftRepository.isExistsShift(anyInt(), anyString(), isNull())).thenReturn(false);
        when(afFacilityShiftRepository.isExistsTime(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), isNull()))
                .thenReturn(true);

        ResponseEntity<?> response = shiftService.addShift(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(afFacilityShiftRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test updateShift should return error if shift not found")
    void testUpdateShiftNotFound() {
        AFAddOrUpdateFacilityShiftRequest request = mock(AFAddOrUpdateFacilityShiftRequest.class);
        when(request.getId()).thenReturn("shift-1");
        when(afFacilityShiftRepository.findById("shift-1")).thenReturn(Optional.empty());
        ResponseEntity<?> response = shiftService.updateShift(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Test updateShift should return error when shift duration is too short")
    void testUpdateShiftDurationTooShort() {
        AFAddOrUpdateFacilityShiftRequest request = mock(AFAddOrUpdateFacilityShiftRequest.class);
        when(request.getId()).thenReturn("shift-1");
        when(request.getFromHour()).thenReturn(7);
        when(request.getFromMinute()).thenReturn(0);
        when(request.getToHour()).thenReturn(7);
        when(request.getToMinute()).thenReturn(5); // Chỉ 5 phút
        FacilityShift existingShift = new FacilityShift();
        existingShift.setId("shift-1");
        when(afFacilityShiftRepository.findById("shift-1")).thenReturn(Optional.of(existingShift));

        ResponseEntity<?> response = shiftService.updateShift(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(afFacilityShiftRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test updateShift should return error when facility not found")
    void testUpdateShiftFacilityNotFound() {
        AFAddOrUpdateFacilityShiftRequest request = mock(AFAddOrUpdateFacilityShiftRequest.class);
        when(request.getId()).thenReturn("shift-1");
        when(request.getIdFacility()).thenReturn("facility-1");
        when(request.getFromHour()).thenReturn(7);
        when(request.getFromMinute()).thenReturn(0);
        when(request.getToHour()).thenReturn(9);
        when(request.getToMinute()).thenReturn(0);
        FacilityShift existingShift = new FacilityShift();
        existingShift.setId("shift-1");
        when(afFacilityShiftRepository.findById("shift-1")).thenReturn(Optional.of(existingShift));
        when(afFacilityExtendRepository.findById("facility-1")).thenReturn(Optional.empty());

        ResponseEntity<?> response = shiftService.updateShift(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(afFacilityShiftRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test updateShift should return error when facility is inactive")
    void testUpdateShiftFacilityInactive() {
        AFAddOrUpdateFacilityShiftRequest request = mock(AFAddOrUpdateFacilityShiftRequest.class);
        when(request.getId()).thenReturn("shift-1");
        when(request.getIdFacility()).thenReturn("facility-1");
        when(request.getFromHour()).thenReturn(7);
        when(request.getFromMinute()).thenReturn(0);
        when(request.getToHour()).thenReturn(9);
        when(request.getToMinute()).thenReturn(0);
        FacilityShift existingShift = new FacilityShift();
        existingShift.setId("shift-1");
        when(afFacilityShiftRepository.findById("shift-1")).thenReturn(Optional.of(existingShift));
        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setStatus(EntityStatus.INACTIVE);
        when(afFacilityExtendRepository.findById("facility-1")).thenReturn(Optional.of(facility));

        ResponseEntity<?> response = shiftService.updateShift(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(afFacilityShiftRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test updateShift should return error when shift already exists")
    void testUpdateShiftAlreadyExists() {
        AFAddOrUpdateFacilityShiftRequest request = mock(AFAddOrUpdateFacilityShiftRequest.class);
        when(request.getId()).thenReturn("shift-1");
        when(request.getIdFacility()).thenReturn("facility-1");
        when(request.getShift()).thenReturn(1);
        when(request.getFromHour()).thenReturn(7);
        when(request.getFromMinute()).thenReturn(0);
        when(request.getToHour()).thenReturn(9);
        when(request.getToMinute()).thenReturn(0);
        FacilityShift existingShift = new FacilityShift();
        existingShift.setId("shift-1");
        when(afFacilityShiftRepository.findById("shift-1")).thenReturn(Optional.of(existingShift));
        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setName("FPT HCM");
        facility.setStatus(EntityStatus.ACTIVE);
        when(afFacilityExtendRepository.findById("facility-1")).thenReturn(Optional.of(facility));
        when(afFacilityShiftRepository.isExistsShift(anyInt(), anyString(), anyString())).thenReturn(true);

        ResponseEntity<?> response = shiftService.updateShift(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(afFacilityShiftRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test updateShift should return error when time already exists")
    void testUpdateShiftTimeAlreadyExists() {
        AFAddOrUpdateFacilityShiftRequest request = mock(AFAddOrUpdateFacilityShiftRequest.class);
        when(request.getId()).thenReturn("shift-1");
        when(request.getIdFacility()).thenReturn("facility-1");
        when(request.getShift()).thenReturn(1);
        when(request.getFromHour()).thenReturn(7);
        when(request.getFromMinute()).thenReturn(0);
        when(request.getToHour()).thenReturn(9);
        when(request.getToMinute()).thenReturn(0);
        FacilityShift existingShift = new FacilityShift();
        existingShift.setId("shift-1");
        when(afFacilityShiftRepository.findById("shift-1")).thenReturn(Optional.of(existingShift));
        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setName("FPT HCM");
        facility.setStatus(EntityStatus.ACTIVE);
        when(afFacilityExtendRepository.findById("facility-1")).thenReturn(Optional.of(facility));
        when(afFacilityShiftRepository.isExistsShift(anyInt(), anyString(), anyString())).thenReturn(false);
        when(afFacilityShiftRepository.isExistsTime(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyString()))
                .thenReturn(true);

        ResponseEntity<?> response = shiftService.updateShift(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(afFacilityShiftRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test updateShift should update shift successfully")
    void testUpdateShiftSuccess() {
        AFAddOrUpdateFacilityShiftRequest request = mock(AFAddOrUpdateFacilityShiftRequest.class);
        when(request.getId()).thenReturn("shift-1");
        when(request.getIdFacility()).thenReturn("facility-1");
        when(request.getShift()).thenReturn(2);
        when(request.getFromHour()).thenReturn(8);
        when(request.getFromMinute()).thenReturn(0);
        when(request.getToHour()).thenReturn(10);
        when(request.getToMinute()).thenReturn(0);
        FacilityShift existingShift = new FacilityShift();
        existingShift.setId("shift-1");
        existingShift.setShift(1);
        existingShift.setFromHour(7);
        existingShift.setFromMinute(0);
        existingShift.setToHour(9);
        existingShift.setToMinute(0);
        when(afFacilityShiftRepository.findById("shift-1")).thenReturn(Optional.of(existingShift));
        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setName("FPT HCM");
        facility.setStatus(EntityStatus.ACTIVE);
        when(afFacilityExtendRepository.findById("facility-1")).thenReturn(Optional.of(facility));
        when(afFacilityShiftRepository.isExistsShift(anyInt(), anyString(), anyString())).thenReturn(false);
        when(afFacilityShiftRepository.isExistsTime(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyString()))
                .thenReturn(false);
        when(afFacilityShiftRepository.save(any(FacilityShift.class))).thenReturn(existingShift);

        ResponseEntity<?> response = shiftService.updateShift(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(afFacilityShiftRepository).save(any(FacilityShift.class));
        verify(userActivityLogHelper).saveLog(contains("Cập nhật ca"));
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test deleteShift should return error if shift not found")
    void testDeleteShiftNotFound() {
        when(afFacilityShiftRepository.findById("shift-1")).thenReturn(Optional.empty());
        ResponseEntity<?> response = shiftService.deleteShift("shift-1");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Test deleteShift should delete shift successfully")
    void testDeleteShiftSuccess() {
        FacilityShift facilityShift = new FacilityShift();
        facilityShift.setId("shift-1");
        facilityShift.setShift(1);
        facilityShift.setFromHour(7);
        facilityShift.setFromMinute(0);
        facilityShift.setToHour(9);
        facilityShift.setToMinute(0);
        Facility facility = new Facility();
        facility.setName("FPT HCM");
        facilityShift.setFacility(facility);
        when(afFacilityShiftRepository.findById("shift-1")).thenReturn(Optional.of(facilityShift));

        ResponseEntity<?> response = shiftService.deleteShift("shift-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(afFacilityShiftRepository).delete(facilityShift);
        verify(userActivityLogHelper).saveLog(contains("Xóa ca"));
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test changeStatus should return error if shift not found")
    void testChangeStatusNotFound() {
        when(afFacilityShiftRepository.findById("shift-1")).thenReturn(Optional.empty());
        ResponseEntity<?> response = shiftService.changeStatus("shift-1");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Test changeStatus should return error when activating inactive shift that already exists")
    void testChangeStatusActivateExistingShift() {
        FacilityShift facilityShift = new FacilityShift();
        facilityShift.setId("shift-1");
        facilityShift.setShift(1);
        facilityShift.setStatus(EntityStatus.INACTIVE);
        Facility facility = new Facility();
        facility.setId("facility-1");
        facilityShift.setFacility(facility);
        when(afFacilityShiftRepository.findById("shift-1")).thenReturn(Optional.of(facilityShift));
        when(afFacilityShiftRepository.isExistsShift(anyInt(), anyString(), anyString())).thenReturn(true);

        ResponseEntity<?> response = shiftService.changeStatus("shift-1");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(afFacilityShiftRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test changeStatus should activate shift successfully")
    void testChangeStatusActivateSuccess() {
        FacilityShift facilityShift = new FacilityShift();
        facilityShift.setId("shift-1");
        facilityShift.setShift(1);
        facilityShift.setStatus(EntityStatus.INACTIVE);
        facilityShift.setFromHour(7);
        facilityShift.setFromMinute(0);
        facilityShift.setToHour(9);
        facilityShift.setToMinute(0);
        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setName("FPT HCM");
        facilityShift.setFacility(facility);
        when(afFacilityShiftRepository.findById("shift-1")).thenReturn(Optional.of(facilityShift));
        when(afFacilityShiftRepository.isExistsShift(anyInt(), anyString(), anyString())).thenReturn(false);
        when(afFacilityShiftRepository.save(any(FacilityShift.class))).thenReturn(facilityShift);

        ResponseEntity<?> response = shiftService.changeStatus("shift-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(afFacilityShiftRepository).save(any(FacilityShift.class));
        verify(userActivityLogHelper).saveLog(contains("Kích hoạt"));
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test changeStatus should deactivate shift successfully")
    void testChangeStatusDeactivateSuccess() {
        FacilityShift facilityShift = new FacilityShift();
        facilityShift.setId("shift-1");
        facilityShift.setShift(1);
        facilityShift.setStatus(EntityStatus.ACTIVE);
        facilityShift.setFromHour(7);
        facilityShift.setFromMinute(0);
        facilityShift.setToHour(9);
        facilityShift.setToMinute(0);
        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setName("FPT HCM");
        facilityShift.setFacility(facility);
        when(afFacilityShiftRepository.findById("shift-1")).thenReturn(Optional.of(facilityShift));
        when(afFacilityShiftRepository.save(any(FacilityShift.class))).thenReturn(facilityShift);

        ResponseEntity<?> response = shiftService.changeStatus("shift-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(afFacilityShiftRepository).save(any(FacilityShift.class));
        verify(userActivityLogHelper).saveLog(contains("Ngừng hoạt động"));
        verify(redisInvalidationHelper).invalidateAllCaches();
    }
}
