package udpm.hn.studentattendance.core.admin.subjectfacility.service.impl;

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
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.ADSubjectFacilityCreateRequest;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.ADSubjectFacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.ADSubjectFacilityUpdateRequest;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.response.ADSubjectFacilityResponse;
import udpm.hn.studentattendance.core.admin.subjectfacility.repository.ADFacilityRepository;
import udpm.hn.studentattendance.core.admin.subjectfacility.repository.ADSubjectFacilityRepository;
import udpm.hn.studentattendance.core.admin.subjectfacility.repository.ADSubjectRepository;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.Subject;
import udpm.hn.studentattendance.entities.SubjectFacility;
import udpm.hn.studentattendance.helpers.RedisCacheHelper;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.config.redis.service.RedisService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ADSubjectFacilityServiceImplTest {

    @Mock
    private ADSubjectFacilityRepository repository;

    @Mock
    private ADSubjectRepository subjectRepository;

    @Mock
    private ADFacilityRepository facilityRepository;

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
    private ADSubjectFacilityServiceImpl subjectFacilityService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(subjectFacilityService, "redisTTL", 3600L);
        // Removed unnecessary stubbing for redisCacheHelper.getOrSet
    }

    @Test
    @DisplayName("Test getListSubjectFacility should return data successfully")
    void testGetListSubjectFacility() {
        ADSubjectFacilitySearchRequest request = new ADSubjectFacilitySearchRequest();
        List<ADSubjectFacilityResponse> responses = new ArrayList<>();
        ADSubjectFacilityResponse response = mock(ADSubjectFacilityResponse.class);
        responses.add(response);
        Page<ADSubjectFacilityResponse> page = new PageImpl<>(responses);

        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong()))
                .thenAnswer(invocation -> {
                    java.util.function.Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        when(repository.getAll(any(Pageable.class), eq(request))).thenReturn(page);

        // When
        ResponseEntity<?> result = subjectFacilityService.getListSubjectFacility(request);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) result.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lây danh sách bộ môn cơ sở thành công", apiResponse.getMessage());

        // Verify repository was called and cache was updated
        verify(redisCacheHelper).getOrSet(anyString(), any(), any(), anyLong());
        verify(repository).getAll(any(Pageable.class), eq(request));
    }

    @Test
    @DisplayName("Test createSubjectFacility should create subject facility successfully")
    void testCreateSubjectFacilitySuccess() {
        // Given
        ADSubjectFacilityCreateRequest request = new ADSubjectFacilityCreateRequest();
        request.setFacilityId("facility-1");
        request.setSubjectId("subject-1");

        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setName("FPT HCM");
        facility.setStatus(EntityStatus.ACTIVE);

        Subject subject = new Subject();
        subject.setId("subject-1");
        subject.setName("Java Programming");
        subject.setStatus(EntityStatus.ACTIVE);

        when(facilityRepository.findById("facility-1")).thenReturn(Optional.of(facility));
        when(subjectRepository.findById("subject-1")).thenReturn(Optional.of(subject));
        when(repository.isExistsSubjectFacility("facility-1", "subject-1", null)).thenReturn(false);

        SubjectFacility savedSubjectFacility = new SubjectFacility();
        savedSubjectFacility.setId("subject-facility-1");
        savedSubjectFacility.setFacility(facility);
        savedSubjectFacility.setSubject(subject);
        savedSubjectFacility.setStatus(EntityStatus.ACTIVE);

        when(repository.save(any(SubjectFacility.class))).thenReturn(savedSubjectFacility);

        // When
        ResponseEntity<?> result = subjectFacilityService.createSubjectFacility(request);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) result.getBody();
        assertNotNull(apiResponse);
        assertEquals("Thêm bộ môn cơ sở thành công", apiResponse.getMessage());
        assertEquals(savedSubjectFacility, apiResponse.getData());

        // Verify repository was called
        verify(repository).save(any(SubjectFacility.class));
        verify(userActivityLogHelper).saveLog(contains("vừa thêm mới bộ môn cơ sở"));
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test createSubjectFacility should return error if facility not found")
    void testCreateSubjectFacilityFacilityNotFound() {
        // Given
        ADSubjectFacilityCreateRequest request = new ADSubjectFacilityCreateRequest();
        request.setFacilityId("facility-1");
        request.setSubjectId("subject-1");

        when(facilityRepository.findById("facility-1")).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> result = subjectFacilityService.createSubjectFacility(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) result.getBody();
        assertNotNull(apiResponse);
        assertEquals("Không tìm thấy cơ sở", apiResponse.getMessage());

        // Verify repository was not called
        verify(repository, never()).save(any(SubjectFacility.class));
        verify(redisInvalidationHelper, never()).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test createSubjectFacility should return error if subject not found")
    void testCreateSubjectFacilitySubjectNotFound() {
        // Given
        ADSubjectFacilityCreateRequest request = new ADSubjectFacilityCreateRequest();
        request.setFacilityId("facility-1");
        request.setSubjectId("subject-1");

        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setName("FPT HCM");
        facility.setStatus(EntityStatus.ACTIVE);

        when(facilityRepository.findById("facility-1")).thenReturn(Optional.of(facility));
        when(subjectRepository.findById("subject-1")).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> result = subjectFacilityService.createSubjectFacility(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) result.getBody();
        assertNotNull(apiResponse);
        assertEquals("Không tìm thấy bộ môn", apiResponse.getMessage());

        // Verify repository was not called
        verify(repository, never()).save(any(SubjectFacility.class));
        verify(redisInvalidationHelper, never()).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test createSubjectFacility should return error if subject facility already exists")
    void testCreateSubjectFacilityAlreadyExists() {
        // Given
        ADSubjectFacilityCreateRequest request = new ADSubjectFacilityCreateRequest();
        request.setFacilityId("facility-1");
        request.setSubjectId("subject-1");

        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setName("FPT HCM");
        facility.setStatus(EntityStatus.ACTIVE);

        Subject subject = new Subject();
        subject.setId("subject-1");
        subject.setName("Java Programming");
        subject.setStatus(EntityStatus.ACTIVE);

        when(facilityRepository.findById("facility-1")).thenReturn(Optional.of(facility));
        when(subjectRepository.findById("subject-1")).thenReturn(Optional.of(subject));
        when(repository.isExistsSubjectFacility("facility-1", "subject-1", null)).thenReturn(true);

        // When
        ResponseEntity<?> result = subjectFacilityService.createSubjectFacility(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) result.getBody();
        assertNotNull(apiResponse);
        assertEquals("Bộ môn đã tồn tại trong cơ sở này", apiResponse.getMessage());

        // Verify repository was not called
        verify(repository, never()).save(any(SubjectFacility.class));
        verify(redisInvalidationHelper, never()).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test updateSubjectFacility should update subject facility successfully")
    void testUpdateSubjectFacilitySuccess() {
        // Given
        String id = "subject-facility-1";
        ADSubjectFacilityUpdateRequest request = new ADSubjectFacilityUpdateRequest();
        request.setFacilityId("facility-2");
        request.setSubjectId("subject-2");

        Facility oldFacility = new Facility();
        oldFacility.setId("facility-1");
        oldFacility.setName("FPT HCM");

        Subject oldSubject = new Subject();
        oldSubject.setId("subject-1");
        oldSubject.setName("Java Programming");

        SubjectFacility existingSubjectFacility = new SubjectFacility();
        existingSubjectFacility.setId(id);
        existingSubjectFacility.setFacility(oldFacility);
        existingSubjectFacility.setSubject(oldSubject);
        existingSubjectFacility.setStatus(EntityStatus.ACTIVE);

        Facility newFacility = new Facility();
        newFacility.setId("facility-2");
        newFacility.setName("FPT HN");
        newFacility.setStatus(EntityStatus.ACTIVE);

        Subject newSubject = new Subject();
        newSubject.setId("subject-2");
        newSubject.setName("Python Programming");
        newSubject.setStatus(EntityStatus.ACTIVE);

        when(repository.findById(id)).thenReturn(Optional.of(existingSubjectFacility));
        when(facilityRepository.findById("facility-2")).thenReturn(Optional.of(newFacility));
        when(subjectRepository.findById("subject-2")).thenReturn(Optional.of(newSubject));
        when(repository.isExistsSubjectFacility("facility-2", "subject-2", id)).thenReturn(false);

        SubjectFacility updatedSubjectFacility = new SubjectFacility();
        updatedSubjectFacility.setId(id);
        updatedSubjectFacility.setFacility(newFacility);
        updatedSubjectFacility.setSubject(newSubject);
        updatedSubjectFacility.setStatus(EntityStatus.ACTIVE);

        when(repository.save(any(SubjectFacility.class))).thenReturn(updatedSubjectFacility);

        // When
        ResponseEntity<?> result = subjectFacilityService.updateSubjectFacility(id, request);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) result.getBody();
        assertNotNull(apiResponse);
        assertEquals("Cập nhật bộ môn cơ sở thành công", apiResponse.getMessage());
        assertEquals(updatedSubjectFacility, apiResponse.getData());

        // Verify repository was called
        verify(repository).save(any(SubjectFacility.class));
        verify(userActivityLogHelper).saveLog(contains("vừa cập nhật bộ môn cơ sở"));
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test detailSubjectFacility should return subject facility detail successfully")
    void testDetailSubjectFacilitySuccess() {
        // Given
        String id = "subject-facility-1";
        ADSubjectFacilityResponse response = mock(ADSubjectFacilityResponse.class);
        when(repository.getOneById(id)).thenReturn(Optional.of(response));

        // When
        ResponseEntity<?> result = subjectFacilityService.detailSubjectFacility(id);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) result.getBody();
        assertNotNull(apiResponse);
        assertEquals("Lấy thông tin bộ môn cơ sở thành công", apiResponse.getMessage());
        assertEquals(response, apiResponse.getData());
    }

    @Test
    @DisplayName("Test detailSubjectFacility should return error if subject facility not found")
    void testDetailSubjectFacilityNotFound() {
        // Given
        String id = "non-existent-id";
        when(repository.getOneById(id)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> result = subjectFacilityService.detailSubjectFacility(id);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) result.getBody();
        assertNotNull(apiResponse);
        assertEquals("Không tìm thấy bộ môn cơ sở", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test changeStatus should toggle subject facility status successfully")
    void testChangeStatusSuccess() {
        // Given
        String id = "subject-facility-1";

        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setName("FPT HCM");

        Subject subject = new Subject();
        subject.setId("subject-1");
        subject.setName("Java Programming");

        SubjectFacility subjectFacility = new SubjectFacility();
        subjectFacility.setId(id);
        subjectFacility.setFacility(facility);
        subjectFacility.setSubject(subject);
        subjectFacility.setStatus(EntityStatus.ACTIVE);

        when(repository.findById(id)).thenReturn(Optional.of(subjectFacility));
        when(repository.save(any(SubjectFacility.class))).thenReturn(subjectFacility);

        // When
        ResponseEntity<?> result = subjectFacilityService.changeStatus(id);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) result.getBody();
        assertNotNull(apiResponse);
        assertEquals("Thay đổi trạng thái thành công", apiResponse.getMessage());

        // Verify status was changed to INACTIVE
        assertEquals(EntityStatus.INACTIVE, subjectFacility.getStatus());

        // Verify repository was called
        verify(repository).save(subjectFacility);
        verify(userActivityLogHelper).saveLog(contains("vừa thay đổi trạng thái bộ môn cơ sở"));
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test changeStatus should call disableAllStudentDuplicateShiftByIdSubjectFacility when status changes to ACTIVE")
    void testChangeStatusToActiveCallsDisable() {
        // Given
        String id = "subject-facility-1";

        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setName("FPT HCM");

        Subject subject = new Subject();
        subject.setId("subject-1");
        subject.setName("Java Programming");

        SubjectFacility subjectFacility = new SubjectFacility();
        subjectFacility.setId(id);
        subjectFacility.setFacility(facility);
        subjectFacility.setSubject(subject);
        subjectFacility.setStatus(EntityStatus.INACTIVE);

        when(repository.findById(id)).thenReturn(Optional.of(subjectFacility));
        when(repository.save(any(SubjectFacility.class))).thenReturn(subjectFacility);

        // When
        ResponseEntity<?> result = subjectFacilityService.changeStatus(id);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());

        // Verify status was changed to ACTIVE
        assertEquals(EntityStatus.ACTIVE, subjectFacility.getStatus());

        // Verify disableAllStudentDuplicateShiftByIdSubjectFacility was called
        verify(commonUserStudentRepository).disableAllStudentDuplicateShiftByIdSubjectFacility(id);
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test changeStatus should return error if subject facility not found")
    void testChangeStatusNotFound() {
        // Given
        String id = "non-existent-id";

        when(repository.findById(id)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> result = subjectFacilityService.changeStatus(id);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) result.getBody();
        assertNotNull(apiResponse);
        assertEquals("Không tìm thấy bộ môn cơ sở", apiResponse.getMessage());

        // Verify repository was not called to save
        verify(repository, never()).save(any(SubjectFacility.class));
        verify(redisInvalidationHelper, never()).invalidateAllCaches();
    }
}