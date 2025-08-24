package udpm.hn.studentattendance.core.staff.plan.services.impl;

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
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDDeletePlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanDateResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanDateGroupResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanFactoryResponse;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDFacilityShiftRepository;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDPlanDateRepository;
import udpm.hn.studentattendance.core.staff.plan.repositories.SPDPlanFactoryRepository;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SPDPlanDateServiceImplTest {

    @Mock
    private SPDPlanDateRepository planDateRepository;

    @Mock
    private SPDPlanFactoryRepository planFactoryRepository;

    @Mock
    private SPDFacilityShiftRepository facilityShiftRepository;

    @Mock
    private SessionHelper sessionHelper;

    @Mock
    private UserActivityLogHelper userActivityLogHelper;

    @InjectMocks
    private SPDPlanDateServiceImpl planDateService;

    @BeforeEach
    void setUp() {
        // Remove the following line if it exists:
        // ReflectionTestUtils.setField(spdPlanDateServiceImpl, "MAX_LATE_ARRIVAL",
        // ...);
        // ... rest of your setup code ...
    }

    @Test
    @DisplayName("getDetail should return plan factory details when exists")
    void testGetDetail_Success() {
        // Arrange
        String planFactoryId = "plan-factory-1";
        String facilityId = "facility-1";

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        SPDPlanFactoryResponse mockResponse = mock(SPDPlanFactoryResponse.class);
        when(planFactoryRepository.getDetail(planFactoryId, facilityId)).thenReturn(Optional.of(mockResponse));

        // Act
        ResponseEntity<?> response = planDateService.getDetail(planFactoryId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Get dữ liệu thành công", apiResponse.getMessage());
        assertEquals(mockResponse, apiResponse.getData());

        verify(planFactoryRepository).getDetail(planFactoryId, facilityId);
    }

    @Test
    @DisplayName("getDetail should return error when plan factory not found")
    void testGetDetail_NotFound() {
        // Arrange
        String planFactoryId = "non-existent-id";
        String facilityId = "facility-1";

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(planFactoryRepository.getDetail(planFactoryId, facilityId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = planDateService.getDetail(planFactoryId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Không tìm thấy kế hoạch", apiResponse.getMessage());
    }

    @Test
    @DisplayName("getAllList should return paginated plan date list")
    void testGetAllList() {
        // Arrange
        String facilityId = "facility-1";
        SPDFilterPlanDateRequest request = new SPDFilterPlanDateRequest();

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        Page<SPDPlanDateGroupResponse> page = new PageImpl<>(new ArrayList<>());
        when(planDateRepository.getAllGroupByFilter(any(), eq(request))).thenReturn(page);

        // Act
        ResponseEntity<?> response = planDateService.getAllList(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy danh sách dữ liệu thành công", apiResponse.getMessage());

        assertEquals(facilityId, request.getIdFacility());
        verify(planDateRepository).getAllGroupByFilter(any(), eq(request));
    }

    @Test
    @DisplayName("deletePlanDate should delete plan date when found")
    void testDeletePlanDate_Success() {
        // Arrange
        String planDateId = "plan-date-1";
        String facilityId = "facility-1";
        long startDate = System.currentTimeMillis();

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        SPDPlanDateResponse planDateResponse = mock(SPDPlanDateResponse.class);
        when(planDateResponse.getId()).thenReturn(planDateId);
        when(planDateResponse.getStartDate()).thenReturn(startDate);

        when(planDateRepository.getPlanDateById(planDateId, facilityId)).thenReturn(Optional.of(planDateResponse));
        when(planDateRepository.deletePlanDateById(eq(facilityId), anyList())).thenReturn(1);

        doNothing().when(userActivityLogHelper).saveLog(anyString());

        // Act
        ResponseEntity<?> response = planDateService.deletePlanDate(planDateId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Xoá thành công kế hoạch chi tiết.", apiResponse.getMessage());

        verify(planDateRepository).deletePlanDateById(facilityId, List.of(planDateId));
        verify(userActivityLogHelper).saveLog(anyString());
    }

    @Test
    @DisplayName("deletePlanDate should return error when plan date not found")
    void testDeletePlanDate_NotFound() {
        // Arrange
        String planDateId = "non-existent-id";
        String facilityId = "facility-1";

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(planDateRepository.getPlanDateById(planDateId, facilityId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = planDateService.deletePlanDate(planDateId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Không tìm thấy kế hoạch chi tiết", apiResponse.getMessage());

        verify(planDateRepository, never()).deletePlanDateById(anyString(), anyList());
    }

    @Test
    @DisplayName("deleteMultiplePlanDate should delete multiple plan dates")
    void testDeleteMultiplePlanDate_Success() {
        // Arrange
        String facilityId = "facility-1";
        List<String> planDateIds = Arrays.asList("plan-date-1", "plan-date-2");

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        SPDDeletePlanDateRequest request = new SPDDeletePlanDateRequest();
        request.setDays(planDateIds);

        when(planDateRepository.deletePlanDateById(facilityId, planDateIds)).thenReturn(2);
        doNothing().when(userActivityLogHelper).saveLog(anyString());

        // Act
        ResponseEntity<?> response = planDateService.deleteMultiplePlanDate(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Xoá thành công 2 kế hoạch chi tiết.", apiResponse.getMessage());

        verify(planDateRepository).deletePlanDateById(facilityId, planDateIds);
        verify(userActivityLogHelper).saveLog(contains("vừa xóa 2 kế hoạch chi tiết"));
    }

    @Test
    @DisplayName("deleteMultiplePlanDate should return error when no IDs provided")
    void testDeleteMultiplePlanDate_NoIds() {
        // Arrange
        SPDDeletePlanDateRequest request = new SPDDeletePlanDateRequest();
        request.setDays(new ArrayList<>());

        // Act
        ResponseEntity<?> response = planDateService.deleteMultiplePlanDate(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Vui lòng chọn ít nhất 1 mục muốn xoá.", apiResponse.getMessage());

        verify(planDateRepository, never()).deletePlanDateById(anyString(), anyList());
    }
}
