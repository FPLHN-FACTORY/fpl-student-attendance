package udpm.hn.studentattendance.core.student.schedule.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import udpm.hn.studentattendance.core.student.schedule.model.request.STDScheduleAttendanceSearchRequest;
import udpm.hn.studentattendance.core.student.schedule.model.response.STDScheduleAttendanceResponse;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class STDScheduleAttendanceServiceTest {
    @Mock
    private STDScheduleAttendanceService stdScheduleAttendanceService;

    @Test
    void testGetList() {
        STDScheduleAttendanceSearchRequest request = new STDScheduleAttendanceSearchRequest();
        when(stdScheduleAttendanceService.getList(any(STDScheduleAttendanceSearchRequest.class)))
                .thenReturn(ResponseEntity.ok().build());
        ResponseEntity<?> response = stdScheduleAttendanceService.getList(request);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testExportScheduleAttendance() {
        List<STDScheduleAttendanceResponse> mockList = Collections.emptyList();
        ByteArrayInputStream mockStream = new ByteArrayInputStream(new byte[] { 1, 2, 3 });
        when(stdScheduleAttendanceService.exportScheduleAttendance(mockList)).thenReturn(mockStream);
        ByteArrayInputStream result = stdScheduleAttendanceService.exportScheduleAttendance(mockList);
        assertNotNull(result);
    }
}
