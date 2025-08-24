package udpm.hn.studentattendance.core.staff.studentfactory.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.studentfactory.service.USStudentFactoryService;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class USStudentFactoryRestControllerTest {
    @Mock
    private USStudentFactoryService usStudentFactoryService;

    @InjectMocks
    private USStudentFactoryRestController usStudentFactoryRestController;

    @Test
    void testUSStudentFactoryRestControllerExists() {
        assertNotNull(usStudentFactoryRestController);
    }

    @Test
    void testGetAllStudentInFactory() {
        String factoryId = "123";
        udpm.hn.studentattendance.core.staff.studentfactory.model.request.USStudentFactoryRequest request = mock(
                udpm.hn.studentattendance.core.staff.studentfactory.model.request.USStudentFactoryRequest.class);
        when(usStudentFactoryService.getAllStudentInFactory(factoryId, request))
                .thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = usStudentFactoryRestController.getAllStudentInFactory(factoryId, request);
        assertNotNull(response);
        verify(usStudentFactoryService).getAllStudentInFactory(factoryId, request);
    }

    @Test
    void testDeleteStudentFactory() {
        String studentFactoryId = "123";
        when(usStudentFactoryService.deleteStudentInFactory(studentFactoryId)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = usStudentFactoryRestController.deleteStudentFactory(studentFactoryId);
        assertNotNull(response);
        verify(usStudentFactoryService).deleteStudentInFactory(studentFactoryId);
    }

    @Test
    void testChangeStatusStudentFactory() {
        String studentFactoryId = "123";
        when(usStudentFactoryService.changeStatus(studentFactoryId)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = usStudentFactoryRestController.changeStatusStudentFactory(studentFactoryId);
        assertNotNull(response);
        verify(usStudentFactoryService).changeStatus(studentFactoryId);
    }

    @Test
    void testGetAllStudent() {
        udpm.hn.studentattendance.core.staff.studentfactory.model.request.USUserStudentRequest request = mock(
                udpm.hn.studentattendance.core.staff.studentfactory.model.request.USUserStudentRequest.class);
        when(usStudentFactoryService.getAllStudent(request)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = usStudentFactoryRestController.getAllStudent(request);
        assertNotNull(response);
        verify(usStudentFactoryService).getAllStudent(request);
    }

    @Test
    void testAddOrDeleteStudentInFactory() {
        udpm.hn.studentattendance.core.staff.studentfactory.model.request.USStudentFactoryCreateUpdateRequest request = mock(
                udpm.hn.studentattendance.core.staff.studentfactory.model.request.USStudentFactoryCreateUpdateRequest.class);
        when(usStudentFactoryService.createOrDeleteStudentFactory(request)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = usStudentFactoryRestController.addOrDeleteStudentInFactory(request);
        assertNotNull(response);
        verify(usStudentFactoryService).createOrDeleteStudentFactory(request);
    }

    @Test
    void testGetStudentInFactory() {
        String id = "123";
        when(usStudentFactoryService.getStudentFactoryExist(id)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = usStudentFactoryRestController.getStudentInFactory(id);
        assertNotNull(response);
        verify(usStudentFactoryService).getStudentFactoryExist(id);
    }

    @Test
    void testDetailStudentFactory() {
        String id = "123";
        when(usStudentFactoryService.detailStudentFactory(id)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = usStudentFactoryRestController.detailStudentFactory(id);
        assertNotNull(response);
        verify(usStudentFactoryService).detailStudentFactory(id);
    }

    @Test
    void testDetailShiftByStudent() {
        String id = "123";
        udpm.hn.studentattendance.core.staff.studentfactory.model.request.USPDDetailShiftByStudentRequest request = mock(
                udpm.hn.studentattendance.core.staff.studentfactory.model.request.USPDDetailShiftByStudentRequest.class);
        when(usStudentFactoryService.getAllPlanDateByStudent(request, id)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = usStudentFactoryRestController.detailShiftByStudent(request, id);
        assertNotNull(response);
        verify(usStudentFactoryService).getAllPlanDateByStudent(request, id);
    }
}
