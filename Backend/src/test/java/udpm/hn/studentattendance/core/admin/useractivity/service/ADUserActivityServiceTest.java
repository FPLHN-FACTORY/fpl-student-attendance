package udpm.hn.studentattendance.core.admin.useractivity.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.core.admin.useractivity.repository.ADUserActivityFilterExtendRepository;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.model.request.UALFilterRequest;
import udpm.hn.studentattendance.infrastructure.common.model.response.UALResponse;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ADUserActivityServiceTest {
    @Mock
    private UserActivityLogHelper userActivityLogHelper;
    @Mock
    private ADUserActivityFilterExtendRepository userActivityFilterExtendRepository;

    @InjectMocks
    private udpm.hn.studentattendance.core.admin.useractivity.service.impl.ADUserActivityServiceImpl adUserActivityService;

    @Test
    void testGetAllUserActivity() {
        UALFilterRequest request = new UALFilterRequest();
        when(userActivityLogHelper.getAll(request)).thenReturn(new PageableObject<>(Collections.emptyList(), 0, 0));
        ResponseEntity<?> response = adUserActivityService.getAllUserActivity(request);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        verify(userActivityLogHelper).getAll(request);
    }

    @Test
    void testGetAllFacility() {
        when(userActivityFilterExtendRepository.getAllFacility(EntityStatus.ACTIVE))
                .thenReturn(Collections.emptyList());
        ResponseEntity<?> response = adUserActivityService.getAllFacility();
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        verify(userActivityFilterExtendRepository).getAllFacility(EntityStatus.ACTIVE);
    }

    @Test
    void testGetAllUserAdmin() {
        when(userActivityFilterExtendRepository.getAllUserAdmin(EntityStatus.ACTIVE))
                .thenReturn(Collections.emptyList());
        ResponseEntity<?> response = adUserActivityService.getAllUserAdmin();
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        verify(userActivityFilterExtendRepository).getAllUserAdmin(EntityStatus.ACTIVE);
    }

    @Test
    void testGetAllUserStaff() {
        when(userActivityFilterExtendRepository.getAllUserStaff(EntityStatus.ACTIVE))
                .thenReturn(Collections.emptyList());
        ResponseEntity<?> response = adUserActivityService.getAllUserStaff();
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        verify(userActivityFilterExtendRepository).getAllUserStaff(EntityStatus.ACTIVE);
    }
}