package udpm.hn.studentattendance.core.admin.subject.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.subject.model.request.ADSubjectCreateRequest;
import udpm.hn.studentattendance.core.admin.subject.model.request.ADSubjectSearchRequest;
import udpm.hn.studentattendance.core.admin.subject.model.request.ADSubjectUpdateRequest;
import udpm.hn.studentattendance.core.admin.subject.repository.ADSubjectExtendRepository;
import udpm.hn.studentattendance.entities.Subject;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;

import udpm.hn.studentattendance.infrastructure.config.redis.service.RedisService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ADSubjectManagementServiceTest {
    @Mock
    private ADSubjectExtendRepository adminSubjectRepository;

    @Mock
    private UserActivityLogHelper userActivityLogHelper;
    @Mock
    private RedisService redisService;
    @Mock
    private RedisInvalidationHelper redisInvalidationHelper;

    @InjectMocks
    private udpm.hn.studentattendance.core.admin.subject.service.impl.ADSubjectManagementServiceImpl adSubjectManagementService;

    @BeforeEach
    void setUp() {
        // Có thể set các giá trị field nếu cần
    }

    @Test
    void testCreateSubjectSuccess() {
        ADSubjectCreateRequest request = new ADSubjectCreateRequest();
        request.setName("Toán cao cấp");
        request.setCode("MATH101");
        when(adminSubjectRepository.isExistsCodeSubject(anyString(), isNull())).thenReturn(false);
        when(adminSubjectRepository.isExistsNameSubject(anyString(), isNull())).thenReturn(false);
        Subject subject = new Subject();
        subject.setId("1");
        subject.setName("Toán cao cấp");
        subject.setCode("MATH101");
        when(adminSubjectRepository.save(any(Subject.class))).thenReturn(subject);
        ResponseEntity<?> response = adSubjectManagementService.createSubject(request);
        assertEquals(200, response.getStatusCodeValue());
        verify(adminSubjectRepository).save(any(Subject.class));
        verify(userActivityLogHelper).saveLog(contains("vừa thêm 1 bộ môn mới"));
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    void testUpdateSubjectNotFound() {
        String id = "1";
        ADSubjectUpdateRequest request = new ADSubjectUpdateRequest();
        request.setName("Toán cao cấp");
        request.setCode("MATH101");
        when(adminSubjectRepository.findById(id)).thenReturn(Optional.empty());
        ResponseEntity<?> response = adSubjectManagementService.updateSubject(id, request);
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void testDetailSubjectSuccess() {
        String id = "1";
        Subject subject = new Subject();
        subject.setId(id);
        subject.setName("Toán cao cấp");
        subject.setCode("MATH101");
        // Only stub repository, as redisService.get is not used in the actual service
        // for this test
        when(adminSubjectRepository.findById(id)).thenReturn(Optional.of(subject));
        ResponseEntity<?> response = adSubjectManagementService.detailSubject(id);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testChangeStatusNotFound() {
        String id = "1";
        when(adminSubjectRepository.findById(id)).thenReturn(Optional.empty());
        ResponseEntity<?> response = adSubjectManagementService.changeStatus(id);
        assertEquals(400, response.getStatusCodeValue());
    }
}
