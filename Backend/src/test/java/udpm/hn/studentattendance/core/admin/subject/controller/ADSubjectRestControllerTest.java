package udpm.hn.studentattendance.core.admin.subject.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.subject.model.request.ADSubjectCreateRequest;
import udpm.hn.studentattendance.core.admin.subject.model.request.ADSubjectUpdateRequest;
import udpm.hn.studentattendance.core.admin.subject.service.ADSubjectManagementService;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ADSubjectRestControllerTest {
    @Mock
    private ADSubjectManagementService adSubjectManagementService;

    @InjectMocks
    private ADSubjectRestController adSubjectRestController;

    @Test
    void testADSubjectRestControllerExists() {
        assertNotNull(adSubjectRestController);
    }

    @Test
    void testGetListSubject() {
        when(adSubjectManagementService.getListSubject(any())).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = adSubjectRestController.getListSubject(null);
        assertNotNull(response);
        verify(adSubjectManagementService).getListSubject(any());
    }

    @Test
    void testGetSubject() {
        String subjectId = "123";
        when(adSubjectManagementService.detailSubject(subjectId)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = adSubjectRestController.getSubject(subjectId);
        assertNotNull(response);
        verify(adSubjectManagementService).detailSubject(subjectId);
    }

    @Test
    void testAddSubject() {
        ADSubjectCreateRequest subjectRequest = new ADSubjectCreateRequest();
        when(adSubjectManagementService.createSubject(subjectRequest)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = adSubjectRestController.addSubject(subjectRequest);
        assertNotNull(response);
        verify(adSubjectManagementService).createSubject(subjectRequest);
    }

    @Test
    void testUpdateSubject() {
        String subjectId = "123";
        ADSubjectUpdateRequest subjectRequest = new ADSubjectUpdateRequest();
        when(adSubjectManagementService.updateSubject(subjectId, subjectRequest))
                .thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = adSubjectRestController.updateSubject(subjectId, subjectRequest);
        assertNotNull(response);
        verify(adSubjectManagementService).updateSubject(subjectId, subjectRequest);
    }

    @Test
    void testDeleteSubject() {
        String subjectId = "123";
        when(adSubjectManagementService.changeStatus(subjectId)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = adSubjectRestController.deleteSubject(subjectId);
        assertNotNull(response);
        verify(adSubjectManagementService).changeStatus(subjectId);
    }
}
