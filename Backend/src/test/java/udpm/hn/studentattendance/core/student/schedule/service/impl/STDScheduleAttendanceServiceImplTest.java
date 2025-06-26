package udpm.hn.studentattendance.core.student.schedule.service.impl;

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
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import udpm.hn.studentattendance.core.student.schedule.model.request.STDScheduleAttendanceSearchRequest;
import udpm.hn.studentattendance.core.student.schedule.model.response.STDScheduleAttendanceResponse;
import udpm.hn.studentattendance.core.student.schedule.repository.STDScheduleAttendanceRepository;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class STDScheduleAttendanceServiceImplTest {

    @Mock
    private STDScheduleAttendanceRepository repository;

    @Mock
    private SessionHelper sessionHelper;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private STDScheduleAttendanceServiceImpl service;

    private STDScheduleAttendanceSearchRequest request;
    private List<STDScheduleAttendanceResponse> scheduleResponseList;

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
    @DisplayName("Should return data from cache when available")
    void getList_WithCachedData_ShouldReturnCachedData() {
        // Given
        String cacheKey = "schedule:list:user123:" + request.toString();
        Object cachedData = new Object(); // Mocked cached data
        when(redisService.get(cacheKey)).thenReturn(cachedData);

        // When
        ResponseEntity<?> result = service.getList(request);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        verify(redisService).get(anyString());
        verify(repository, never()).getAllListAttendanceByUser(any(Pageable.class), any());
        verify(redisService, never()).set(anyString(), any(), anyLong());
    }

    @Test
    @DisplayName("Should fetch data from repository when not in cache")
    void getList_WithoutCachedData_ShouldFetchFromRepo() {
        // Given
        String cacheKey = "schedule:list:user123:" + request.toString();
        when(redisService.get(cacheKey)).thenReturn(null);

        Page<STDScheduleAttendanceResponse> page = new PageImpl<>(scheduleResponseList);
        when(repository.getAllListAttendanceByUser(any(Pageable.class), eq(request))).thenReturn(page);

        // When
        ResponseEntity<?> result = service.getList(request);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        verify(redisService).get(anyString());
        verify(repository).getAllListAttendanceByUser(any(Pageable.class), eq(request));
        verify(redisService).set(anyString(), any(), eq(3600L));
    }

    @Test
    @DisplayName("Should invalidate schedule cache for a user")
    void invalidateScheduleCache_ShouldCallRedisDeletePattern() {
        // Given
        String userId = "user123";
        String cachePattern = "schedule:list:" + userId + ":*";

        // When
        service.invalidateScheduleCache(userId);

        // Then
        verify(redisService).deletePattern(cachePattern);
    }

    @Test
    @DisplayName("Should export schedule attendance to PDF")
    void exportScheduleAttendance_ShouldReturnPdfInputStream() {
        // When
        ByteArrayInputStream result = service.exportScheduleAttendance(scheduleResponseList);

        // Then
        assertNotNull(result);
        assertTrue(result.available() > 0);
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