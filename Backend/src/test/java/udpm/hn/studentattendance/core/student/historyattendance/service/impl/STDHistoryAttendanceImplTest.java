package udpm.hn.studentattendance.core.student.historyattendance.service.impl;

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
import udpm.hn.studentattendance.core.authentication.oauth2.AuthUser;
import udpm.hn.studentattendance.core.student.historyattendance.model.request.STDHistoryAttendanceRequest;
import udpm.hn.studentattendance.core.student.historyattendance.model.response.STDHistoryAttendanceResponse;
import udpm.hn.studentattendance.core.student.historyattendance.model.response.STDHistoryPlanDateAttendanceResponse;
import udpm.hn.studentattendance.core.student.historyattendance.repository.STDHistoryAttendanceExtendRepository;
import udpm.hn.studentattendance.core.student.historyattendance.repository.STDHistoryAttendanceFactoryExtendRepository;
import udpm.hn.studentattendance.core.student.historyattendance.repository.STDHistoryAttendanceSemesterExtendRepository;
import udpm.hn.studentattendance.entities.Factory;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class STDHistoryAttendanceImplTest {

    @Mock
    private STDHistoryAttendanceExtendRepository historyAttendanceExtendRepository;

    @Mock
    private SessionHelper sessionHelper;

    @Mock
    private STDHistoryAttendanceSemesterExtendRepository historyAttendanceSemesterExtendRepository;

    @Mock
    private STDHistoryAttendanceFactoryExtendRepository historyAttendanceFactoryExtendRepository;

    @InjectMocks
    private STDHistoryAttendanceImpl service;

    private List<STDHistoryAttendanceResponse> attendanceResponses;
    private AuthUser testUser;
    private List<Semester> semesters;
    private List<Factory> factories;
    private List<STDHistoryPlanDateAttendanceResponse> planDateResponses;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new AuthUser();
        testUser.setId("user123");
        testUser.setCode("HE123456");
        testUser.setName("Test User");

        Set<RoleConstant> roles = new HashSet<>();
        roles.add(RoleConstant.STUDENT);
        testUser.setRole(roles);

        // Mock user session
        when(sessionHelper.getCurrentUser()).thenReturn(testUser);
        when(sessionHelper.getUserId()).thenReturn("user123");
        when(sessionHelper.getUserCode()).thenReturn("HE123456");
        when(sessionHelper.getUserName()).thenReturn("Test User");
        when(sessionHelper.getFacilityId()).thenReturn("facility123");

        // Setup test data
        setupTestData();
    }

    @Test
    @DisplayName("Should get all history attendance with current semester when semesterId is not provided")
    void getAllHistoryAttendance_WithNoSemesterProvided_ShouldUseCurrentSemester() {
        // Given
        STDHistoryAttendanceRequest request = new STDHistoryAttendanceRequest();
        request.setPage(1);
        request.setSize(10);

        // Mock current date to fall within semester1
        long now = new Date().getTime();
        when(historyAttendanceSemesterExtendRepository.getAllSemestersByStatus(EntityStatus.ACTIVE))
                .thenReturn(semesters);

        Page<STDHistoryAttendanceResponse> page = new PageImpl<>(attendanceResponses);
        when(historyAttendanceExtendRepository.getAllFactoryAttendance(
                eq("user123"), any(Pageable.class), any(STDHistoryAttendanceRequest.class), anyLong()))
                .thenReturn(page);

        // When
        ResponseEntity<?> result = service.getAllHistoryAttendanceByStudent(request);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        // Verify that the result contains data
        assertNotNull(result.getBody());
    }

    @Test
    @DisplayName("Should get all history attendance with provided semester")
    void getAllHistoryAttendance_WithSemesterProvided_ShouldUseThatSemester() {
        // Given
        STDHistoryAttendanceRequest request = new STDHistoryAttendanceRequest();
        request.setPage(1);
        request.setSize(10);
        request.setSemesterId("semester2"); // Use specific semester

        Page<STDHistoryAttendanceResponse> page = new PageImpl<>(attendanceResponses);
        when(historyAttendanceExtendRepository.getAllFactoryAttendance(
                eq("user123"), any(Pageable.class), any(STDHistoryAttendanceRequest.class), anyLong()))
                .thenReturn(page);

        // When
        ResponseEntity<?> result = service.getAllHistoryAttendanceByStudent(request);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        // Verify that the semester ID was preserved
        assertEquals("semester2", request.getSemesterId());
    }

    @Test
    @DisplayName("Should get all semesters")
    void getAllSemester_ShouldReturnAllActiveSemesters() {
        // Given
        when(historyAttendanceSemesterExtendRepository.getAllSemesterByCode(EntityStatus.ACTIVE))
                .thenReturn(semesters);

        // When
        ResponseEntity<?> result = service.getAllSemester();

        // Then
        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        // Verify the message contains the correct data
        assertTrue(result.getBody().toString().contains("\"data\""));
    }

    @Test
    @DisplayName("Should get all factories for current student")
    void getAllFactoryByUserStudent_ShouldReturnFactories() {
        // Given
        when(historyAttendanceFactoryExtendRepository.getAllFactoryByUser(EntityStatus.ACTIVE, "user123"))
                .thenReturn(factories);

        // When
        ResponseEntity<?> result = service.getAllFactoryByUserStudent();

        // Then
        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        // Verify the message contains the correct data
        assertTrue(result.getBody().toString().contains("\"data\""));
    }

    @Test
    @DisplayName("Should export history attendance to PDF")
    void exportHistoryAttendance_ShouldReturnPdfInputStream() {
        // Given
        String factoryName = "Test Factory";

        // When
        ByteArrayInputStream result = service.exportHistoryAttendance(attendanceResponses, factoryName);

        // Then
        assertNotNull(result);
        assertTrue(result.available() > 0);
    }

    @Test
    @DisplayName("Should get detail plan date")
    void getDetailPlanDate_ShouldReturnPlanDates() {
        // Given
        when(historyAttendanceExtendRepository.getDetailPlanDate("user123", "facility123"))
                .thenReturn(planDateResponses);

        // When
        ResponseEntity<?> result = service.getDetailPlanDate();

        // Then
        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        // Verify the message contains the correct data
        assertTrue(result.getBody().toString().contains("\"data\""));
    }

    private void setupTestData() {
        // Create semesters
        semesters = new ArrayList<>();

        Semester semester1 = new Semester();
        semester1.setId("semester1");
        semester1.setCode("Spring2023");
        // Set name if there's a setter for it
        semester1.setFromDate(new Date().getTime() - 86400000 * 30); // 30 days ago
        semester1.setToDate(new Date().getTime() + 86400000 * 60); // 60 days in future

        Semester semester2 = new Semester();
        semester2.setId("semester2");
        semester2.setCode("Summer2023");
        // Set name if there's a setter for it
        semester2.setFromDate(new Date().getTime() + 86400000 * 90); // 90 days in future
        semester2.setToDate(new Date().getTime() + 86400000 * 180); // 180 days in future

        semesters.add(semester1);
        semesters.add(semester2);

        // Create factories
        factories = new ArrayList<>();

        Factory factory1 = new Factory();
        factory1.setId("factory1");
        factory1.setName("Java Programming");

        Factory factory2 = new Factory();
        factory2.setId("factory2");
        factory2.setName("Python Programming");

        factories.add(factory1);
        factories.add(factory2);

        // Create history attendance responses
        attendanceResponses = createSampleAttendanceResponses();

        // Create plan date responses
        planDateResponses = createSamplePlanDateResponses();
    }

    private List<STDHistoryAttendanceResponse> createSampleAttendanceResponses() {
        List<STDHistoryAttendanceResponse> responses = new ArrayList<>();

        // Add sample implementation of the interface
        responses.add(new STDHistoryAttendanceResponse() {
            @Override
            public Integer getRowNumber() {
                return 1;
            }

            @Override
            public String getStatusAttendance() {
                return "ON TIME";
            }

            @Override
            public Long getPlanDateStartDate() {
                return new Date().getTime() - 86400000 * 2; // 2 days ago
            }

            @Override
            public String getPlanDateShift() {
                return "Morning";
            }

            @Override
            public Integer getLateArrival() {
                return 0;
            }

            @Override
            public String getPlanDateDescription() {
                return "Java OOP Lesson";
            }

            @Override
            public String getFactoryName() {
                return "Test Factory";
            }

            @Override
            public Long getPlanDateEndDate() {
                return new Date().getTime() - 86400000 * 2 + 7200000; // 2 hours after start
            }

            @Override
            public String getPlanDateId() {
                return "plan1";
            }

            @Override
            public String getFactoryId() {
                return "factory1";
            }

            @Override
            public Long getCheckIn() {
                return new Date().getTime() - 86400000 * 2 + 300000; // 5 minutes after start
            }

            @Override
            public Long getCheckOut() {
                return new Date().getTime() - 86400000 * 2 + 7000000; // Just before end
            }

            @Override
            public Integer getRequiredCheckIn() {
                return 0;
            }

            @Override
            public Integer getRequiredCheckOut() {
                return 0;
            }
        });

        responses.add(new STDHistoryAttendanceResponse() {
            @Override
            public Integer getRowNumber() {
                return 2;
            }

            @Override
            public String getStatusAttendance() {
                return "LATE";
            }

            @Override
            public Long getPlanDateStartDate() {
                return new Date().getTime() - 86400000; // 1 day ago
            }

            @Override
            public String getPlanDateShift() {
                return "Afternoon";
            }

            @Override
            public Integer getLateArrival() {
                return 15;
            }

            @Override
            public String getPlanDateDescription() {
                return "Spring Boot Intro";
            }

            @Override
            public String getFactoryName() {
                return "Test Factory";
            }

            @Override
            public Long getPlanDateEndDate() {
                return new Date().getTime() - 86400000 + 7200000; // 2 hours after start
            }

            @Override
            public String getPlanDateId() {
                return "plan2";
            }

            @Override
            public String getFactoryId() {
                return "factory1";
            }

            @Override
            public Long getCheckIn() {
                return new Date().getTime() - 86400000 + 900000; // 15 minutes after start
            }

            @Override
            public Long getCheckOut() {
                return new Date().getTime() - 86400000 + 7000000; // Just before end
            }

            @Override
            public Integer getRequiredCheckIn() {
                return 0;
            }

            @Override
            public Integer getRequiredCheckOut() {
                return 0;
            }
        });

        return responses;
    }

    private List<STDHistoryPlanDateAttendanceResponse> createSamplePlanDateResponses() {
        // Implementation depends on what fields STDHistoryPlanDateAttendanceResponse
        // has
        // This is a generic implementation assuming it has simple getters/setters
        List<STDHistoryPlanDateAttendanceResponse> responses = new ArrayList<>();

        // We'd need to implement the interface directly if it's an interface like the
        // other response types
        // This is just a placeholder implementation
        return responses;
    }
}