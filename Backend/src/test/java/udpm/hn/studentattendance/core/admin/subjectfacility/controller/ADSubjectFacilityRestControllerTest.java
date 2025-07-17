package udpm.hn.studentattendance.core.admin.subjectfacility.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.subjectfacility.service.ADSubjectFacilityService;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.ADSubjectFacilityCreateRequest;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.ADSubjectFacilityUpdateRequest;

@ExtendWith(MockitoExtension.class)
class ADSubjectFacilityRestControllerTest {
    @Mock
    private ADSubjectFacilityService adSubjectFacilityService;

    @InjectMocks
    private ADSubjectFacilityRestController adSubjectFacilityRestController;

    @Test
    void testADSubjectFacilityRestControllerExists() {
        assertNotNull(adSubjectFacilityRestController);
    }

    @Test
    void testAddSubjectFacility() {
        ADSubjectFacilityCreateRequest request = new ADSubjectFacilityCreateRequest();
        when(adSubjectFacilityService.createSubjectFacility(request)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = adSubjectFacilityRestController.addSubjectFacility(request);
        assertNotNull(response);
        verify(adSubjectFacilityService).createSubjectFacility(request);
    }

    @Test
    void testDeleteSubjectFacility() {
        String id = "123";
        when(adSubjectFacilityService.changeStatus(id)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = adSubjectFacilityRestController.deleteSubjectFacility(id);
        assertNotNull(response);
        verify(adSubjectFacilityService).changeStatus(id);
    }

    @Test
    void testGetSubjectFacility() {
        String id = "123";
        when(adSubjectFacilityService.detailSubjectFacility(id)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = adSubjectFacilityRestController.getSubjectFacility(id);
        assertNotNull(response);
        verify(adSubjectFacilityService).detailSubjectFacility(id);
    }

    @Test
    void testUpdateSubjectFacility() {
        String id = "123";
        ADSubjectFacilityUpdateRequest request = new ADSubjectFacilityUpdateRequest();
        when(adSubjectFacilityService.updateSubjectFacility(id, request)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = adSubjectFacilityRestController.updateSubjectFacility(id, request);
        assertNotNull(response);
        verify(adSubjectFacilityService).updateSubjectFacility(id, request);
    }
}