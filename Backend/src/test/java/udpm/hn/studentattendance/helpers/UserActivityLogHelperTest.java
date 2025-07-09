package udpm.hn.studentattendance.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.UserActivityLog;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.model.request.UALFilterRequest;
import udpm.hn.studentattendance.infrastructure.common.model.response.UALResponse;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonUserActivityLogRepository;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;
import udpm.hn.studentattendance.repositories.FacilityRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserActivityLogHelperTest {

    @Mock
    private CommonUserActivityLogRepository commonUserActivityLogRepository;

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private SessionHelper sessionHelper;

    @InjectMocks
    private UserActivityLogHelper userActivityLogHelper;

    @BeforeEach
    void setUp() {
        lenient().when(sessionHelper.getUserId()).thenReturn("1");
        lenient().when(sessionHelper.getLoginRole()).thenReturn(RoleConstant.ADMIN);
    }

    @Test
    void canInstantiate() {
        CommonUserActivityLogRepository mockLogRepo = Mockito.mock(CommonUserActivityLogRepository.class);
        FacilityRepository mockFacilityRepo = Mockito.mock(FacilityRepository.class);
        SessionHelper mockSessionHelper = Mockito.mock(SessionHelper.class);
        UserActivityLogHelper helper = new UserActivityLogHelper(mockLogRepo, mockFacilityRepo, mockSessionHelper);
        assertThat(helper).isNotNull();
    }

    @Test
    void testSaveLogWithValidFacility() {
        // Given
        String message = "Test activity log";
        String facilityId = "facility-123";
        Facility facility = new Facility();
        facility.setId(facilityId);

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));
        when(commonUserActivityLogRepository.save(any(UserActivityLog.class))).thenReturn(new UserActivityLog());

        // When
        userActivityLogHelper.saveLog(message);

        // Then
        verify(sessionHelper).getFacilityId();
        verify(sessionHelper).getLoginRole();
        verify(sessionHelper).getUserId();
        verify(facilityRepository).findById(facilityId);
        verify(commonUserActivityLogRepository).save(any(UserActivityLog.class));
    }

    @Test
    void testSaveLogWithNullFacilityId() {
        // Given
        String message = "Test activity log";

        when(sessionHelper.getFacilityId()).thenReturn(null);

        // When
        userActivityLogHelper.saveLog(message);

        // Then
        verify(sessionHelper).getFacilityId();
        verify(sessionHelper).getLoginRole();
        verify(sessionHelper).getUserId();
        verify(facilityRepository, never()).findById(any());
        verify(commonUserActivityLogRepository).save(any(UserActivityLog.class));
    }

    @Test
    void testSaveLogWithNonExistentFacility() {
        // Given
        String message = "Test activity log";
        String facilityId = "non-existent-facility";

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(facilityRepository.findById(facilityId)).thenReturn(Optional.empty());

        // When
        userActivityLogHelper.saveLog(message);

        // Then
        verify(sessionHelper).getFacilityId();
        verify(sessionHelper).getLoginRole();
        verify(sessionHelper).getUserId();
        verify(facilityRepository).findById(facilityId);
        verify(commonUserActivityLogRepository).save(any(UserActivityLog.class));
    }

    @Test
    void testSaveLogWithEmptyMessage() {
        // Given
        String message = "";
        String facilityId = "facility-123";
        Facility facility = new Facility();
        facility.setId(facilityId);

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));
        when(commonUserActivityLogRepository.save(any(UserActivityLog.class))).thenReturn(new UserActivityLog());

        // When
        userActivityLogHelper.saveLog(message);

        // Then
        verify(commonUserActivityLogRepository).save(any(UserActivityLog.class));
    }

    @Test
    void testSaveLogWithNullMessage() {
        // Given
        String message = null;
        String facilityId = "facility-123";
        Facility facility = new Facility();
        facility.setId(facilityId);

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));
        when(commonUserActivityLogRepository.save(any(UserActivityLog.class))).thenReturn(new UserActivityLog());

        // When
        userActivityLogHelper.saveLog(message);

        // Then
        verify(commonUserActivityLogRepository).save(any(UserActivityLog.class));
    }

    @Test
    void testGetAllWithValidRequest() {
        // Given
        UALFilterRequest request = new UALFilterRequest();
        request.setPage(0);
        request.setSize(10);

        UALResponse mockResponse1 = Mockito.mock(UALResponse.class);
        UALResponse mockResponse2 = Mockito.mock(UALResponse.class);
        List<UALResponse> mockResponses = Arrays.asList(mockResponse1, mockResponse2);
        Page<UALResponse> mockPage = new PageImpl<>(mockResponses);

        when(commonUserActivityLogRepository.getListFilter(any(Pageable.class), eq(request)))
                .thenReturn(mockPage);

        // When
        PageableObject<UALResponse> result = userActivityLogHelper.getAll(request);

        // Then
        assertNotNull(result);
        verify(commonUserActivityLogRepository).getListFilter(any(Pageable.class), eq(request));
    }

    @Test
    void testGetAllWithNullRequest() {
        // Given
        UALFilterRequest request = null;

        UALResponse mockResponse1 = Mockito.mock(UALResponse.class);
        UALResponse mockResponse2 = Mockito.mock(UALResponse.class);
        List<UALResponse> mockResponses = Arrays.asList(mockResponse1, mockResponse2);
        Page<UALResponse> mockPage = new PageImpl<>(mockResponses);

        when(commonUserActivityLogRepository.getListFilter(any(Pageable.class), eq(null)))
                .thenReturn(mockPage);

        // When
        PageableObject<UALResponse> result = userActivityLogHelper.getAll(request);

        // Then
        assertNotNull(result);
        verify(commonUserActivityLogRepository).getListFilter(any(Pageable.class), eq(null));
    }

    @Test
    void testGetAllWithEmptyResult() {
        // Given
        UALFilterRequest request = new UALFilterRequest();
        request.setPage(0);
        request.setSize(10);

        Page<UALResponse> mockPage = new PageImpl<>(Arrays.asList());

        when(commonUserActivityLogRepository.getListFilter(any(Pageable.class), eq(request)))
                .thenReturn(mockPage);

        // When
        PageableObject<UALResponse> result = userActivityLogHelper.getAll(request);

        // Then
        assertNotNull(result);
        verify(commonUserActivityLogRepository).getListFilter(any(Pageable.class), eq(request));
    }

    @Test
    void testSaveLogWithDifferentRoles() {
        // Given
        String message = "Test activity log";
        String facilityId = "facility-123";
        Facility facility = new Facility();
        facility.setId(facilityId);

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));
        when(commonUserActivityLogRepository.save(any(UserActivityLog.class))).thenReturn(new UserActivityLog());

        // Test with different roles
        RoleConstant[] roles = { RoleConstant.STUDENT, RoleConstant.TEACHER, RoleConstant.STAFF, RoleConstant.ADMIN };

        for (RoleConstant role : roles) {
            when(sessionHelper.getLoginRole()).thenReturn(role);

            // When
            userActivityLogHelper.saveLog(message);

            // Then
            verify(sessionHelper).getLoginRole();
        }
    }

    @Test
    void testSaveLogWithDifferentUserIds() {
        // Given
        String message = "Test activity log";
        String facilityId = "facility-123";
        Facility facility = new Facility();
        facility.setId(facilityId);

        when(sessionHelper.getFacilityId()).thenReturn(facilityId);
        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));
        when(commonUserActivityLogRepository.save(any(UserActivityLog.class))).thenReturn(new UserActivityLog());

        // Test with different user IDs
        String[] userIds = { "1", "2", "3", "100" };

        for (String userId : userIds) {
            when(sessionHelper.getUserId()).thenReturn(userId);

            // When
            userActivityLogHelper.saveLog(message);

            // Then
            verify(sessionHelper).getUserId();
        }
    }
}