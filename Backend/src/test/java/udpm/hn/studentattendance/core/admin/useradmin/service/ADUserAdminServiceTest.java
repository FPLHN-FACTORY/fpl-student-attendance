package udpm.hn.studentattendance.core.admin.useradmin.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.useradmin.model.request.ADUserAdminCreateOrUpdateRequest;
import udpm.hn.studentattendance.core.admin.useradmin.repository.ADUserAdminExtendRepository;
import udpm.hn.studentattendance.core.admin.useradmin.repository.ADUserAdminStaffExtendRepository;
import udpm.hn.studentattendance.core.notification.service.NotificationService;
import udpm.hn.studentattendance.helpers.*;
import udpm.hn.studentattendance.infrastructure.config.redis.service.RedisService;
import udpm.hn.studentattendance.entities.UserAdmin;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ADUserAdminServiceTest {
    @Mock
    private ADUserAdminExtendRepository userAdminExtendRepository;
    @Mock
    private ADUserAdminStaffExtendRepository userAdminStaffExtendRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private SessionHelper sessionHelper;
    @Mock
    private MailerHelper mailerHelper;
    @Mock
    private UserActivityLogHelper userActivityLogHelper;
    @Mock
    private RedisService redisService;
    @Mock
    private RedisInvalidationHelper redisInvalidationHelper;
    @Mock
    private SettingHelper settingHelper;

    @InjectMocks
    private udpm.hn.studentattendance.core.admin.useradmin.service.impl.ADUserAdminServiceImpl adUserAdminService;

    @Test
    void testCreateUserAdminSuccess() {
        ADUserAdminCreateOrUpdateRequest request = new ADUserAdminCreateOrUpdateRequest();
        request.setStaffCode("A001");
        request.setEmail("admin@gmail.com");
        request.setStaffName("Nguyen Van A");
        when(userAdminExtendRepository.getUserAdminByCode(anyString())).thenReturn(Optional.empty());
        when(userAdminExtendRepository.getUserAdminByEmail(anyString())).thenReturn(Optional.empty());
        when(userAdminExtendRepository.save(any(UserAdmin.class))).thenReturn(new UserAdmin());
        when(sessionHelper.getUserId()).thenReturn("current-user-id");
        when(notificationService.add(any())).thenReturn(null);

        ResponseEntity<?> response = adUserAdminService.createUserAdmin(request);
        assertEquals(200, response.getStatusCodeValue());
        verify(userAdminExtendRepository).save(any(UserAdmin.class));
        verify(userActivityLogHelper).saveLog(anyString());
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    void testGetUserAdminByIdNotFound() {
        String id = "1";
        when(userAdminExtendRepository.findById(id)).thenReturn(Optional.empty());
        ResponseEntity<?> response = adUserAdminService.getUserAdminById(id);
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void testDeleteUserAdminSuccess() {
        String id = "1";
        UserAdmin userAdmin = new UserAdmin();
        userAdmin.setId(id);
        when(userAdminExtendRepository.findById(id)).thenReturn(Optional.of(userAdmin));
        doNothing().when(userAdminExtendRepository).deleteById(id);
        ResponseEntity<?> response = adUserAdminService.deleteUserAdmin(id);
        assertEquals(200, response.getStatusCodeValue());
        verify(userAdminExtendRepository).deleteById(id);
        verify(userActivityLogHelper).saveLog(anyString());
        verify(redisInvalidationHelper).invalidateAllCaches();
    }
}