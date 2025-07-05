package udpm.hn.studentattendance.core.teacher.factory.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCFilterPlanDateAttendanceRequest;
import udpm.hn.studentattendance.core.teacher.factory.model.response.TCPlanDateAttendanceResponse;
import udpm.hn.studentattendance.core.teacher.factory.model.response.TCPlanDateStudentResponse;
import udpm.hn.studentattendance.core.teacher.factory.repository.TCAttendanceRepository;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TCPlanDateAttendanceServiceImplTest {

    @Mock
    private TCAttendanceRepository tcAttendanceRepository;

    @Mock
    private SessionHelper sessionHelper;
    
    @Mock
    private RedisService redisService;
    
    @Mock
    private RedisInvalidationHelper redisInvalidationHelper;

    @InjectMocks
    private TCPlanDateAttendanceServiceImpl planDateAttendanceService;
    
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(planDateAttendanceService, "redisTTL", 3600L);
    }

    @Test
    @DisplayName("getDetail should return plan date attendance when found")
    void testGetDetail_Success() {
        // Arrange
        String idPlanDate = "plan-date-1";
        String facilityId = "facility-1";
        TCPlanDateAttendanceResponse mockResponse = mock(TCPlanDateAttendanceResponse.class);

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(tcAttendanceRepository.getDetailPlanDate(idPlanDate, facilityId)).thenReturn(Optional.of(mockResponse));

        // Act
        ResponseEntity<?> response = planDateAttendanceService.getDetail(idPlanDate);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Get dữ liệu thành công", apiResponse.getMessage());
        assertEquals(mockResponse, apiResponse.getData());

        verify(sessionHelper).getFacilityId();
        verify(tcAttendanceRepository).getDetailPlanDate(idPlanDate, facilityId);
    }

    @Test
    @DisplayName("getDetail should return error when plan date attendance not found")
    void testGetDetail_NotFound() {
        // Arrange
        String idPlanDate = "non-existent-plan";
        String facilityId = "facility-1";

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(tcAttendanceRepository.getDetailPlanDate(idPlanDate, facilityId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = planDateAttendanceService.getDetail(idPlanDate);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.ERROR, apiResponse.getStatus());
        assertEquals("Không tìm thấy kế hoạch", apiResponse.getMessage());

        verify(sessionHelper).getFacilityId();
        verify(tcAttendanceRepository).getDetailPlanDate(idPlanDate, facilityId);
    }

    @Test
    @DisplayName("getAllList should return paginated list of plan date students")
    void testGetAllList() {
        // Arrange
        TCFilterPlanDateAttendanceRequest request = new TCFilterPlanDateAttendanceRequest();
        String facilityId = "facility-1";
        Page<TCPlanDateStudentResponse> mockPage = new PageImpl<>(Arrays.asList(
                mock(TCPlanDateStudentResponse.class),
                mock(TCPlanDateStudentResponse.class)));

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(tcAttendanceRepository.getAllByFilter(any(Pageable.class), eq(request))).thenReturn(mockPage);

        // Act
        ResponseEntity<?> response = planDateAttendanceService.getAllList(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy danh sách dữ liệu thành công", apiResponse.getMessage());
        assertTrue(apiResponse.getData() instanceof PageableObject);

        verify(sessionHelper).getFacilityId();
        verify(tcAttendanceRepository).getAllByFilter(any(Pageable.class), eq(request));
        assertEquals(facilityId, request.getIdFacility());
    }
}