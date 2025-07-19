package udpm.hn.studentattendance.core.student.schedule.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import udpm.hn.studentattendance.core.student.schedule.model.request.STDScheduleAttendanceSearchRequest;
import udpm.hn.studentattendance.core.student.schedule.model.response.STDScheduleAttendanceResponse;
import udpm.hn.studentattendance.core.student.schedule.repository.STDScheduleAttendanceRepository;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.RedisCacheHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class STDScheduleAttendanceServiceImplTest {

    @Mock
    private STDScheduleAttendanceRepository repository;

    @Mock
    private SessionHelper sessionHelper;

    @Mock
    private RedisService redisService;

    @Mock
    private RedisInvalidationHelper redisInvalidationHelper;

    @Mock
    private RedisCacheHelper redisCacheHelper;

    @Spy
    @InjectMocks
    private STDScheduleAttendanceServiceImpl service;

    private STDScheduleAttendanceSearchRequest request;
    private List<STDScheduleAttendanceResponse> scheduleResponseList;

    // Shared TypeReference instance for mocking
    private final TypeReference<PageableObject<STDScheduleAttendanceResponse>> pageableObjectTypeRef = new TypeReference<PageableObject<STDScheduleAttendanceResponse>>() {
    };

    @BeforeEach
    void setUp() {
        // Set up TTL value using reflection
        ReflectionTestUtils.setField(service, "redisTTL", 3600L);

        // Create a search request
        request = new STDScheduleAttendanceSearchRequest();
        request.setPage(1);
        request.setSize(10);

        // Mock user session data
        when(sessionHelper.getUserId()).thenReturn("user123");
        when(sessionHelper.getUserName()).thenReturn("Test User");
        when(sessionHelper.getUserCode()).thenReturn("HE123456");

        // Create sample data
        scheduleResponseList = createSampleScheduleList();
    }

    @Test
    @DisplayName("getCachedScheduleList should return data from cache when available")
    void getCachedScheduleList_CacheHit() {
        // Given
        String userId = "user123";
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_STUDENT + "list_" + userId + "_"
                + request.toString();
        PageableObject<STDScheduleAttendanceResponse> cachedData = mock(PageableObject.class);

        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong())).thenReturn(cachedData);

        // When
        PageableObject<STDScheduleAttendanceResponse> result = (PageableObject<STDScheduleAttendanceResponse>) service
                .getCachedScheduleList(request);

        // Then
        assertNotNull(result);
        assertSame(cachedData, result);
        verify(redisCacheHelper).getOrSet(anyString(), any(), any(), anyLong());
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("getCachedScheduleList should fetch and cache data when not in cache")
    void getCachedScheduleList_CacheMiss() {
        // Given
        String userId = "user123";
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_STUDENT + "list_" + userId + "_"
                + request.toString();

        Page<STDScheduleAttendanceResponse> page = new PageImpl<>(scheduleResponseList);
        when(repository.getAllListAttendanceByUser(any(Pageable.class), eq(request))).thenReturn(page);
        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong()))
                .thenAnswer(invocation -> {
                    java.util.function.Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });

        // When
        PageableObject<?> result = service.getCachedScheduleList(request);

        // Then
        assertNotNull(result);
        verify(redisCacheHelper).getOrSet(anyString(), any(), any(), anyLong());
        verify(repository).getAllListAttendanceByUser(any(Pageable.class), eq(request));
    }

    @Test
    @DisplayName("Should return data from cache when available")
    void getList_WithCachedData_ShouldReturnCachedData() {
        // Given
        PageableObject<?> mockedResult = new PageableObject<>();
        doReturn(mockedResult).when(service).getCachedScheduleList(request);

        // When
        ResponseEntity<?> result = service.getList(request);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) result.getBody();
        assertEquals(RestApiStatus.SUCCESS, apiResponse.getStatus());
        assertEquals("Lấy danh sách điểm danh thành công", apiResponse.getMessage());
        assertEquals(mockedResult, apiResponse.getData());

        verify(service).getCachedScheduleList(request);
        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("Should export schedule attendance to PDF")
    void exportScheduleAttendance_ShouldReturnPdfInputStream() {
        // Given
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream mockInputStream = new ByteArrayInputStream(outputStream.toByteArray());

        // Use spy to partially mock the service
        doReturn(mockInputStream).when(service).exportScheduleAttendance(anyList());

        // When
        ByteArrayInputStream result = service.exportScheduleAttendance(scheduleResponseList);

        // Then
        assertNotNull(result);
    }

    /**
     * Helper method to create a list of sample schedule attendance responses
     */
    private List<STDScheduleAttendanceResponse> createSampleScheduleList() {
        List<STDScheduleAttendanceResponse> list = new ArrayList<>();

        // Create concrete implementation of interface
        STDScheduleAttendanceResponse item1 = new STDScheduleAttendanceResponse() {
            @Override
            public Integer getIndexs() {
                return 1;
            }

            @Override
            public String getId() {
                return "attendance1";
            }

            @Override
            public Long getAttendanceDayStart() {
                return new Date().getTime();
            }

            @Override
            public Long getAttendanceDayEnd() {
                return new Date().getTime() + 3600000; // 1 hour later
            }

            @Override
            public String getSubjectCode() {
                return "CS101";
            }

            @Override
            public String getSubjectName() {
                return "Introduction to Programming";
            }

            @Override
            public String getStaffName() {
                return "Teacher One";
            }

            @Override
            public String getShift() {
                return "1";
            }

            @Override
            public String getDescription() {
                return "First class";
            }

            @Override
            public String getFactoryName() {
                return "Factory A";
            }

            @Override
            public String getProjectName() {
                return "Project 1";
            }

            @Override
            public String getLink() {
                return "http://meet.example.com/abc123";
            }

            @Override
            public String getLocation() {
                return "Room 101";
            }

            @Override
            public Integer getType() {
                return 1;
            }
        };

        STDScheduleAttendanceResponse item2 = new STDScheduleAttendanceResponse() {
            @Override
            public Integer getIndexs() {
                return 2;
            }

            @Override
            public String getId() {
                return "attendance2";
            }

            @Override
            public Long getAttendanceDayStart() {
                return new Date().getTime() + 86400000; // Next day
            }

            @Override
            public Long getAttendanceDayEnd() {
                return new Date().getTime() + 86400000 + 3600000; // Next day + 1 hour
            }

            @Override
            public String getSubjectCode() {
                return "CS102";
            }

            @Override
            public String getSubjectName() {
                return "Data Structures";
            }

            @Override
            public String getStaffName() {
                return "Teacher Two";
            }

            @Override
            public String getShift() {
                return "2";
            }

            @Override
            public String getDescription() {
                return "Second class";
            }

            @Override
            public String getFactoryName() {
                return "Factory B";
            }

            @Override
            public String getProjectName() {
                return "Project 2";
            }

            @Override
            public String getLink() {
                return "http://meet.example.com/def456";
            }

            @Override
            public String getLocation() {
                return "Room 102";
            }

            @Override
            public Integer getType() {
                return 1;
            }
        };

        list.add(item1);
        list.add(item2);

        return list;
    }
}