package udpm.hn.studentattendance.core.admin.semester.service.impl;

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
import udpm.hn.studentattendance.core.admin.semester.model.request.ADCreateUpdateSemesterRequest;
import udpm.hn.studentattendance.core.admin.semester.model.request.ADSemesterRequest;
import udpm.hn.studentattendance.core.admin.semester.model.response.ADSemesterResponse;
import udpm.hn.studentattendance.core.admin.semester.repository.ADSemesterRepository;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.helpers.RedisCacheHelper;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonUserStudentRepository;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.SemesterName;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ADSemesterServiceImplTest {

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

    @Mock
    private RedisCacheHelper redisCacheHelper;

    @InjectMocks
    private ADSemesterServiceImpl adSemesterService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adSemesterService, "redisTTL", 3600L);
        // Removed unnecessary stubbing for redisCacheHelper.getOrSet
    }

    @Test
    @DisplayName("Test getAllSemester should return paginated semesters")
    void testGetAllSemester() {
        // Given
        ADSemesterRequest request = new ADSemesterRequest();
        List<ADSemesterResponse> semesters = new ArrayList<>();

        ADSemesterResponse semesterResponse = mock(ADSemesterResponse.class);
        semesters.add(semesterResponse);

        Page<ADSemesterResponse> page = new PageImpl<>(semesters);

        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong()))
                .thenAnswer(invocation -> {
                    java.util.function.Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        when(adSemesterRepository.getAllSemester(any(Pageable.class), eq(request))).thenReturn(page);

        // When
        ResponseEntity<?> response = adSemesterService.getAllSemester(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Hiển thị tất cả học kỳ thành công", apiResponse.getMessage());

        PageableObject pageableObject = (PageableObject) apiResponse.getData();
        assertNotNull(pageableObject);
        assertEquals(1, pageableObject.getData().size());
    }

    @Test
    @DisplayName("Test getSemesterById should return semester when found")
    void testGetSemesterByIdFound() {
        // Given
        String semesterId = "semester-1";
        Semester semester = new Semester();
        semester.setId(semesterId);
        semester.setCode("SPRING-2023");

        when(adSemesterRepository.findById(semesterId)).thenReturn(Optional.of(semester));

        // When
        ResponseEntity<?> response = adSemesterService.getSemesterById(semesterId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Tìm học kỳ thành công", apiResponse.getMessage());
        assertEquals(semester, apiResponse.getData());
    }

    @Test
    @DisplayName("Test getSemesterById should return error when semester not found")
    void testGetSemesterByIdNotFound() {
        // Given
        String semesterId = "non-existent-id";
        when(adSemesterRepository.findById(semesterId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = adSemesterService.getSemesterById(semesterId);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Học kỳ không tồn tại", apiResponse.getMessage());
    }

    @Test
    @DisplayName("Test createSemester should create semester successfully")
    void testCreateSemesterSuccess() {
        // Given
        ADCreateUpdateSemesterRequest request = new ADCreateUpdateSemesterRequest();
        request.setSemesterName("SPRING");

        // Set dates to future (3 months apart)
        LocalDateTime fromDate = LocalDateTime.now().plusDays(1);
        LocalDateTime toDate = fromDate.plusMonths(4);

        Long fromDateEpoch = fromDate.toInstant(ZoneOffset.UTC).toEpochMilli();
        Long toDateEpoch = toDate.toInstant(ZoneOffset.UTC).toEpochMilli();

        request.setFromDate(fromDateEpoch);
        request.setToDate(toDateEpoch);

        // Create real instances for the custom time methods
        final ADCreateUpdateSemesterRequest finalRequest = request;
        ADCreateUpdateSemesterRequest spyRequest = spy(new ADCreateUpdateSemesterRequest() {
            @Override
            public Long getStartTimeCustom() {
                return finalRequest.getFromDate();
            }

            @Override
            public Long getEndTimeCustom() {
                return finalRequest.getToDate();
            }
        });
        spyRequest.setFromDate(fromDateEpoch);
        spyRequest.setToDate(toDateEpoch);
        spyRequest.setSemesterName("SPRING");

        when(adSemesterRepository.checkConflictTime(anyLong(), anyLong())).thenReturn(Collections.emptyList());
        when(adSemesterRepository.checkSemesterExistNameAndYear(anyString(), anyInt(), any(EntityStatus.class),
                isNull())).thenReturn(Optional.empty());

        Semester savedSemester = new Semester();
        savedSemester.setId("new-semester-id");
        savedSemester.setCode("SPRING-" + fromDate.getYear());
        savedSemester.setSemesterName(SemesterName.SPRING);
        savedSemester.setYear(fromDate.getYear());

        when(adSemesterRepository.save(any(Semester.class))).thenReturn(savedSemester);

        // When
        ResponseEntity<?> response = adSemesterService.createSemester(spyRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Created semester successfully", apiResponse.getMessage());

        verify(adSemesterRepository).save(any(Semester.class));
        verify(userActivityLogHelper).saveLog(contains("vừa thêm 1 học kỳ mới"));
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test createSemester should return error when semester already exists")
    void testCreateSemesterAlreadyExists() {
        // Given
        ADCreateUpdateSemesterRequest request = new ADCreateUpdateSemesterRequest();
        request.setSemesterName("SPRING");

        // Set dates to future (3 months apart)
        LocalDateTime fromDate = LocalDateTime.now().plusDays(1);
        LocalDateTime toDate = fromDate.plusMonths(4);

        Long fromDateEpoch = fromDate.toInstant(ZoneOffset.UTC).toEpochMilli();
        Long toDateEpoch = toDate.toInstant(ZoneOffset.UTC).toEpochMilli();

        request.setFromDate(fromDateEpoch);
        request.setToDate(toDateEpoch);

        // Create real instances for the custom time methods
        final ADCreateUpdateSemesterRequest finalRequest = request;
        ADCreateUpdateSemesterRequest spyRequest = spy(new ADCreateUpdateSemesterRequest() {
            @Override
            public Long getStartTimeCustom() {
                return finalRequest.getFromDate();
            }

            @Override
            public Long getEndTimeCustom() {
                return finalRequest.getToDate();
            }
        });
        spyRequest.setFromDate(fromDateEpoch);
        spyRequest.setToDate(toDateEpoch);
        spyRequest.setSemesterName("SPRING");

        when(adSemesterRepository.checkConflictTime(anyLong(), anyLong())).thenReturn(Collections.emptyList());

        Semester existingSemester = new Semester();
        existingSemester.setId("existing-semester");
        when(adSemesterRepository.checkSemesterExistNameAndYear(anyString(), anyInt(), any(EntityStatus.class),
                isNull())).thenReturn(Optional.of(existingSemester));

        // When
        ResponseEntity<?> response = adSemesterService.createSemester(spyRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Học kỳ đã tồn tại", apiResponse.getMessage());

        verify(adSemesterRepository, never()).save(any(Semester.class));
        verify(redisInvalidationHelper, never()).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test createSemester should return error when time conflict exists")
    void testCreateSemesterTimeConflict() {
        // Given
        ADCreateUpdateSemesterRequest request = new ADCreateUpdateSemesterRequest();
        request.setSemesterName("SPRING");

        // Set dates to future (3 months apart)
        LocalDateTime fromDate = LocalDateTime.now().plusDays(1);
        LocalDateTime toDate = fromDate.plusMonths(4);

        Long fromDateEpoch = fromDate.toInstant(ZoneOffset.UTC).toEpochMilli();
        Long toDateEpoch = toDate.toInstant(ZoneOffset.UTC).toEpochMilli();

        request.setFromDate(fromDateEpoch);
        request.setToDate(toDateEpoch);

        // Create real instances for the custom time methods
        final ADCreateUpdateSemesterRequest finalRequest = request;
        ADCreateUpdateSemesterRequest spyRequest = spy(new ADCreateUpdateSemesterRequest() {
            @Override
            public Long getStartTimeCustom() {
                return finalRequest.getFromDate();
            }

            @Override
            public Long getEndTimeCustom() {
                return finalRequest.getToDate();
            }
        });
        spyRequest.setFromDate(fromDateEpoch);
        spyRequest.setToDate(toDateEpoch);
        spyRequest.setSemesterName("SPRING");

        List<Semester> conflictingSemesters = new ArrayList<>();
        conflictingSemesters.add(new Semester());
        when(adSemesterRepository.checkConflictTime(anyLong(), anyLong())).thenReturn(conflictingSemesters);

        // When
        ResponseEntity<?> response = adSemesterService.createSemester(spyRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Đã có học kỳ trong khoảng thời gian này!", apiResponse.getMessage());

        verify(adSemesterRepository, never()).save(any(Semester.class));
        verify(redisInvalidationHelper, never()).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test updateSemester should update semester successfully")
    void testUpdateSemesterSuccess() {
        // Given
        String semesterId = "semester-1";
        ADCreateUpdateSemesterRequest request = new ADCreateUpdateSemesterRequest();
        request.setSemesterId(semesterId);
        request.setSemesterName("SUMMER");

        // Set dates to future (3 months apart)
        LocalDateTime fromDate = LocalDateTime.now().plusDays(1);
        LocalDateTime toDate = fromDate.plusMonths(4);

        Long fromDateEpoch = fromDate.toInstant(ZoneOffset.UTC).toEpochMilli();
        Long toDateEpoch = toDate.toInstant(ZoneOffset.UTC).toEpochMilli();

        request.setFromDate(fromDateEpoch);
        request.setToDate(toDateEpoch);

        // Create real instances for the custom time methods
        final ADCreateUpdateSemesterRequest finalRequest = request;
        ADCreateUpdateSemesterRequest spyRequest = spy(new ADCreateUpdateSemesterRequest() {
            @Override
            public Long getStartTimeCustom() {
                return finalRequest.getFromDate();
            }

            @Override
            public Long getEndTimeCustom() {
                return finalRequest.getToDate();
            }
        });
        spyRequest.setSemesterId(semesterId);
        spyRequest.setFromDate(fromDateEpoch);
        spyRequest.setToDate(toDateEpoch);
        spyRequest.setSemesterName("SUMMER");

        Semester existingSemester = new Semester();
        existingSemester.setId(semesterId);
        existingSemester.setCode("SPRING-" + fromDate.getYear());
        existingSemester.setSemesterName(SemesterName.SPRING);
        existingSemester.setYear(fromDate.getYear());
        existingSemester.setFromDate(fromDate.toInstant(ZoneOffset.UTC).toEpochMilli());
        existingSemester.setToDate(toDate.toInstant(ZoneOffset.UTC).toEpochMilli());

        when(adSemesterRepository.findById(semesterId)).thenReturn(Optional.of(existingSemester));
        when(adSemesterRepository.checkSemesterExistNameAndYear(anyString(), anyInt(), any(EntityStatus.class),
                eq(semesterId))).thenReturn(Optional.empty());
        when(adSemesterRepository.checkConflictTime(anyLong(), anyLong())).thenReturn(Collections.emptyList());

        Semester updatedSemester = new Semester();
        updatedSemester.setId(semesterId);
        updatedSemester.setCode("SUMMER-" + fromDate.getYear());
        updatedSemester.setSemesterName(SemesterName.SUMMER);
        updatedSemester.setYear(fromDate.getYear());

        when(adSemesterRepository.save(any(Semester.class))).thenReturn(updatedSemester);

        // When
        ResponseEntity<?> response = adSemesterService.updateSemester(spyRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Cập nhật thành công", apiResponse.getMessage());

        verify(adSemesterRepository).save(any(Semester.class));
        verify(userActivityLogHelper).saveLog(contains("vừa cập nhật học kỳ"));
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test changeStatusSemester should change status successfully")
    void testChangeStatusSemesterSuccess() {
        // Given
        String semesterId = "semester-1";

        Semester existingSemester = new Semester();
        existingSemester.setId(semesterId);
        existingSemester.setCode("SPRING-2023");
        existingSemester.setStatus(EntityStatus.ACTIVE);

        when(adSemesterRepository.findById(semesterId)).thenReturn(Optional.of(existingSemester));
        when(adSemesterRepository.save(any(Semester.class))).thenReturn(existingSemester);

        // When
        ResponseEntity<?> response = adSemesterService.changeStatusSemester(semesterId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Thay đổi trạng thái học kỳ thành công", apiResponse.getMessage());

        verify(adSemesterRepository).save(any(Semester.class));
        verify(userActivityLogHelper).saveLog(contains("vừa thay đổi trạng thái học kỳ"));
        verify(redisInvalidationHelper).invalidateAllCaches();

        // Verify status was changed from ACTIVE to INACTIVE
        assertEquals(EntityStatus.INACTIVE, existingSemester.getStatus());
    }

    @Test
    @DisplayName("Test changeStatusSemester should return error when semester not found")
    void testChangeStatusSemesterNotFound() {
        // Given
        String semesterId = "non-existent-id";
        when(adSemesterRepository.findById(semesterId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = adSemesterService.changeStatusSemester(semesterId);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Học kỳ không tồn tại", apiResponse.getMessage());

        verify(adSemesterRepository, never()).save(any(Semester.class));
        verify(redisInvalidationHelper, never()).invalidateAllCaches();
    }
}