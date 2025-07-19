package udpm.hn.studentattendance.core.staff.factory.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.factory.service.USFactoryService;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import udpm.hn.studentattendance.core.staff.factory.model.request.USFactoryRequest;
import udpm.hn.studentattendance.core.staff.factory.model.request.USFactoryCreateUpdateRequest;

@ExtendWith(MockitoExtension.class)
class USFactoryRestControllerTest {
    @Mock
    private USFactoryService usFactoryService;

    @InjectMocks
    private USFactoryRestController usFactoryRestController;

    @Test
    void testUSFactoryRestControllerExists() {
        assertNotNull(usFactoryRestController);
    }

    @Test
    void testGetAllFactory() {
        USFactoryRequest request = mock(
                udpm.hn.studentattendance.core.staff.factory.model.request.USFactoryRequest.class);
        when(usFactoryService.getAllFactory(request)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = usFactoryRestController.getAllFactory(request);
        assertNotNull(response);
        verify(usFactoryService).getAllFactory(request);
    }

    @Test
    void testGetDetailFactory() {
        String factoryId = "123";
        when(usFactoryService.getDetailFactory(factoryId)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = usFactoryRestController.getDetailFactory(factoryId);
        assertNotNull(response);
        verify(usFactoryService).getDetailFactory(factoryId);
    }

    @Test
    void testCreateFactory() {
        udpm.hn.studentattendance.core.staff.factory.model.request.USFactoryCreateUpdateRequest factoryRequest = mock(
                udpm.hn.studentattendance.core.staff.factory.model.request.USFactoryCreateUpdateRequest.class);
        when(usFactoryService.createFactory(factoryRequest)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = usFactoryRestController.createFactory(factoryRequest);
        assertNotNull(response);
        verify(usFactoryService).createFactory(factoryRequest);
    }

    @Test
    void testUpdateFactory() {
        udpm.hn.studentattendance.core.staff.factory.model.request.USFactoryCreateUpdateRequest factoryRequest = mock(
                udpm.hn.studentattendance.core.staff.factory.model.request.USFactoryCreateUpdateRequest.class);
        when(usFactoryService.updateFactory(factoryRequest)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = usFactoryRestController.updateFactory(factoryRequest);
        assertNotNull(response);
        verify(usFactoryService).updateFactory(factoryRequest);
    }

    @Test
    void testChangeStatus() {
        String factoryId = "123";
        when(usFactoryService.changeStatus(factoryId)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = usFactoryRestController.changeStatus(factoryId);
        assertNotNull(response);
        verify(usFactoryService).changeStatus(factoryId);
    }
}