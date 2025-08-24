package udpm.hn.studentattendance.core.teacher.factory.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.teacher.factory.service.TCShiftFactoryService;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCFilterShiftFactoryRequest;

@ExtendWith(MockitoExtension.class)
class TCShiftFactoryRestControllerTest {
    @Mock
    private TCShiftFactoryService tcShiftFactoryService;

    @InjectMocks
    private TCShiftFactoryRestController tcShiftFactoryRestController;

    @Test
    void testTCShiftFactoryRestControllerExists() {
        assertNotNull(tcShiftFactoryRestController);
    }

    @Test
    void testGetDetail() {
        String idFactory = "factory123";
        when(tcShiftFactoryService.getDetail(idFactory)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = tcShiftFactoryRestController.getDetail(idFactory);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(tcShiftFactoryService).getDetail(idFactory);
    }

    @Test
    void testGetAllList() {
        String idFactory = "factory123";
        TCFilterShiftFactoryRequest request = new TCFilterShiftFactoryRequest();
        request.setIdFactory(idFactory);
        when(tcShiftFactoryService.getAllList(request)).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = tcShiftFactoryRestController.getAllList(idFactory, request);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(tcShiftFactoryService).getAllList(request);
    }

    @Test
    void testGetAllShift() {
        when(tcShiftFactoryService.getListShift()).thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = tcShiftFactoryRestController.getAllShift();
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(tcShiftFactoryService).getListShift();
    }
}
