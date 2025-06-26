package udpm.hn.studentattendance.core.teacher.factory.service.impl;

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
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCFilterShiftFactoryRequest;
import udpm.hn.studentattendance.core.teacher.factory.model.response.TCFacilityShiftRepository;
import udpm.hn.studentattendance.core.teacher.factory.model.response.TCPlanDateResponse;
import udpm.hn.studentattendance.core.teacher.factory.model.response.TCPlanFactoryResponse;
import udpm.hn.studentattendance.core.teacher.factory.repository.TCPlanDateRepository;
import udpm.hn.studentattendance.core.teacher.factory.repository.TCPlanFactoryRepository;
import udpm.hn.studentattendance.entities.FacilityShift;
import udpm.hn.studentattendance.helpers.SessionHelper;
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
class TCShiftFactoryServiceImplTest {

    @Mock
    private TCPlanDateRepository tcPlanDateRepository;

    @Mock
    private TCPlanFactoryRepository tcPlanFactoryRepository;

    @Mock
    private TCFacilityShiftRepository tcFacilityShiftRepository;

    @Mock
    private SessionHelper sessionHelper;

    @InjectMocks
    private TCShiftFactoryServiceImpl shiftFactoryService;

    @Test
    @DisplayName("getDetail should return plan factory details when found")
    void testGetDetail_Success() {
        // Arrange
        String idFactory = "factory-1";
        String facilityId = "facility-1";
        TCPlanFactoryResponse mockResponse = mock(TCPlanFactoryResponse.class);

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(tcPlanFactoryRepository.getDetail(idFactory, facilityId)).thenReturn(Optional.of(mockResponse));

        // Act
        ResponseEntity<?> response = shiftFactoryService.getDetail(idFactory);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Get dữ liệu thành công", apiResponse.getMessage());
        assertEquals(mockResponse, apiResponse.getData());

        verify(sessionHelper).getFacilityId();
        verify(tcPlanFactoryRepository).getDetail(idFactory, facilityId);
    }

    @Test
    @DisplayName("getDetail should return error when plan factory not found")
    void testGetDetail_NotFound() {
        // Arrange
        String idFactory = "non-existent-factory";
        String facilityId = "facility-1";

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(tcPlanFactoryRepository.getDetail(idFactory, facilityId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = shiftFactoryService.getDetail(idFactory);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Không tìm thấy kế hoạch", apiResponse.getMessage());

        verify(sessionHelper).getFacilityId();
        verify(tcPlanFactoryRepository).getDetail(idFactory, facilityId);
    }

    @Test
    @DisplayName("getAllList should return paginated list of plan dates")
    void testGetAllList() {
        // Arrange
        TCFilterShiftFactoryRequest request = new TCFilterShiftFactoryRequest();
        String facilityId = "facility-1";
        Page<TCPlanDateResponse> mockPage = new PageImpl<>(Arrays.asList(
                mock(TCPlanDateResponse.class),
                mock(TCPlanDateResponse.class)));

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(tcPlanDateRepository.getAllByFilter(any(Pageable.class), eq(request))).thenReturn(mockPage);

        // Act
        ResponseEntity<?> response = shiftFactoryService.getAllList(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy danh sách dữ liệu thành công", apiResponse.getMessage());
        assertTrue(apiResponse.getData() instanceof PageableObject);

        verify(sessionHelper).getFacilityId();
        verify(tcPlanDateRepository).getAllByFilter(any(Pageable.class), eq(request));
        assertEquals(facilityId, request.getIdFacility());
    }

    @Test
    @DisplayName("getListShift should return list of facility shifts")
    void testGetListShift() {
        // Arrange
        String facilityId = "facility-1";
        List<FacilityShift> mockShifts = Arrays.asList(
                mock(FacilityShift.class),
                mock(FacilityShift.class));

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(tcFacilityShiftRepository.getAllList(facilityId)).thenReturn(mockShifts);

        // Act
        ResponseEntity<?> response = shiftFactoryService.getListShift();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy danh sách dữ liệu thành công", apiResponse.getMessage());
        assertEquals(mockShifts, apiResponse.getData());

        verify(sessionHelper).getFacilityId();
        verify(tcFacilityShiftRepository).getAllList(facilityId);
    }
}