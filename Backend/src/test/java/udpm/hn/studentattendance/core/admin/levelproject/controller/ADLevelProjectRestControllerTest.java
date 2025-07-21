package udpm.hn.studentattendance.core.admin.levelproject.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.levelproject.service.ADLevelProjectManagementService;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import udpm.hn.studentattendance.core.admin.levelproject.model.request.ADLevelProjectCreateRequest;
import udpm.hn.studentattendance.core.admin.levelproject.model.request.ADLevelProjectUpdateRequest;
import udpm.hn.studentattendance.core.admin.levelproject.model.request.ADLevelProjectSearchRequest;

@ExtendWith(MockitoExtension.class)
class ADLevelProjectRestControllerTest {
    @Mock
    private ADLevelProjectManagementService adLevelProjectService;

    @InjectMocks
    private ADLevelProjectRestController adLevelProjectRestController;

    @Test
    void testADLevelProjectRestControllerExists() {
        assertNotNull(adLevelProjectRestController);
    }

    @Test
    void testGetListLevelProject() {
        ADLevelProjectSearchRequest request = new ADLevelProjectSearchRequest();
        when(adLevelProjectService.getListLevelProject(any())).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<?> response = adLevelProjectRestController.getListLevelProject(request);

        assertNotNull(response);
        verify(adLevelProjectService).getListLevelProject(any());
    }

    @Test
    void testGetLevelProject() {
        String projectId = "123";
        when(adLevelProjectService.detailLevelProject(projectId)).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<?> response = adLevelProjectRestController.getLevelProject(projectId);

        assertNotNull(response);
        verify(adLevelProjectService).detailLevelProject(projectId);
    }

    @Test
    void testAddLevelProject() {
        ADLevelProjectCreateRequest projectRequest = new ADLevelProjectCreateRequest();
        when(adLevelProjectService.createLevelProject(projectRequest)).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<?> response = adLevelProjectRestController.addLevelProject(projectRequest);

        assertNotNull(response);
        verify(adLevelProjectService).createLevelProject(projectRequest);
    }

    @Test
    void testUpdateLevelProject() {
        String projectId = "123";
        ADLevelProjectUpdateRequest projectRequest = new ADLevelProjectUpdateRequest();
        when(adLevelProjectService.updateLevelProject(projectId, projectRequest))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<?> response = adLevelProjectRestController.updateLevelProject(projectId, projectRequest);

        assertNotNull(response);
        verify(adLevelProjectService).updateLevelProject(projectId, projectRequest);
    }

    @Test
    void testDeleteLevelProject() {
        String projectId = "123";
        when(adLevelProjectService.changeStatus(projectId)).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<?> response = adLevelProjectRestController.deleteLevelProject(projectId);

        assertNotNull(response);
        verify(adLevelProjectService).changeStatus(projectId);
    }
}
