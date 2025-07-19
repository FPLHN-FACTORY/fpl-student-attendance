package udpm.hn.studentattendance.core.admin.useractivity.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.useractivity.repository.ADUserActivityFilterExtendRepository;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.model.request.UALFilterRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ADUserActivityServiceImplTest {
    @Mock
    private UserActivityLogHelper userActivityLogHelper;
    @Mock
    private ADUserActivityFilterExtendRepository userActivityFilterExtendRepository;
    @InjectMocks
    private ADUserActivityServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllUserActivity() {
        UALFilterRequest request = mock(UALFilterRequest.class);
        when(userActivityLogHelper.getAll(request)).thenReturn(null);
        ResponseEntity<?> response = service.getAllUserActivity(request);
        assertNotNull(response);
        verify(userActivityLogHelper, times(1)).getAll(request);
    }

    @Test
    void testGetAllFacility() {
        when(userActivityFilterExtendRepository.getAllFacility(EntityStatus.ACTIVE)).thenReturn(null);
        ResponseEntity<?> response = service.getAllFacility();
        assertNotNull(response);
        verify(userActivityFilterExtendRepository, times(1)).getAllFacility(EntityStatus.ACTIVE);
    }

    @Test
    void testGetAllUserAdmin() {
        when(userActivityFilterExtendRepository.getAllUserAdmin(EntityStatus.ACTIVE)).thenReturn(null);
        ResponseEntity<?> response = service.getAllUserAdmin();
        assertNotNull(response);
        verify(userActivityFilterExtendRepository, times(1)).getAllUserAdmin(EntityStatus.ACTIVE);
    }

    @Test
    void testGetAllUserStaff() {
        when(userActivityFilterExtendRepository.getAllUserStaff(EntityStatus.ACTIVE)).thenReturn(null);
        ResponseEntity<?> response = service.getAllUserStaff();
        assertNotNull(response);
        verify(userActivityFilterExtendRepository, times(1)).getAllUserStaff(EntityStatus.ACTIVE);
    }
}