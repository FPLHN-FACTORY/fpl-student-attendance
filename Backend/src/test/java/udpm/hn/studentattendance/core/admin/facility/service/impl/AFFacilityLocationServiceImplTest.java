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
import udpm.hn.studentattendance.core.admin.facility.model.request.AFAddOrUpdateFacilityLocationRequest;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFFilterFacilityLocationRequest;
import udpm.hn.studentattendance.core.admin.facility.model.response.AFFacilityLocationResponse;
import udpm.hn.studentattendance.core.admin.facility.repository.AFFacilityExtendRepository;
import udpm.hn.studentattendance.core.admin.facility.repository.AFFacilityLocationRepository;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.FacilityLocation;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AFFacilityLocationServiceImplTest {

    @Mock
    private AFFacilityLocationRepository facilityLocationRepository;

    @Mock
    private AFFacilityExtendRepository facilityRepository;

    @Mock
    private UserActivityLogHelper userActivityLogHelper;

    @Mock
    private RedisService redisService;
    
    @Mock
    private RedisInvalidationHelper redisInvalidationHelper;

    @InjectMocks
    private AFFacilityLocationServiceImpl facilityLocationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(facilityLocationService, "redisTTL", 3600L);
    }

    @Test
    @DisplayName("Test getAllList should return data from cache if available")
    void testGetAllListFromCache() {
        // Given
        AFFilterFacilityLocationRequest request = new AFFilterFacilityLocationRequest();
        PageableObject mockData = mock(PageableObject.class);

        when(redisService.get(anyString())).thenReturn(mockData);
        when(redisService.getObject(anyString(), eq(PageableObject.class))).thenReturn(mockData);

        // When
        ResponseEntity<?> response = facilityLocationService.getAllList(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy danh sách dữ liệu thành công", apiResponse.getMessage());
        assertEquals(mockData, apiResponse.getData());

        // Verify repository was not called
        verify(facilityLocationRepository, never()).getAllByFilter(any(Pageable.class),
                any(AFFilterFacilityLocationRequest.class));
    }

    @Test
    @DisplayName("Test getAllList should fetch and cache data if not in cache")
    void testGetAllListFromRepository() {
        // Given
        AFFilterFacilityLocationRequest request = new AFFilterFacilityLocationRequest();

        List<AFFacilityLocationResponse> locations = new ArrayList<>();
        AFFacilityLocationResponse location = mock(AFFacilityLocationResponse.class);
        locations.add(location);
        Page<AFFacilityLocationResponse> page = new PageImpl<>(locations);

        when(redisService.get(anyString())).thenReturn(null);
        when(facilityLocationRepository.getAllByFilter(any(Pageable.class), eq(request))).thenReturn(page);

        // When
        ResponseEntity<?> response = facilityLocationService.getAllList(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy danh sách dữ liệu thành công", apiResponse.getMessage());

        // Verify repository was called and cache was updated
        verify(facilityLocationRepository).getAllByFilter(any(Pageable.class), eq(request));
        verify(redisService).set(anyString(), any(PageableObject.class), eq(3600L));
    }

    @Test
    @DisplayName("Test addLocation should add location successfully")
    void testAddLocationSuccess() {
        // Given
        String facilityId = "facility-1";
        String locationName = "FPT Building";
        Double latitude = 21.028511;
        Double longitude = 105.804817;
        Integer radius = 100;

        AFAddOrUpdateFacilityLocationRequest request = new AFAddOrUpdateFacilityLocationRequest();
        request.setIdFacility(facilityId);
        request.setName(locationName);
        request.setLatitude(latitude);
        request.setLongitude(longitude);
        request.setRadius(radius);

        Facility facility = new Facility();
        facility.setId(facilityId);
        facility.setName("FPT HCM");
        facility.setStatus(EntityStatus.ACTIVE);

        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));
        when(facilityLocationRepository.isExistsLocation(eq(locationName), eq(facilityId), isNull())).thenReturn(false);

        FacilityLocation savedLocation = new FacilityLocation();
        savedLocation.setId("location-1");
        savedLocation.setName(locationName);
        savedLocation.setLatitude(latitude);
        savedLocation.setLongitude(longitude);
        savedLocation.setRadius(radius);
        savedLocation.setFacility(facility);
        savedLocation.setStatus(EntityStatus.ACTIVE);

        when(facilityLocationRepository.save(any(FacilityLocation.class))).thenReturn(savedLocation);

        // When
        ResponseEntity<?> response = facilityLocationService.addLocation(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Tạo mới địa điểm thành công", apiResponse.getMessage());
        assertEquals(savedLocation, apiResponse.getData());

        // Verify repository was called
        verify(facilityLocationRepository).save(any(FacilityLocation.class));
        verify(userActivityLogHelper).saveLog(contains("vừa thêm địa điểm mới"));
        verify(redisInvalidationHelper).invalidateAllCaches();
    }
}
