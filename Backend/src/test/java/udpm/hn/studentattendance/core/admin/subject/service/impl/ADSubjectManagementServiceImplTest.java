package udpm.hn.studentattendance.core.admin.subject.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import udpm.hn.studentattendance.core.admin.subject.model.request.ADSubjectCreateRequest;
import udpm.hn.studentattendance.core.admin.subject.model.request.ADSubjectSearchRequest;
import udpm.hn.studentattendance.core.admin.subject.model.request.ADSubjectUpdateRequest;
import udpm.hn.studentattendance.core.admin.subject.model.response.ADSubjectResponse;
import udpm.hn.studentattendance.core.admin.subject.repository.ADSubjectExtendRepository;
import udpm.hn.studentattendance.entities.Subject;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonUserStudentRepository;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ADSubjectManagementServiceImplTest {

    @Mock
    private ADSubjectExtendRepository adminSubjectRepository;

    @Mock
    private CommonUserStudentRepository commonUserStudentRepository;

    @Mock
    private UserActivityLogHelper userActivityLogHelper;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private ADSubjectManagementServiceImpl subjectService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(subjectService, "redisTTL", 3600L);
    }

    @Test
    @DisplayName("Test getListSubject should return data from cache if available")
    void testGetListSubjectFromCache() {
        // Given
        ADSubjectSearchRequest request = new ADSubjectSearchRequest();
        String cacheKey = "admin:subject:list:" + request.toString();
        PageableObject mockData = mock(PageableObject.class);

        when(redisService.get(cacheKey)).thenReturn(mockData);

        // When
        ResponseEntity<?> response = subjectService.getListSubject(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy danh sách bộ môn thành công (cached)", apiResponse.getMessage());
        assertEquals(mockData, apiResponse.getData());

        // Verify repository was not called
        verify(adminSubjectRepository, never()).getAll(any(Pageable.class), any(ADSubjectSearchRequest.class));
    }

    @Test
    @DisplayName("Test getListSubject should fetch and cache data if not in cache")
    void testGetListSubjectFromRepository() {
        // Given
        ADSubjectSearchRequest request = new ADSubjectSearchRequest();
        String cacheKey = "admin:subject:list:" + request.toString();

        List<ADSubjectResponse> subjects = new ArrayList<>();
        ADSubjectResponse subject = mock(ADSubjectResponse.class);
        subjects.add(subject);
        Page<ADSubjectResponse> page = new PageImpl<>(subjects);

        when(redisService.get(cacheKey)).thenReturn(null);
        when(adminSubjectRepository.getAll(any(Pageable.class), eq(request))).thenReturn(page);

        // When
        ResponseEntity<?> response = subjectService.getListSubject(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy danh sách bộ môn thành công", apiResponse.getMessage());

        // Verify repository was called and cache was updated
        verify(adminSubjectRepository).getAll(any(Pageable.class), eq(request));
        verify(redisService).set(eq(cacheKey), any(PageableObject.class), eq(3600L * 2));
    }

    @Test
    @DisplayName("Test createSubject should create subject successfully")
    void testCreateSubjectSuccess() {
        // Given
        ADSubjectCreateRequest request = mock(ADSubjectCreateRequest.class);
        when(request.getName()).thenReturn("Java Programming");
        when(request.getCode()).thenReturn("JAVA");

        when(adminSubjectRepository.isExistsCodeSubject("JAVA", null)).thenReturn(false);
        when(adminSubjectRepository.isExistsNameSubject("Java Programming", null)).thenReturn(false);

        Subject savedSubject = new Subject();
        savedSubject.setId("new-subject-id");
        savedSubject.setName("Java Programming");
        savedSubject.setCode("JAVA");
        savedSubject.setStatus(EntityStatus.ACTIVE);

        when(adminSubjectRepository.save(any(Subject.class))).thenReturn(savedSubject);

        // When
        ResponseEntity<?> response = subjectService.createSubject(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Thêm mới bộ môn thành công", apiResponse.getMessage());
        assertEquals(savedSubject, apiResponse.getData());

        // Verify repository was called
        verify(adminSubjectRepository).save(any(Subject.class));
        verify(userActivityLogHelper).saveLog(contains("vừa thêm 1 bộ môn mới"));
        verify(redisService).deletePattern("admin:subject:list:*");
    }

    @Test
    @DisplayName("Test createSubject should return error for invalid code")
    void testCreateSubjectInvalidCode() {
        // Given
        ADSubjectCreateRequest request = mock(ADSubjectCreateRequest.class);
        when(request.getName()).thenReturn("Java Programming");
        when(request.getCode()).thenReturn("JAVA@123"); // Invalid code with special character

        // When
        ResponseEntity<?> response = subjectService.createSubject(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.getMessage().contains("Mã bộ môn không hợp lệ"));

        // Verify repository was not called
        verify(adminSubjectRepository, never()).save(any(Subject.class));
    }

    @Test
    @DisplayName("Test createSubject should return error if code already exists")
    void testCreateSubjectCodeExists() {
        // Given
        ADSubjectCreateRequest request = mock(ADSubjectCreateRequest.class);
        when(request.getName()).thenReturn("Java Programming");
        when(request.getCode()).thenReturn("JAVA");

        when(adminSubjectRepository.isExistsCodeSubject("JAVA", null)).thenReturn(true);

        // When
        ResponseEntity<?> response = subjectService.createSubject(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Mã bộ môn đã tồn tại trên hệ thống", apiResponse.getMessage());

        // Verify repository was not called to save
        verify(adminSubjectRepository, never()).save(any(Subject.class));
    }

    @Test
    @DisplayName("Test createSubject should return error if name already exists")
    void testCreateSubjectNameExists() {
        // Given
        ADSubjectCreateRequest request = mock(ADSubjectCreateRequest.class);
        when(request.getName()).thenReturn("Java Programming");
        when(request.getCode()).thenReturn("JAVA");

        when(adminSubjectRepository.isExistsCodeSubject("JAVA", null)).thenReturn(false);
        when(adminSubjectRepository.isExistsNameSubject("Java Programming", null)).thenReturn(true);

        // When
        ResponseEntity<?> response = subjectService.createSubject(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Tên bộ môn đã tồn tại trên hệ thống", apiResponse.getMessage());

        // Verify repository was not called to save
        verify(adminSubjectRepository, never()).save(any(Subject.class));
    }

    @Test
    @DisplayName("Test updateSubject should update subject successfully")
    void testUpdateSubjectSuccess() {
        // Given
        String subjectId = "subject-1";
        ADSubjectUpdateRequest request = mock(ADSubjectUpdateRequest.class);
        when(request.getName()).thenReturn("Java Programming Updated");
        when(request.getCode()).thenReturn("JAVA_UPDATED");

        Subject existingSubject = new Subject();
        existingSubject.setId(subjectId);
        existingSubject.setName("Java Programming");
        existingSubject.setCode("JAVA");
        existingSubject.setStatus(EntityStatus.ACTIVE);

        when(adminSubjectRepository.findById(subjectId)).thenReturn(Optional.of(existingSubject));
        when(adminSubjectRepository.isExistsCodeSubject("JAVA_UPDATED", subjectId)).thenReturn(false);
        when(adminSubjectRepository.isExistsNameSubject("Java Programming Updated", subjectId)).thenReturn(false);
        when(adminSubjectRepository.save(any(Subject.class))).thenReturn(existingSubject);

        // When
        ResponseEntity<?> response = subjectService.updateSubject(subjectId, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Cập nhật bộ môn thành công", apiResponse.getMessage());

        // Verify subject was updated correctly
        assertEquals("Java Programming Updated", existingSubject.getName());
        assertEquals("JAVA_UPDATED", existingSubject.getCode());

        // Verify repository was called
        verify(adminSubjectRepository).save(existingSubject);
        verify(userActivityLogHelper).saveLog(contains("vừa cập nhật 1 bộ môn"));
        verify(redisService).delete("admin:subject:detail:" + subjectId);
        verify(redisService).deletePattern("admin:subject:list:*");
    }

    @Test
    @DisplayName("Test updateSubject should return error if subject not found")
    void testUpdateSubjectNotFound() {
        // Given
        String subjectId = "non-existent-id";
        ADSubjectUpdateRequest request = mock(ADSubjectUpdateRequest.class);
        when(request.getName()).thenReturn("Java Programming Updated");
        when(request.getCode()).thenReturn("JAVA_UPDATED");

        when(adminSubjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = subjectService.updateSubject(subjectId, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Không tìm thấy bộ môn", apiResponse.getMessage());

        // Verify repository was not called to save
        verify(adminSubjectRepository, never()).save(any(Subject.class));
    }

    @Test
    @DisplayName("Test detailSubject should return subject from cache if available")
    void testDetailSubjectFromCache() {
        // Given
        String subjectId = "subject-1";
        String cacheKey = "admin:subject:detail:" + subjectId;
        Subject cachedSubject = new Subject();
        cachedSubject.setId(subjectId);

        when(redisService.get(cacheKey)).thenReturn(cachedSubject);

        // When
        ResponseEntity<?> response = subjectService.detailSubject(subjectId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy thông tin bộ môn thành công (cached)", apiResponse.getMessage());
        assertEquals(cachedSubject, apiResponse.getData());

        // Verify repository was not called
        verify(adminSubjectRepository, never()).findById(subjectId);
    }

    @Test
    @DisplayName("Test detailSubject should fetch and cache data if not in cache")
    void testDetailSubjectFromRepository() {
        // Given
        String subjectId = "subject-1";
        String cacheKey = "admin:subject:detail:" + subjectId;

        Subject subject = new Subject();
        subject.setId(subjectId);
        subject.setName("Java Programming");
        subject.setCode("JAVA");
        subject.setStatus(EntityStatus.ACTIVE);

        when(redisService.get(cacheKey)).thenReturn(null);
        when(adminSubjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));

        // When
        ResponseEntity<?> response = subjectService.detailSubject(subjectId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy thông tin bộ môn thành công", apiResponse.getMessage());
        assertEquals(subject, apiResponse.getData());

        // Verify repository was called and cache was updated
        verify(adminSubjectRepository).findById(subjectId);
        verify(redisService).set(eq(cacheKey), eq(subject), eq(3600L * 3));
    }

    @Test
    @DisplayName("Test detailSubject should return error if subject not found")
    void testDetailSubjectNotFound() {
        // Given
        String subjectId = "non-existent-id";
        String cacheKey = "admin:subject:detail:" + subjectId;

        when(redisService.get(cacheKey)).thenReturn(null);
        when(adminSubjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = subjectService.detailSubject(subjectId);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Không tìm thấy bộ môn", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test changeStatus should toggle subject status successfully")
    void testChangeStatusSuccess() {
        // Given
        String subjectId = "subject-1";

        Subject subject = new Subject();
        subject.setId(subjectId);
        subject.setName("Java Programming");
        subject.setCode("JAVA");
        subject.setStatus(EntityStatus.ACTIVE);

        when(adminSubjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(adminSubjectRepository.save(any(Subject.class))).thenReturn(subject);

        // When
        ResponseEntity<?> response = subjectService.changeStatus(subjectId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Đổi trạng thái bộ môn thành công", apiResponse.getMessage());

        // Verify status was changed to INACTIVE
        assertEquals(EntityStatus.INACTIVE, subject.getStatus());

        // Verify repository was called
        verify(adminSubjectRepository).save(subject);
        verify(userActivityLogHelper).saveLog(contains("vừa thay đổi trạng thái 1 bộ môn"));
        verify(redisService).delete("admin:subject:detail:" + subjectId);
        verify(redisService).deletePattern("admin:subject:list:*");
    }

    @Test
    @DisplayName("Test changeStatus should call disableAllStudentDuplicateShiftByIdSubject when status changes to ACTIVE")
    void testChangeStatusToActiveCallsDisable() {
        // Given
        String subjectId = "subject-1";

        Subject subject = new Subject();
        subject.setId(subjectId);
        subject.setName("Java Programming");
        subject.setCode("JAVA");
        subject.setStatus(EntityStatus.INACTIVE);

        when(adminSubjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(adminSubjectRepository.save(any(Subject.class))).thenReturn(subject);

        // When
        ResponseEntity<?> response = subjectService.changeStatus(subjectId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify status was changed to ACTIVE
        assertEquals(EntityStatus.ACTIVE, subject.getStatus());

        // Verify disableAllStudentDuplicateShiftByIdSubject was called
        verify(commonUserStudentRepository).disableAllStudentDuplicateShiftByIdSubject(subjectId);
    }

    @Test
    @DisplayName("Test changeStatus should return error if subject not found")
    void testChangeStatusNotFound() {
        // Given
        String subjectId = "non-existent-id";

        when(adminSubjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = subjectService.changeStatus(subjectId);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Không tìm thấy bộ môn", apiResponse.getMessage());

        // Verify repository was not called to save
        verify(adminSubjectRepository, never()).save(any(Subject.class));
    }
}