package udpm.hn.studentattendance.core.teacher.teachingschedule.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.teacher.teachingschedule.model.request.TCTSPlanDateUpdateRequest;
import udpm.hn.studentattendance.core.teacher.teachingschedule.model.request.TCTeachingScheduleRequest;
import udpm.hn.studentattendance.core.teacher.teachingschedule.model.response.TCTeachingScheduleResponse;
import udpm.hn.studentattendance.core.teacher.teachingschedule.repository.TCTeachingScheduleExtendRepository;
import udpm.hn.studentattendance.core.teacher.teachingschedule.service.TCTeachingScheduleService;
import udpm.hn.studentattendance.helpers.SessionHelper;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TCTeachingScheduleRestControllerTest {
    @Mock
    private TCTeachingScheduleService service;
    @Mock
    private TCTeachingScheduleExtendRepository teacherTeachingScheduleExtendRepository;
    @Mock
    private SessionHelper sessionHelper;

    @InjectMocks
    private TCTeachingScheduleRestController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllTeachingScheduleByStaff() {
        TCTeachingScheduleRequest request = new TCTeachingScheduleRequest();
        when(service.getAllTeachingScheduleByStaff(request))
                .thenReturn((ResponseEntity) ResponseEntity.ok("schedules"));
        ResponseEntity<?> res = controller.getAllTeachingScheduleByStaff(request);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("schedules", res.getBody());
    }

    @Test
    void testGetTeachingScheduleByPresent() {
        TCTeachingScheduleRequest request = new TCTeachingScheduleRequest();
        when(service.getAllTeachingSchedulePresent(request)).thenReturn((ResponseEntity) ResponseEntity.ok("present"));
        ResponseEntity<?> res = controller.getTeachingScheduleByPresent(request);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("present", res.getBody());
    }

    @Test
    void testGetAllFactoriesStaff() {
        when(service.getAllFactoryByStaff()).thenReturn((ResponseEntity) ResponseEntity.ok("factories"));
        ResponseEntity<?> res = controller.getAllFactoriesStaff();
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("factories", res.getBody());
    }

    @Test
    void testGetAllProjectsStaff() {
        when(service.getAllProjectByStaff()).thenReturn((ResponseEntity) ResponseEntity.ok("projects"));
        ResponseEntity<?> res = controller.getAllProjectsStaff();
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("projects", res.getBody());
    }

    @Test
    void testGetAllSubjectsStaff() {
        when(service.getAllSubjectByStaff()).thenReturn((ResponseEntity) ResponseEntity.ok("subjects"));
        ResponseEntity<?> res = controller.getAllSubjectsStaff();
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("subjects", res.getBody());
    }

    @Test
    void testGetDetailPlanDate() {
        when(service.getDetailPlanDate("1")).thenReturn((ResponseEntity) ResponseEntity.ok("detail"));
        ResponseEntity<?> res = controller.getDetailPlanDate("1");
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("detail", res.getBody());
    }

    @Test
    void testUpdatePlanDate() {
        TCTSPlanDateUpdateRequest request = new TCTSPlanDateUpdateRequest();
        when(service.updatePlanDate(request)).thenReturn((ResponseEntity) ResponseEntity.ok("updated"));
        ResponseEntity<?> res = controller.updatePlanDate(request);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("updated", res.getBody());
    }

    @Test
    void testChangeType() {
        when(service.changeTypePlanDate("1", "room1")).thenReturn((ResponseEntity) ResponseEntity.ok("changed"));
        ResponseEntity<?> res = controller.changeType("1", "room1");
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("changed", res.getBody());
    }

    @Test
    void testExportTeachingSchedule() throws Exception {
        TCTeachingScheduleRequest request = new TCTeachingScheduleRequest();
        when(sessionHelper.getUserId()).thenReturn("USER123");
        List<TCTeachingScheduleResponse> mockList = List.of(mock(TCTeachingScheduleResponse.class));
        when(teacherTeachingScheduleExtendRepository.exportExcelTeachingSchedule("USER123", request))
                .thenReturn(mockList);
        when(service.exportTeachingSchedule(mockList)).thenReturn(new ByteArrayInputStream("test".getBytes()));

        ResponseEntity<InputStreamResource> res = controller.exportTeachingSchedule(request);
        assertEquals(200, res.getStatusCodeValue());
        assertNotNull(res.getBody());
    }

    @Test
    void testGetAllType() {
        when(service.getAllType()).thenReturn((ResponseEntity) ResponseEntity.ok("types"));
        ResponseEntity<?> res = controller.getAllType();
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("types", res.getBody());
    }
}