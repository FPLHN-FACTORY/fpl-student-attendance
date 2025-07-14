package udpm.hn.studentattendance.core.staff.useractivity.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.useractivity.repository.STUserActivityFilterExtendRepository;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.model.request.UALFilterRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class STUserActivityServiceImplTest {

    @Mock
    private UserActivityLogHelper userActivityLogHelper;

    @Mock
    private STUserActivityFilterExtendRepository userActivityFilterExtendRepository;

    @Mock
    private SessionHelper sessionHelper;

    @InjectMocks
    private STUserActivityServiceImpl userActivityService;

    @Test
    @DisplayName("getAllUserActivity should return paginated user activity logs")
    void testGetAllUserActivity() {
        // Arrange
        String facilityId = "facility-1";
        UALFilterRequest request = new UALFilterRequest();

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        PageableObject mockResponse = new PageableObject();
        when(userActivityLogHelper.getAll(any(UALFilterRequest.class))).thenReturn(mockResponse);

        // Act
        ResponseEntity<?> response = userActivityService.getAllUserActivity(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy danh sách hành động của người dùng thành công", apiResponse.getMessage());
        assertEquals(mockResponse, apiResponse.getData());

        // Verify facility ID was set and repository method was called
        assertEquals(facilityId, request.getFacilityId());
        verify(userActivityLogHelper).getAll(request);
    }

    @Test
    @DisplayName("getAllUserStaff should return list of staff users")
    void testGetAllUserStaff() {
        // Arrange
        String facilityId = "facility-1";
        when(sessionHelper.getFacilityId()).thenReturn(facilityId);

        List<UserStaff> mockStaffList = Arrays.asList(
                mock(UserStaff.class),
                mock(UserStaff.class));

        when(userActivityFilterExtendRepository.getAllUserStaff(
                eq(EntityStatus.ACTIVE),
                eq(EntityStatus.ACTIVE),
                eq(EntityStatus.ACTIVE),
                eq(facilityId)))
                .thenReturn(mockStaffList);

        // Act
        ResponseEntity<?> response = userActivityService.getAllUserStaff();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy tất cả phụ trách xưởng thành công", apiResponse.getMessage());
        assertEquals(mockStaffList, apiResponse.getData());

        verify(userActivityFilterExtendRepository).getAllUserStaff(
                EntityStatus.ACTIVE,
                EntityStatus.ACTIVE,
                EntityStatus.ACTIVE,
                facilityId);
    }
}