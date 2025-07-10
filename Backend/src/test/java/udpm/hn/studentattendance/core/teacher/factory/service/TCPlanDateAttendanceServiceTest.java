package udpm.hn.studentattendance.core.teacher.factory.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.teacher.factory.repository.TCPlanDateRepository;
import udpm.hn.studentattendance.core.teacher.factory.repository.TCAttendanceRepository;
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCFilterPlanDateAttendanceRequest;
import udpm.hn.studentattendance.core.teacher.factory.model.response.TCPlanDateStudentResponse;
import udpm.hn.studentattendance.core.teacher.factory.service.impl.TCPlanDateAttendanceServiceImpl;
import udpm.hn.studentattendance.helpers.SessionHelper;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TCPlanDateAttendanceServiceTest {
    @Mock
    private TCPlanDateRepository tcPlanDateRepository;

    @Mock
    private TCAttendanceRepository tcAttendanceRepository;

    @Mock
    private SessionHelper sessionHelper;

    @InjectMocks
    private TCPlanDateAttendanceServiceImpl tcPlanDateAttendanceService;

    @Test
    void testTCPlanDateAttendanceServiceExists() {
        assertNotNull(tcPlanDateAttendanceService);
    }

    @Test
    void testGetDetail() {
        String idPlanDate = "123";
        when(sessionHelper.getFacilityId()).thenReturn("facility-1");
        when(tcAttendanceRepository.getDetailPlanDate(idPlanDate, "facility-1")).thenReturn(java.util.Optional.empty());

        ResponseEntity<?> response = tcPlanDateAttendanceService.getDetail(idPlanDate);

        assertNotNull(response);
        verify(sessionHelper).getFacilityId();
        verify(tcAttendanceRepository).getDetailPlanDate(idPlanDate, "facility-1");
    }

    @Test
    void testGetAllList() {
        TCFilterPlanDateAttendanceRequest request = new TCFilterPlanDateAttendanceRequest();
        when(sessionHelper.getFacilityId()).thenReturn("facility-1");
        when(tcAttendanceRepository.getAllByFilter(any(), any())).thenReturn(Page.empty());

        ResponseEntity<?> response = tcPlanDateAttendanceService.getAllList(request);

        assertNotNull(response);
        verify(sessionHelper).getFacilityId();
        verify(tcAttendanceRepository).getAllByFilter(any(), any());
    }
}