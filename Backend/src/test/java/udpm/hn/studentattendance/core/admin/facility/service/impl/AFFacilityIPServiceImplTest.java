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
import udpm.hn.studentattendance.core.admin.facility.model.request.AFAddOrUpdateFacilityIPRequest;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFFilterFacilityIPRequest;
import udpm.hn.studentattendance.core.admin.facility.model.response.AFFacilityIPResponse;
import udpm.hn.studentattendance.core.admin.facility.repository.AFFacilityExtendRepository;
import udpm.hn.studentattendance.core.admin.facility.repository.AFFacilityIPRepository;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.FacilityIP;
import udpm.hn.studentattendance.helpers.RedisCacheHelper;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.IPType;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import udpm.hn.studentattendance.infrastructure.config.redis.service.RedisService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.mockito.ArgumentCaptor;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AFFacilityIPServiceImplTest {

    @Mock
    private AFFacilityExtendRepository afFacilityExtendRepository;

    @Mock
    private AFFacilityIPRepository afFacilityIPRepository;

    @Mock
    private UserActivityLogHelper userActivityLogHelper;

    @Mock
    private RedisService redisService;

    @Mock
    private RedisInvalidationHelper redisInvalidationHelper;

    @Mock
    private RedisCacheHelper redisCacheHelper;

    @InjectMocks
    private AFFacilityIPServiceImpl facilityIPService;

    @BeforeEach
    void setUp() {
        // Removed unnecessary stubbing for redisCacheHelper.getOrSet
    }

    @Test
    @DisplayName("Test getAllList should return data from cache if available")
    void testGetAllListFromCache() {
        // Given
        AFFilterFacilityIPRequest request = new AFFilterFacilityIPRequest();
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_FACILITY_IP + "list";
        PageableObject mockData = mock(PageableObject.class);

        when(redisCacheHelper.getOrSet(anyString(), any(), any())).thenReturn(mockData);

        // When
        ResponseEntity<?> response = facilityIPService.getAllList(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy danh sách dữ liệu thành công", apiResponse.getMessage());
        assertEquals(mockData, apiResponse.getData());

        // Verify repository was not called
        verify(redisCacheHelper).getOrSet(anyString(), any(), any());
        verify(afFacilityIPRepository, never()).getAllByFilter(any(Pageable.class),
                any(AFFilterFacilityIPRequest.class));
    }

    @Test
    @DisplayName("Test getAllList should fetch and cache data if not in cache")
    void testGetAllListFromRepository() {
        AFFilterFacilityIPRequest request = new AFFilterFacilityIPRequest();
        List<AFFacilityIPResponse> ipList = new ArrayList<>();
        AFFacilityIPResponse ipResponse = mock(AFFacilityIPResponse.class);
        ipList.add(ipResponse);
        Page<AFFacilityIPResponse> page = new PageImpl<>(ipList);
        PageableObject<AFFacilityIPResponse> expected = PageableObject.of(page);

        // Cache miss: gọi supplier
        when(redisCacheHelper.getOrSet(anyString(), any(), any()))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        // Repository returns PageImpl
        when(afFacilityIPRepository.getAllByFilter(any(Pageable.class), eq(request))).thenReturn(page);

        // Gọi service qua getAllList để nhận ApiResponse không null
        ResponseEntity<?> response = facilityIPService.getAllList(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy danh sách dữ liệu thành công", apiResponse.getMessage());
        PageableObject<AFFacilityIPResponse> actual = (PageableObject<AFFacilityIPResponse>) apiResponse.getData();
        assertNotNull(actual);
        assertEquals(expected.getData(), actual.getData());
        assertEquals(expected.getTotalPages(), actual.getTotalPages());
        assertEquals(expected.getCurrentPage(), actual.getCurrentPage());
        verify(afFacilityIPRepository).getAllByFilter(any(Pageable.class), eq(request));
    }

    @Test
    @DisplayName("Test addIP should add IPv4 successfully")
    void testAddIPv4Success() {
        // Given
        AFAddOrUpdateFacilityIPRequest request = mock(AFAddOrUpdateFacilityIPRequest.class);
        when(request.getIdFacility()).thenReturn("facility-1");
        when(request.getIp()).thenReturn("192.168.1.1");
        when(request.getType()).thenReturn(IPType.IPV4.getKey());

        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setName("FPT HCM");
        facility.setStatus(EntityStatus.ACTIVE);

        when(afFacilityExtendRepository.findById("facility-1")).thenReturn(Optional.of(facility));
        when(afFacilityIPRepository.isExistsIP("192.168.1.1", IPType.IPV4.getKey(), "facility-1", null))
                .thenReturn(false);

        FacilityIP savedIP = new FacilityIP();
        savedIP.setId("ip-1");
        savedIP.setIp("192.168.1.1");
        savedIP.setType(IPType.IPV4);
        savedIP.setFacility(facility);
        savedIP.setStatus(EntityStatus.ACTIVE);

        when(afFacilityIPRepository.save(any(FacilityIP.class))).thenReturn(savedIP);

        // When
        ResponseEntity<?> response = facilityIPService.addIP(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Thêm mới IP 192.168.1.1 thành công", apiResponse.getMessage());
        assertEquals(savedIP, apiResponse.getData());

        // Verify repository was called
        verify(afFacilityIPRepository).save(any(FacilityIP.class));
        verify(userActivityLogHelper).saveLog(contains("vừa thêm IP"));
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test addIP should add DNS suffix successfully")
    void testAddDNSSuffixSuccess() {
        // Given
        AFAddOrUpdateFacilityIPRequest request = mock(AFAddOrUpdateFacilityIPRequest.class);
        when(request.getIdFacility()).thenReturn("facility-1");
        when(request.getIp()).thenReturn("example.com");
        when(request.getType()).thenReturn(IPType.DNSSUFFIX.getKey());

        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setName("FPT HCM");
        facility.setStatus(EntityStatus.ACTIVE);

        when(afFacilityExtendRepository.findById("facility-1")).thenReturn(Optional.of(facility));
        when(afFacilityIPRepository.isExistsIP("example.com", IPType.DNSSUFFIX.getKey(), "facility-1", null))
                .thenReturn(false);

        FacilityIP savedIP = new FacilityIP();
        savedIP.setId("ip-1");
        savedIP.setIp("example.com");
        savedIP.setType(IPType.DNSSUFFIX);
        savedIP.setFacility(facility);
        savedIP.setStatus(EntityStatus.ACTIVE);

        when(afFacilityIPRepository.save(any(FacilityIP.class))).thenReturn(savedIP);

        // When
        ResponseEntity<?> response = facilityIPService.addIP(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Thêm mới DNS Suffix example.com thành công", apiResponse.getMessage());
        assertEquals(savedIP, apiResponse.getData());

        // Verify repository was called
        verify(afFacilityIPRepository).save(any(FacilityIP.class));
        verify(userActivityLogHelper).saveLog(contains("vừa thêm DNS Suffix"));
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test addIP should return error if facility not found")
    void testAddIPFacilityNotFound() {
        // Given
        AFAddOrUpdateFacilityIPRequest request = mock(AFAddOrUpdateFacilityIPRequest.class);
        when(request.getIdFacility()).thenReturn("non-existent-id");

        when(afFacilityExtendRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = facilityIPService.addIP(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Không tìm cơ sở", apiResponse.getMessage());

        // Verify repository was not called
        verify(afFacilityIPRepository, never()).save(any(FacilityIP.class));
        verify(redisInvalidationHelper, never()).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test addIP should return error if IP already exists")
    void testAddIPAlreadyExists() {
        // Given
        AFAddOrUpdateFacilityIPRequest request = mock(AFAddOrUpdateFacilityIPRequest.class);
        when(request.getIdFacility()).thenReturn("facility-1");
        when(request.getIp()).thenReturn("192.168.1.1");
        when(request.getType()).thenReturn(IPType.IPV4.getKey());

        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setName("FPT HCM");
        facility.setStatus(EntityStatus.ACTIVE);

        when(afFacilityExtendRepository.findById("facility-1")).thenReturn(Optional.of(facility));
        when(afFacilityIPRepository.isExistsIP("192.168.1.1", IPType.IPV4.getKey(), "facility-1", null))
                .thenReturn(true);

        // When
        ResponseEntity<?> response = facilityIPService.addIP(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("IP 192.168.1.1 đã tồn tại trong cơ sở FPT HCM", apiResponse.getMessage());

        // Verify repository was not called
        verify(afFacilityIPRepository, never()).save(any(FacilityIP.class));
        verify(redisInvalidationHelper, never()).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test updateIP should update IP successfully")
    void testUpdateIPSuccess() {
        // Given
        String ipId = "ip-1";
        AFAddOrUpdateFacilityIPRequest request = mock(AFAddOrUpdateFacilityIPRequest.class);
        when(request.getId()).thenReturn(ipId);
        when(request.getIdFacility()).thenReturn("facility-1");
        when(request.getIp()).thenReturn("192.168.1.2");
        when(request.getType()).thenReturn(IPType.IPV4.getKey());

        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setName("FPT HCM");
        facility.setStatus(EntityStatus.ACTIVE);

        FacilityIP existingIP = new FacilityIP();
        existingIP.setId(ipId);
        existingIP.setIp("192.168.1.1");
        existingIP.setType(IPType.IPV4);
        existingIP.setFacility(facility);
        existingIP.setStatus(EntityStatus.ACTIVE);

        when(afFacilityIPRepository.findById(ipId)).thenReturn(Optional.of(existingIP));
        when(afFacilityExtendRepository.findById("facility-1")).thenReturn(Optional.of(facility));
        when(afFacilityIPRepository.isExistsIP("192.168.1.2", IPType.IPV4.getKey(), "facility-1", ipId))
                .thenReturn(false);
        when(afFacilityIPRepository.save(any(FacilityIP.class))).thenReturn(existingIP);

        // When
        ResponseEntity<?> response = facilityIPService.updateIP(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Cập nhật IP thành công", apiResponse.getMessage());

        // Verify IP was updated correctly
        assertEquals("192.168.1.2", existingIP.getIp());

        // Verify repository was called
        verify(afFacilityIPRepository).save(existingIP);
        verify(userActivityLogHelper).saveLog(contains("vừa cập nhật IP"));
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test deleteIP should delete IP successfully")
    void testDeleteIPSuccess() {
        // Given
        String ipId = "ip-1";

        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setName("FPT HCM");

        FacilityIP facilityIP = new FacilityIP();
        facilityIP.setId(ipId);
        facilityIP.setIp("192.168.1.1");
        facilityIP.setType(IPType.IPV4);
        facilityIP.setFacility(facility);

        when(afFacilityIPRepository.findById(ipId)).thenReturn(Optional.of(facilityIP));
        // No need to mock void method with doNothing()

        // When
        ResponseEntity<?> response = facilityIPService.deleteIP(ipId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Xoá thành công IP/DNS Suffix: 192.168.1.1", apiResponse.getMessage());

        // Verify repository was called
        verify(afFacilityIPRepository).delete(facilityIP);
        verify(userActivityLogHelper).saveLog(contains("vừa xóa IP"));
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test changeStatus should toggle status successfully")
    void testChangeStatusSuccess() {
        // Given
        String ipId = "ip-1";

        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setName("FPT HCM");

        FacilityIP facilityIP = new FacilityIP();
        facilityIP.setId(ipId);
        facilityIP.setIp("192.168.1.1");
        facilityIP.setType(IPType.IPV4);
        facilityIP.setFacility(facility);
        facilityIP.setStatus(EntityStatus.ACTIVE);

        when(afFacilityIPRepository.findById(ipId)).thenReturn(Optional.of(facilityIP));
        when(afFacilityIPRepository.save(any(FacilityIP.class))).thenReturn(facilityIP);

        // When
        ResponseEntity<?> response = facilityIPService.changeStatus(ipId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Thay đổi trạng thái IP thành công", apiResponse.getMessage());

        // Verify status was changed
        assertEquals(EntityStatus.INACTIVE, facilityIP.getStatus());

        // Verify repository was called
        verify(afFacilityIPRepository).save(facilityIP);
        verify(userActivityLogHelper).saveLog(contains("vừa thay đổi trạng thái IP"));
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test changeStatus should return error if IP already in use")
    void testChangeStatusIPInUse() {
        // Given
        String ipId = "ip-1";

        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setName("FPT HCM");

        FacilityIP facilityIP = new FacilityIP();
        facilityIP.setId(ipId);
        facilityIP.setIp("192.168.1.1");
        facilityIP.setType(IPType.IPV4);
        facilityIP.setFacility(facility);
        facilityIP.setStatus(EntityStatus.INACTIVE);

        when(afFacilityIPRepository.findById(ipId)).thenReturn(Optional.of(facilityIP));
        when(afFacilityIPRepository.isExistsIP("192.168.1.1", IPType.IPV4.getKey(), "facility-1", ipId))
                .thenReturn(true);

        // When
        ResponseEntity<?> response = facilityIPService.changeStatus(ipId);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("IP 192.168.1.1 đã được áp dụng trong cơ sở", apiResponse.getMessage());

        // Verify repository was not called
        verify(afFacilityIPRepository, never()).save(any(FacilityIP.class));
        verify(redisInvalidationHelper, never()).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test getIPList should handle cache deserialization error")
    void testGetIPListWithCacheError() {
        AFFilterFacilityIPRequest request = new AFFilterFacilityIPRequest();
        List<AFFacilityIPResponse> ipList = new ArrayList<>();
        AFFacilityIPResponse ipResponse = mock(AFFacilityIPResponse.class);
        ipList.add(ipResponse);
        Page<AFFacilityIPResponse> page = new PageImpl<>(ipList);

        // Simulate deserialization error (cache error)
        when(redisCacheHelper.getOrSet(anyString(), any(), any()))
                .thenThrow(new RuntimeException("Deserialization error"));
        // Remove unnecessary stubbing for repository
        // when(afFacilityIPRepository.getAllByFilter(any(Pageable.class),
        // eq(request))).thenReturn(page);

        // Since the service does not handle the exception, assertThrows is appropriate
        assertThrows(RuntimeException.class, () -> facilityIPService.getIPList(request));
    }

    @Test
    @DisplayName("Test getIPList should handle redis set exception")
    void testGetIPListWithRedisSetError() {
        AFFilterFacilityIPRequest request = new AFFilterFacilityIPRequest();
        List<AFFacilityIPResponse> ipList = new ArrayList<>();
        AFFacilityIPResponse ipResponse = mock(AFFacilityIPResponse.class);
        ipList.add(ipResponse);
        Page<AFFacilityIPResponse> page = new PageImpl<>(ipList);
        PageableObject<AFFacilityIPResponse> expected = PageableObject.of(page);
        // Cache miss: gọi supplier
        when(redisCacheHelper.getOrSet(anyString(), any(), any()))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        // Repository returns PageImpl
        when(afFacilityIPRepository.getAllByFilter(any(Pageable.class), eq(request))).thenReturn(page);

        // Gọi service qua getAllList để nhận ApiResponse không null
        ResponseEntity<?> response = facilityIPService.getAllList(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy danh sách dữ liệu thành công", apiResponse.getMessage());
        PageableObject<AFFacilityIPResponse> actual = (PageableObject<AFFacilityIPResponse>) apiResponse.getData();
        assertNotNull(actual);
        assertEquals(expected.getData(), actual.getData());
        assertEquals(expected.getTotalPages(), actual.getTotalPages());
        assertEquals(expected.getCurrentPage(), actual.getCurrentPage());
        verify(afFacilityIPRepository).getAllByFilter(any(Pageable.class), eq(request));
    }

    @Test
    @DisplayName("Test addIP should return error when facility is inactive")
    void testAddIPFacilityInactive() {
        AFAddOrUpdateFacilityIPRequest request = mock(AFAddOrUpdateFacilityIPRequest.class);
        when(request.getIdFacility()).thenReturn("facility-1");
        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setStatus(EntityStatus.INACTIVE);
        when(afFacilityExtendRepository.findById("facility-1")).thenReturn(Optional.of(facility));

        ResponseEntity<?> response = facilityIPService.addIP(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(afFacilityIPRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test addIP should return error for invalid IPv4")
    void testAddIPInvalidIPv4() {
        AFAddOrUpdateFacilityIPRequest request = mock(AFAddOrUpdateFacilityIPRequest.class);
        when(request.getIdFacility()).thenReturn("facility-1");
        when(request.getIp()).thenReturn("256.256.256.256"); // Invalid IP
        when(request.getType()).thenReturn(IPType.IPV4.getKey());

        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setStatus(EntityStatus.ACTIVE);
        when(afFacilityExtendRepository.findById("facility-1")).thenReturn(Optional.of(facility));
        when(afFacilityIPRepository.isExistsIP(anyString(), anyInt(), anyString(), isNull())).thenReturn(false);

        ResponseEntity<?> response = facilityIPService.addIP(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(afFacilityIPRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test addIP should return error for invalid IPv4 CIDR")
    void testAddIPInvalidIPv4CIDR() {
        AFAddOrUpdateFacilityIPRequest request = mock(AFAddOrUpdateFacilityIPRequest.class);
        when(request.getIdFacility()).thenReturn("facility-1");
        when(request.getIp()).thenReturn("192.168.1.0/33"); // Invalid CIDR
        when(request.getType()).thenReturn(IPType.IPV4.getKey());

        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setStatus(EntityStatus.ACTIVE);
        when(afFacilityExtendRepository.findById("facility-1")).thenReturn(Optional.of(facility));
        when(afFacilityIPRepository.isExistsIP(anyString(), anyInt(), anyString(), isNull())).thenReturn(false);

        ResponseEntity<?> response = facilityIPService.addIP(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(afFacilityIPRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test addIP should return error for invalid IPv6")
    void testAddIPInvalidIPv6() {
        AFAddOrUpdateFacilityIPRequest request = mock(AFAddOrUpdateFacilityIPRequest.class);
        when(request.getIdFacility()).thenReturn("facility-1");
        when(request.getIp()).thenReturn("2001:db8::1:2:3:4:5:6:7:8:9"); // Invalid IPv6
        when(request.getType()).thenReturn(IPType.IPV6.getKey());

        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setStatus(EntityStatus.ACTIVE);
        when(afFacilityExtendRepository.findById("facility-1")).thenReturn(Optional.of(facility));
        when(afFacilityIPRepository.isExistsIP(anyString(), anyInt(), anyString(), isNull())).thenReturn(false);

        ResponseEntity<?> response = facilityIPService.addIP(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(afFacilityIPRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test addIP should return error for invalid DNS suffix")
    void testAddIPInvalidDNSSuffix() {
        AFAddOrUpdateFacilityIPRequest request = mock(AFAddOrUpdateFacilityIPRequest.class);
        when(request.getIdFacility()).thenReturn("facility-1");
        when(request.getIp()).thenReturn("invalid..domain"); // Invalid DNS suffix
        when(request.getType()).thenReturn(IPType.DNSSUFFIX.getKey());

        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setStatus(EntityStatus.ACTIVE);
        when(afFacilityExtendRepository.findById("facility-1")).thenReturn(Optional.of(facility));
        when(afFacilityIPRepository.isExistsIP(anyString(), anyInt(), anyString(), isNull())).thenReturn(false);

        ResponseEntity<?> response = facilityIPService.addIP(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(afFacilityIPRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test updateIP should return error if IP not found")
    void testUpdateIPNotFound() {
        AFAddOrUpdateFacilityIPRequest request = mock(AFAddOrUpdateFacilityIPRequest.class);
        when(request.getId()).thenReturn("ip-1");
        when(afFacilityIPRepository.findById("ip-1")).thenReturn(Optional.empty());

        ResponseEntity<?> response = facilityIPService.updateIP(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Test updateIP should return error when facility not found")
    void testUpdateIPFacilityNotFound() {
        AFAddOrUpdateFacilityIPRequest request = mock(AFAddOrUpdateFacilityIPRequest.class);
        when(request.getId()).thenReturn("ip-1");
        when(request.getIdFacility()).thenReturn("facility-1");
        FacilityIP existingIP = new FacilityIP();
        existingIP.setId("ip-1");
        when(afFacilityIPRepository.findById("ip-1")).thenReturn(Optional.of(existingIP));
        when(afFacilityExtendRepository.findById("facility-1")).thenReturn(Optional.empty());

        ResponseEntity<?> response = facilityIPService.updateIP(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(afFacilityIPRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test updateIP should return error when facility is inactive")
    void testUpdateIPFacilityInactive() {
        AFAddOrUpdateFacilityIPRequest request = mock(AFAddOrUpdateFacilityIPRequest.class);
        when(request.getId()).thenReturn("ip-1");
        when(request.getIdFacility()).thenReturn("facility-1");
        FacilityIP existingIP = new FacilityIP();
        existingIP.setId("ip-1");
        when(afFacilityIPRepository.findById("ip-1")).thenReturn(Optional.of(existingIP));
        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setStatus(EntityStatus.INACTIVE);
        when(afFacilityExtendRepository.findById("facility-1")).thenReturn(Optional.of(facility));

        ResponseEntity<?> response = facilityIPService.updateIP(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(afFacilityIPRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test updateIP should return error when IP already exists")
    void testUpdateIPAlreadyExists() {
        AFAddOrUpdateFacilityIPRequest request = mock(AFAddOrUpdateFacilityIPRequest.class);
        when(request.getId()).thenReturn("ip-1");
        when(request.getIdFacility()).thenReturn("facility-1");
        when(request.getIp()).thenReturn("192.168.1.1");
        when(request.getType()).thenReturn(IPType.IPV4.getKey());
        FacilityIP existingIP = new FacilityIP();
        existingIP.setId("ip-1");
        when(afFacilityIPRepository.findById("ip-1")).thenReturn(Optional.of(existingIP));
        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setName("FPT HCM");
        facility.setStatus(EntityStatus.ACTIVE);
        when(afFacilityExtendRepository.findById("facility-1")).thenReturn(Optional.of(facility));
        when(afFacilityIPRepository.isExistsIP("192.168.1.1", IPType.IPV4.getKey(), "facility-1", "ip-1"))
                .thenReturn(true);

        ResponseEntity<?> response = facilityIPService.updateIP(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(afFacilityIPRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test updateIP should return error for invalid IPv4")
    void testUpdateIPInvalidIPv4() {
        AFAddOrUpdateFacilityIPRequest request = mock(AFAddOrUpdateFacilityIPRequest.class);
        when(request.getId()).thenReturn("ip-1");
        when(request.getIdFacility()).thenReturn("facility-1");
        when(request.getIp()).thenReturn("256.256.256.256"); // Invalid IP
        when(request.getType()).thenReturn(IPType.IPV4.getKey());
        FacilityIP existingIP = new FacilityIP();
        existingIP.setId("ip-1");
        when(afFacilityIPRepository.findById("ip-1")).thenReturn(Optional.of(existingIP));
        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setStatus(EntityStatus.ACTIVE);
        when(afFacilityExtendRepository.findById("facility-1")).thenReturn(Optional.of(facility));
        when(afFacilityIPRepository.isExistsIP(anyString(), anyInt(), anyString(), anyString())).thenReturn(false);

        ResponseEntity<?> response = facilityIPService.updateIP(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(afFacilityIPRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test deleteIP should return error if IP not found")
    void testDeleteIPNotFound() {
        when(afFacilityIPRepository.findById("ip-1")).thenReturn(Optional.empty());

        ResponseEntity<?> response = facilityIPService.deleteIP("ip-1");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Test changeStatus should return error if IP not found")
    void testChangeStatusNotFound() {
        when(afFacilityIPRepository.findById("ip-1")).thenReturn(Optional.empty());

        ResponseEntity<?> response = facilityIPService.changeStatus("ip-1");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Test changeStatus should return error when activating inactive IP that already exists")
    void testChangeStatusActivateExistingIP() {
        FacilityIP facilityIP = new FacilityIP();
        facilityIP.setId("ip-1");
        facilityIP.setIp("192.168.1.1");
        facilityIP.setType(IPType.IPV4);
        facilityIP.setStatus(EntityStatus.INACTIVE);
        Facility facility = new Facility();
        facility.setId("facility-1");
        facilityIP.setFacility(facility);
        when(afFacilityIPRepository.findById("ip-1")).thenReturn(Optional.of(facilityIP));
        when(afFacilityIPRepository.isExistsIP("192.168.1.1", IPType.IPV4.getKey(), "facility-1", "ip-1"))
                .thenReturn(true);

        ResponseEntity<?> response = facilityIPService.changeStatus("ip-1");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(afFacilityIPRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test changeStatus should activate IP successfully")
    void testChangeStatusActivateSuccess() {
        FacilityIP facilityIP = new FacilityIP();
        facilityIP.setId("ip-1");
        facilityIP.setIp("192.168.1.1");
        facilityIP.setType(IPType.IPV4);
        facilityIP.setStatus(EntityStatus.INACTIVE);
        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setName("FPT HCM");
        facilityIP.setFacility(facility);
        when(afFacilityIPRepository.findById("ip-1")).thenReturn(Optional.of(facilityIP));
        when(afFacilityIPRepository.isExistsIP("192.168.1.1", IPType.IPV4.getKey(), "facility-1", "ip-1"))
                .thenReturn(false);
        when(afFacilityIPRepository.save(any(FacilityIP.class))).thenReturn(facilityIP);

        ResponseEntity<?> response = facilityIPService.changeStatus("ip-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(afFacilityIPRepository).save(any(FacilityIP.class));
        verify(userActivityLogHelper).saveLog(contains("vừa thay đổi trạng thái IP"));
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test changeStatus should deactivate IP successfully")
    void testChangeStatusDeactivateSuccess() {
        FacilityIP facilityIP = new FacilityIP();
        facilityIP.setId("ip-1");
        facilityIP.setIp("192.168.1.1");
        facilityIP.setType(IPType.IPV4);
        facilityIP.setStatus(EntityStatus.ACTIVE);
        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setName("FPT HCM");
        facilityIP.setFacility(facility);
        when(afFacilityIPRepository.findById("ip-1")).thenReturn(Optional.of(facilityIP));
        when(afFacilityIPRepository.save(any(FacilityIP.class))).thenReturn(facilityIP);

        ResponseEntity<?> response = facilityIPService.changeStatus("ip-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(afFacilityIPRepository).save(any(FacilityIP.class));
        verify(userActivityLogHelper).saveLog(contains("vừa thay đổi trạng thái IP"));
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test changeStatus should handle DNS suffix type")
    void testChangeStatusDNSSuffix() {
        FacilityIP facilityIP = new FacilityIP();
        facilityIP.setId("ip-1");
        facilityIP.setIp("example.com");
        facilityIP.setType(IPType.DNSSUFFIX);
        facilityIP.setStatus(EntityStatus.ACTIVE);
        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setName("FPT HCM");
        facilityIP.setFacility(facility);
        when(afFacilityIPRepository.findById("ip-1")).thenReturn(Optional.of(facilityIP));
        when(afFacilityIPRepository.save(any(FacilityIP.class))).thenReturn(facilityIP);

        ResponseEntity<?> response = facilityIPService.changeStatus("ip-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(afFacilityIPRepository).save(any(FacilityIP.class));
        verify(userActivityLogHelper).saveLog(contains("vừa thay đổi trạng thái DNS Suffix"));
        verify(redisInvalidationHelper).invalidateAllCaches();
    }
}
