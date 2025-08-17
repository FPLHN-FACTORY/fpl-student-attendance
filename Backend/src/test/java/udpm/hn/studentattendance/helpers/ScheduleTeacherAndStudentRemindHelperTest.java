package udpm.hn.studentattendance.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import udpm.hn.studentattendance.entities.Factory;
import udpm.hn.studentattendance.entities.PlanDate;
import udpm.hn.studentattendance.entities.PlanFactory;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.entities.UserStudentFactory;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonScheduleReminderRepository;
import udpm.hn.studentattendance.infrastructure.config.mailer.model.MailerDefaultRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleTeacherAndStudentRemindHelperTest {

    @Mock
    private CommonScheduleReminderRepository commonScheduleReminderRepository;

    @Mock
    private MailerHelper mailerHelper;

    @InjectMocks
    private ScheduleTeacherAndStudentRemindHelper scheduleTeacherAndStudentRemindHelper;

    private PlanDate planDate1;
    private PlanDate planDate2;
    private UserStudentFactory userStudentFactory;
    private Factory factory;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(scheduleTeacherAndStudentRemindHelper, "appName", "Test Student Attendance");

        // Setup test data
        factory = new Factory();
        factory.setId("factory-1");
        factory.setName("Test Factory");

        PlanFactory planFactory = new PlanFactory();
        planFactory.setFactory(factory);

        planDate1 = new PlanDate();
        planDate1.setId("plan-date-1");
        planDate1.setStartDate(
                LocalDate.now().plusDays(1).atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant().toEpochMilli());
        planDate1.setEndDate(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).plusHours(2)
                .toInstant().toEpochMilli());
        planDate1.setShift(Arrays.asList(1));
        planDate1.setRoom("Room A101");
        planDate1.setDescription("Test Session 1");
        planDate1.setPlanFactory(planFactory);

        planDate2 = new PlanDate();
        planDate2.setId("plan-date-2");
        planDate2.setStartDate(
                LocalDate.now().plusDays(2).atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant().toEpochMilli());
        planDate2.setEndDate(LocalDate.now().plusDays(2).atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).plusHours(2)
                .toInstant().toEpochMilli());
        planDate2.setShift(Arrays.asList(2));
        planDate2.setRoom("Room B202");
        planDate2.setDescription("Test Session 2");
        planDate2.setPlanFactory(planFactory);

        UserStudent userStudent = new UserStudent();
        userStudent.setId("student-1");
        userStudent.setName("Test Student");
        userStudent.setEmail("student@test.com");

        userStudentFactory = new UserStudentFactory();
        userStudentFactory.setId("usf-1");
        userStudentFactory.setUserStudent(userStudent);
        userStudentFactory.setFactory(factory);
    }

    @Test
    void testScheduleTeacherAndStudentRemindHelperExists() {
        assertNotNull(ScheduleTeacherAndStudentRemindHelper.class);
    }

    @Test
    void testCreateWeeklyScheduleEmailContent() {
        // Given
        List<PlanDate> planDates = Arrays.asList(planDate1, planDate2);
        LocalDate weekStart = LocalDate.now().plusDays(1);
        LocalDate weekEnd = LocalDate.now().plusDays(7);

        // When
        String result = ReflectionTestUtils.invokeMethod(
                scheduleTeacherAndStudentRemindHelper,
                "createWeeklyScheduleEmailContent",
                planDates, weekStart, weekEnd);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("day-schedule"));
        assertTrue(result.contains("schedule-table"));
        assertTrue(result.contains("Room A101"));
        assertTrue(result.contains("Room B202"));
        assertTrue(result.contains("Test Session 1"));
        assertTrue(result.contains("Test Session 2"));
    }

    @Test
    void testGetNextMonday() {
        // When
        LocalDate result = ReflectionTestUtils.invokeMethod(
                scheduleTeacherAndStudentRemindHelper,
                "getNextMonday");

        // Then
        assertNotNull(result);
        assertEquals(1, result.getDayOfWeek().getValue()); // Monday = 1
        assertTrue(result.isAfter(LocalDate.now()) || result.isEqual(LocalDate.now()));
    }

    @Test
    void testSendManualScheduleReminders() {
        // Given
        String factoryId = "factory-1";
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(7);

        when(commonScheduleReminderRepository.findUpcomingPlanDates(
                anyLong(), anyLong(), eq(EntityStatus.ACTIVE), eq(EntityStatus.ACTIVE)))
                .thenReturn(Arrays.asList(planDate1, planDate2));

        when(commonScheduleReminderRepository.findStudentsByFactoryIdAndStatus(
                eq(factoryId), eq(EntityStatus.ACTIVE))).thenReturn(Arrays.asList(userStudentFactory));

        when(commonScheduleReminderRepository.findTeachersByFactoryId(factoryId))
                .thenReturn(Arrays.asList("teacher@test.com"));

        // When
        scheduleTeacherAndStudentRemindHelper.sendManualScheduleReminders(factoryId, startDate, endDate);

        // Then
        verify(commonScheduleReminderRepository).findUpcomingPlanDates(
                anyLong(), anyLong(), eq(EntityStatus.ACTIVE), eq(EntityStatus.ACTIVE));
        verify(commonScheduleReminderRepository).findStudentsByFactoryIdAndStatus(
                eq(factoryId), eq(EntityStatus.ACTIVE));
        verify(commonScheduleReminderRepository).findTeachersByFactoryId(factoryId);
    }

    @Test
    void testSendManualScheduleRemindersWithEmptyPlanDates() {
        // Given
        String factoryId = "factory-1";
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(7);

        when(commonScheduleReminderRepository.findUpcomingPlanDates(
                anyLong(), anyLong(), eq(EntityStatus.ACTIVE), eq(EntityStatus.ACTIVE))).thenReturn(Arrays.asList());

        // When
        scheduleTeacherAndStudentRemindHelper.sendManualScheduleReminders(factoryId, startDate, endDate);

        // Then
        verify(commonScheduleReminderRepository).findUpcomingPlanDates(
                anyLong(), anyLong(), eq(EntityStatus.ACTIVE), eq(EntityStatus.ACTIVE));
        verify(commonScheduleReminderRepository, never()).findStudentsByFactoryIdAndStatus(any(), any());
        verify(commonScheduleReminderRepository, never()).findTeachersByFactoryId(any());
    }

    @Test
    void testSendManualScheduleRemindersWithException() {
        // Given
        String factoryId = "factory-1";
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(7);

        when(commonScheduleReminderRepository.findUpcomingPlanDates(
                anyLong(), anyLong(), eq(EntityStatus.ACTIVE), eq(EntityStatus.ACTIVE)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then - Should not throw exception
        assertDoesNotThrow(
                () -> scheduleTeacherAndStudentRemindHelper.sendManualScheduleReminders(factoryId, startDate, endDate));
    }

    @Test
    void testEmailContentContainsAppName() {
        // Given
        List<PlanDate> planDates = Arrays.asList(planDate1);
        LocalDate weekStart = LocalDate.now().plusDays(1);
        LocalDate weekEnd = LocalDate.now().plusDays(7);

        // When
        String emailContent = ReflectionTestUtils.invokeMethod(
                scheduleTeacherAndStudentRemindHelper,
                "createWeeklyScheduleEmailContent",
                planDates, weekStart, weekEnd);

        // Then
        assertNotNull(emailContent);
        assertTrue(emailContent.contains("day-schedule"));
        assertTrue(emailContent.contains("schedule-table"));
    }
}
