package udpm.hn.studentattendance.core.student.schedule.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.student.schedule.service.STDScheduleAttendanceService;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.InputStreamResource;
import udpm.hn.studentattendance.core.student.schedule.model.request.STDScheduleAttendanceSearchRequest;
import udpm.hn.studentattendance.core.student.schedule.model.response.STDScheduleAttendanceResponse;
import udpm.hn.studentattendance.core.student.schedule.repository.STDScheduleAttendanceRepository;
import udpm.hn.studentattendance.helpers.SessionHelper;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class STDScheduleAttendanceRestControllerTest {
    @Mock
    private SessionHelper sessionHelper;
    @Mock
    private STDScheduleAttendanceRepository repository;
    @Mock
    private STDScheduleAttendanceService stdScheduleAttendanceService;
    @InjectMocks
    private STDScheduleAttendanceRestController stdScheduleAttendanceRestController;

    @Test
    void testGetListProject() {
        STDScheduleAttendanceSearchRequest request = new STDScheduleAttendanceSearchRequest();
        String userId = "student123";
        request.setIdStudent(userId);
        when(sessionHelper.getUserId()).thenReturn(userId);
        when(stdScheduleAttendanceService.getList(any(STDScheduleAttendanceSearchRequest.class)))
                .thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = stdScheduleAttendanceRestController.getListProject(request);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testExportTeachingSchedule() throws Exception {
        STDScheduleAttendanceSearchRequest request = new STDScheduleAttendanceSearchRequest();
        String userId = "student123";
        request.setIdStudent(userId);
        when(sessionHelper.getUserId()).thenReturn(userId);
        List<STDScheduleAttendanceResponse> mockList = Collections.emptyList();
        when(repository.getAllListAttendanceByUserList(any(STDScheduleAttendanceSearchRequest.class)))
                .thenReturn(mockList);
        ByteArrayInputStream mockStream = new ByteArrayInputStream(new byte[] { 1, 2, 3 });
        when(stdScheduleAttendanceService.exportScheduleAttendance(mockList)).thenReturn(mockStream);
        ResponseEntity<InputStreamResource> response = stdScheduleAttendanceRestController
                .exportTeachingSchedule(request);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("application/pdf", response.getHeaders().getContentType().toString());
    }
}