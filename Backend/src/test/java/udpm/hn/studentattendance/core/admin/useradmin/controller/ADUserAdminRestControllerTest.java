package udpm.hn.studentattendance.core.admin.useradmin.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.useradmin.model.request.ADUserAdminCreateOrUpdateRequest;
import udpm.hn.studentattendance.core.admin.useradmin.service.impl.ADUserAdminServiceImpl;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ADUserAdminRestControllerTest {
    @Mock
    private ADUserAdminServiceImpl adUserAdminService;

    @InjectMocks
    private ADUserAdminRestController adUserAdminRestController;

    @Test
    void testADUserAdminRestControllerExists() {
        assertNotNull(adUserAdminRestController);
    }

    @Test
    void testGetAllUserAdmin() {
        var request = mock(udpm.hn.studentattendance.core.admin.useradmin.model.request.ADUserAdminRequest.class);
        when(adUserAdminService.getAllUserAdmin(request)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = adUserAdminRestController.getAllUserAdmin(request);
        assertNotNull(response);
        verify(adUserAdminService).getAllUserAdmin(request);
    }

    @Test
    void testGetUserAdminById() {
        String id = "123";
        when(adUserAdminService.getUserAdminById(id)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = adUserAdminRestController.getUserAdminById(id);
        assertNotNull(response);
        verify(adUserAdminService).getUserAdminById(id);
    }

    @Test
    void testCreateUserAdmin() {
        ADUserAdminCreateOrUpdateRequest request = new ADUserAdminCreateOrUpdateRequest();
        when(adUserAdminService.createUserAdmin(request)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = adUserAdminRestController.createUserAdmin(request);
        assertNotNull(response);
        verify(adUserAdminService).createUserAdmin(request);
    }

    @Test
    void testUpdateUserAdmin() {
        String id = "123";
        ADUserAdminCreateOrUpdateRequest request = new ADUserAdminCreateOrUpdateRequest();
        when(adUserAdminService.updateUserAdmin(request, id)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = adUserAdminRestController.updateUserAdmin(request, id);
        assertNotNull(response);
        verify(adUserAdminService).updateUserAdmin(request, id);
    }

    @Test
    void testDeleteUserAdmin() {
        String id = "123";
        when(adUserAdminService.deleteUserAdmin(id)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = adUserAdminRestController.deleteUserAdmin(id);
        assertNotNull(response);
        verify(adUserAdminService).deleteUserAdmin(id);
    }
}