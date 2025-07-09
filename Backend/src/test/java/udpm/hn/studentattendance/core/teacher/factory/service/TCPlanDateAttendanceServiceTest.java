package udpm.hn.studentattendance.core.teacher.factory.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.teacher.factory.repository.TCPlanDateRepository;
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCFilterPlanDateAttendanceRequest;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TCPlanDateAttendanceServiceTest {
    @Mock
    private TCPlanDateRepository tcPlanDateRepository;

    @InjectMocks
    private TCPlanDateAttendanceService tcPlanDateAttendanceService;

    @Test
    void testTCPlanDateAttendanceServiceExists() {
        assertNotNull(tcPlanDateAttendanceService);
    }

    @Test
    void testGetDetail() {
        String idPlanDate = "123";
        when(tcPlanDateRepository.findById(idPlanDate)).thenReturn(java.util.Optional.empty());

        ResponseEntity<?> response = tcPlanDateAttendanceService.getDetail(idPlanDate);

        assertNotNull(response);
        verify(tcPlanDateRepository).findById(idPlanDate);
    }

    @Test
    void testGetAllList() {
        TCFilterPlanDateAttendanceRequest request = new TCFilterPlanDateAttendanceRequest();
        when(tcPlanDateRepository.findAll()).thenReturn(java.util.Collections.emptyList());

        ResponseEntity<?> response = tcPlanDateAttendanceService.getAllList(request);

        assertNotNull(response);
        verify(tcPlanDateRepository).findAll();
    }
}