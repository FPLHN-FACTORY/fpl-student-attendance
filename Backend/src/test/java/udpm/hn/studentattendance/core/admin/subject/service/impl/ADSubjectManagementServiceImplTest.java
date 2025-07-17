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
import udpm.hn.studentattendance.helpers.RedisCacheHelper;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import udpm.hn.studentattendance.infrastructure.config.redis.service.RedisService;

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

    @Mock
    private RedisInvalidationHelper redisInvalidationHelper;

    @Mock
    private RedisCacheHelper redisCacheHelper;

    @InjectMocks
    private ADSubjectManagementServiceImpl subjectService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(subjectService, "redisTTL", 3600L);
        // Removed unnecessary stubbing for redisCacheHelper.getOrSet
    }

    @Test
    @DisplayName("Test getListSubject should return data from cache if available")
    void testGetListSubjectFromCache() {
        // Given
        ADSubjectSearchRequest request = new ADSubjectSearchRequest();
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_SUBJECT + "list_" +
                "page=" + request.getPage() +
                "_size=" + request.getSize() +
                "_orderBy=" + request.getOrderBy() +
                "_sortBy=" + request.getSortBy() +
                "_q=" +
                "_name=" +
                "_status=";
        PageableObject mockData = mock(PageableObject.class);

        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong())).thenReturn(mockData);

        // When
        ResponseEntity<?> response = subjectService.getListSubject(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy danh sách bộ môn thành công", apiResponse.getMessage());
        assertEquals(mockData, apiResponse.getData());

        // Verify repository was not called
        verify(redisCacheHelper).getOrSet(anyString(), any(), any(), anyLong());
        verify(adminSubjectRepository, never()).getAll(any(Pageable.class), any(ADSubjectSearchRequest.class));
    }

    @Test
    @DisplayName("Test getListSubject should fetch and cache data if not in cache")
    void testGetListSubjectFromRepository() {
        // Given
        ADSubjectSearchRequest request = new ADSubjectSearchRequest();
        List<ADSubjectResponse> subjects = new ArrayList<>();
        ADSubjectResponse subject = mock(ADSubjectResponse.class);
        subjects.add(subject);
        Page<ADSubjectResponse> page = new PageImpl<>(subjects);

        // Cache miss: call supplier
        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong()))
                .thenAnswer(invocation -> {
                    java.util.function.Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        // Repository returns Page
        when(adminSubjectRepository.getAll(any(Pageable.class), eq(request))).thenReturn(page);

        // When
        ResponseEntity<?> response = subjectService.getListSubject(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy danh sách bộ môn thành công", apiResponse.getMessage());
        // Verify repository was called and cache was updated
        verify(redisCacheHelper).getOrSet(anyString(), any(), any(), anyLong());
        verify(adminSubjectRepository).getAll(any(Pageable.class), eq(request));
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
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test createSubject should return error for invalid code")
    void testCreateSubjectInvalidCode() {
        // Given
        ADSubjectCreateRequest request = mock(ADSubjectCreateRequest.class);
        // Only stub the code since that's what's being validated first
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
        verify(redisInvalidationHelper, never()).invalidateAllCaches();
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
        verify(redisInvalidationHelper, never()).invalidateAllCaches();
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
        verify(redisInvalidationHelper, never()).invalidateAllCaches();
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
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test updateSubject should return error if subject not found")
    void testUpdateSubjectNotFound() {
        // Given
        String subjectId = "non-existent-id";
        ADSubjectUpdateRequest request = mock(ADSubjectUpdateRequest.class);

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
        verify(redisInvalidationHelper, never()).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test detailSubject should return subject if found")
    void testDetailSubjectFromCache() {
        String subjectId = "subject-1";
        Subject subject = mock(Subject.class);
        when(adminSubjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));

        ResponseEntity<?> response = subjectService.detailSubject(subjectId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy thông tin bộ môn thành công", apiResponse.getMessage());
        assertEquals(subject, apiResponse.getData());
    }

    @Test
    @DisplayName("Test detailSubject should fetch and cache data if not in cache")
    void testDetailSubjectFromRepository() {
        String subjectId = "subject-1";
        Subject subject = mock(Subject.class);
        when(adminSubjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));

        ResponseEntity<?> response = subjectService.detailSubject(subjectId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy thông tin bộ môn thành công", apiResponse.getMessage());
        assertEquals(subject, apiResponse.getData());
    }

    @Test
    @DisplayName("Test detailSubject should return error if subject not found")
    void testDetailSubjectNotFound() {
        String subjectId = "not-found";
        when(adminSubjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = subjectService.detailSubject(subjectId);
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
        verify(redisInvalidationHelper).invalidateAllCaches();
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
        verify(redisInvalidationHelper).invalidateAllCaches();
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
        verify(redisInvalidationHelper, never()).invalidateAllCaches();
    }
}