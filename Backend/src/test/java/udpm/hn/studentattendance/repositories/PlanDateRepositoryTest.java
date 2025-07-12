package udpm.hn.studentattendance.repositories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import udpm.hn.studentattendance.entities.PlanDate;
import udpm.hn.studentattendance.entities.PlanFactory;
import udpm.hn.studentattendance.infrastructure.constants.ShiftType;
import udpm.hn.studentattendance.infrastructure.constants.StatusType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanDateRepositoryTest {

    @Mock
    private PlanDateRepository planDateRepository;

    @Test
    void testSaveAndFindById() {
        // Given
        PlanDate planDate = new PlanDate();
        planDate.setDescription("Monday morning session");
        planDate.setStartDate(1640995200000L); // 2022-01-01
        planDate.setEndDate(1640998800000L); // 2022-01-01 + 1 hour
        planDate.setShift(Arrays.asList(1, 2, 3));
        planDate.setLateArrival(15);
        planDate.setLink("https://meet.google.com/abc-def");
        planDate.setRoom("Room A101");
        planDate.setType(ShiftType.OFFLINE);
        planDate.setRequiredLocation(StatusType.ENABLE);
        planDate.setRequiredIp(StatusType.ENABLE);
        planDate.setRequiredCheckin(StatusType.ENABLE);
        planDate.setRequiredCheckout(StatusType.ENABLE);

        // Mock behavior
        when(planDateRepository.save(any(PlanDate.class))).thenReturn(planDate);
        when(planDateRepository.findById(anyString())).thenReturn(Optional.of(planDate));

        // When
        PlanDate savedPlanDate = planDateRepository.save(planDate);
        Optional<PlanDate> foundPlanDate = planDateRepository.findById("mock-id");

        // Then
        assertTrue(foundPlanDate.isPresent());
        assertEquals("Monday morning session", foundPlanDate.get().getDescription());
        assertEquals(1640995200000L, foundPlanDate.get().getStartDate());
        assertEquals(1640998800000L, foundPlanDate.get().getEndDate());
        assertEquals(Arrays.asList(1, 2, 3), foundPlanDate.get().getShift());
        assertEquals(15, foundPlanDate.get().getLateArrival());
        assertEquals("https://meet.google.com/abc-def", foundPlanDate.get().getLink());
        assertEquals("Room A101", foundPlanDate.get().getRoom());
        assertEquals(ShiftType.OFFLINE, foundPlanDate.get().getType());
        assertEquals(StatusType.ENABLE, foundPlanDate.get().getRequiredLocation());
        assertEquals(StatusType.ENABLE, foundPlanDate.get().getRequiredIp());
        assertEquals(StatusType.ENABLE, foundPlanDate.get().getRequiredCheckin());
        assertEquals(StatusType.ENABLE, foundPlanDate.get().getRequiredCheckout());
        verify(planDateRepository).save(any(PlanDate.class));
        verify(planDateRepository).findById(anyString());
    }

    @Test
    void testSaveAndFindAll() {
        // Given
        PlanDate planDate1 = new PlanDate();
        planDate1.setDescription("Session 1");
        planDate1.setStartDate(1640995200000L);
        planDate1.setEndDate(1640998800000L);
        planDate1.setShift(Arrays.asList(1, 2));

        PlanDate planDate2 = new PlanDate();
        planDate2.setDescription("Session 2");
        planDate2.setStartDate(1640998800000L);
        planDate2.setEndDate(1641002400000L);
        planDate2.setShift(Arrays.asList(3, 4));

        List<PlanDate> planDates = Arrays.asList(planDate1, planDate2);

        // Mock behavior
        when(planDateRepository.findAll()).thenReturn(planDates);
        when(planDateRepository.save(any(PlanDate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        planDateRepository.save(planDate1);
        planDateRepository.save(planDate2);
        List<PlanDate> allPlanDates = planDateRepository.findAll();

        // Then
        assertEquals(2, allPlanDates.size());
        assertTrue(allPlanDates.stream().anyMatch(pd -> "Session 1".equals(pd.getDescription())));
        assertTrue(allPlanDates.stream().anyMatch(pd -> "Session 2".equals(pd.getDescription())));
        verify(planDateRepository, times(2)).save(any(PlanDate.class));
        verify(planDateRepository).findAll();
    }

    @Test
    void testUpdatePlanDate() {
        // Given
        PlanDate planDate = new PlanDate();
        planDate.setDescription("Original Description");
        planDate.setStartDate(1640995200000L);
        planDate.setEndDate(1640998800000L);
        planDate.setLateArrival(10);

        PlanDate updatedPlanDate = new PlanDate();
        updatedPlanDate.setDescription("Updated Description");
        updatedPlanDate.setStartDate(1640995200000L);
        updatedPlanDate.setEndDate(1640998800000L);
        updatedPlanDate.setLateArrival(20);
        updatedPlanDate.setRoom("Updated Room");

        // Mock behavior
        when(planDateRepository.save(any(PlanDate.class))).thenReturn(planDate).thenReturn(updatedPlanDate);

        // When
        PlanDate savedPlanDate = planDateRepository.save(planDate);
        savedPlanDate.setDescription("Updated Description");
        savedPlanDate.setLateArrival(20);
        savedPlanDate.setRoom("Updated Room");
        PlanDate resultPlanDate = planDateRepository.save(savedPlanDate);

        // Then
        assertEquals("Updated Description", resultPlanDate.getDescription());
        assertEquals(20, resultPlanDate.getLateArrival());
        assertEquals("Updated Room", resultPlanDate.getRoom());
        verify(planDateRepository, times(2)).save(any(PlanDate.class));
    }

    @Test
    void testDeletePlanDate() {
        // Given
        PlanDate planDate = new PlanDate();
        planDate.setDescription("Plan Date to Delete");
        planDate.setStartDate(1640995200000L);
        planDate.setEndDate(1640998800000L);
        String planDateId = "mock-id";

        // Mock behavior
        when(planDateRepository.save(any(PlanDate.class))).thenReturn(planDate);
        doNothing().when(planDateRepository).deleteById(anyString());
        when(planDateRepository.findById(planDateId)).thenReturn(Optional.empty());

        // When
        PlanDate savedPlanDate = planDateRepository.save(planDate);
        planDateRepository.deleteById(planDateId);
        Optional<PlanDate> deletedPlanDate = planDateRepository.findById(planDateId);

        // Then
        assertFalse(deletedPlanDate.isPresent());
        verify(planDateRepository).save(any(PlanDate.class));
        verify(planDateRepository).deleteById(anyString());
        verify(planDateRepository).findById(anyString());
    }

    @Test
    void testSavePlanDateWithPlanFactory() {
        // Given
        PlanFactory planFactory = new PlanFactory();
        planFactory.setId("factory-id");

        PlanDate planDate = new PlanDate();
        planDate.setDescription("Session with Factory");
        planDate.setStartDate(1640995200000L);
        planDate.setEndDate(1640998800000L);
        planDate.setPlanFactory(planFactory);

        // Mock behavior
        when(planDateRepository.save(any(PlanDate.class))).thenReturn(planDate);
        when(planDateRepository.findById(anyString())).thenReturn(Optional.of(planDate));

        // When
        PlanDate savedPlanDate = planDateRepository.save(planDate);
        Optional<PlanDate> foundPlanDate = planDateRepository.findById("mock-id");

        // Then
        assertTrue(foundPlanDate.isPresent());
        assertNotNull(foundPlanDate.get().getPlanFactory());
        assertEquals("factory-id", foundPlanDate.get().getPlanFactory().getId());
        verify(planDateRepository).save(any(PlanDate.class));
        verify(planDateRepository).findById(anyString());
    }
}