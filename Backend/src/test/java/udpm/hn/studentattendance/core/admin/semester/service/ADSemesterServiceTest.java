package udpm.hn.studentattendance.core.admin.semester.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.semester.model.request.ADCreateUpdateSemesterRequest;
import udpm.hn.studentattendance.core.admin.semester.repository.ADSemesterRepository;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.infrastructure.config.redis.service.RedisService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ADSemesterServiceTest {
    @Mock
    private ADSemesterRepository adSemesterRepository;
    @Mock
    private CommonUserStudentRepository commonUserStudentRepository;
    @Mock
    private UserActivityLogHelper userActivityLogHelper;
    @Mock
    private RedisService redisService;
    @Mock
    private RedisInvalidationHelper redisInvalidationHelper;

    @InjectMocks
    private udpm.hn.studentattendance.core.admin.semester.service.impl.ADSemesterServiceImpl adSemesterService;

    @BeforeEach
    void setUp() {
        // Có thể set các giá trị field nếu cần
    }

    @Test
    void testCreateSemesterNotConflict() {
        ADCreateUpdateSemesterRequest request = new ADCreateUpdateSemesterRequest();
        request.setSemesterName("SPRING");

        // Set dates to future (at least 3 months apart and in the same year)
        long currentTime = System.currentTimeMillis();
        long fromDate = currentTime + (24 * 60 * 60 * 1000L); // Tomorrow
        long toDate = fromDate + (4 * 30 * 24 * 60 * 60 * 1000L); // 4 months later

        request.setFromDate(fromDate);
        request.setToDate(toDate);

        when(adSemesterRepository.checkConflictTime(anyLong(), anyLong()))
                .thenReturn(java.util.Collections.emptyList());
        when(adSemesterRepository.checkSemesterExistNameAndYear(anyString(), anyInt(), any(), any()))
                .thenReturn(Optional.empty());
        Semester semester = new Semester();
        semester.setId("1");
        semester.setCode("SPRING-2024");
        when(adSemesterRepository.save(any(Semester.class))).thenReturn(semester);

        ResponseEntity<?> response = adSemesterService.createSemester(request);
        assertEquals(200, response.getStatusCodeValue());
        verify(adSemesterRepository).save(any(Semester.class));
        verify(userActivityLogHelper).saveLog(contains("vừa thêm 1 học kỳ mới"));
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    void testUpdateSemesterNotFound() {
        ADCreateUpdateSemesterRequest request = new ADCreateUpdateSemesterRequest();
        request.setSemesterId("1");
        when(adSemesterRepository.findById("1")).thenReturn(Optional.empty());
        ResponseEntity<?> response = adSemesterService.updateSemester(request);
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void testGetSemesterByIdFound() {
        String semesterId = "1";
        Semester semester = new Semester();
        semester.setId(semesterId);
        when(adSemesterRepository.findById(semesterId)).thenReturn(Optional.of(semester));
        ResponseEntity<?> response = adSemesterService.getSemesterById(semesterId);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetSemesterByIdNotFound() {
        String semesterId = "1";
        when(adSemesterRepository.findById(semesterId)).thenReturn(Optional.empty());
        ResponseEntity<?> response = adSemesterService.getSemesterById(semesterId);
        assertEquals(400, response.getStatusCodeValue());
    }
}