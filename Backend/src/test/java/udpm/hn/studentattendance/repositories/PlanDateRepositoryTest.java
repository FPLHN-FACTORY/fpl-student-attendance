package udpm.hn.studentattendance.repositories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import udpm.hn.studentattendance.entities.PlanDate;
import udpm.hn.studentattendance.infrastructure.config.TestDatabaseConfig;
import udpm.hn.studentattendance.entities.PlanFactory;
import udpm.hn.studentattendance.infrastructure.config.TestDatabaseConfig;
import udpm.hn.studentattendance.infrastructure.constants.ShiftType;
import udpm.hn.studentattendance.infrastructure.constants.StatusType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.context.annotation.Import;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@Import(TestDatabaseConfig.class)
class PlanDateRepositoryTest {

    @Autowired
    private PlanDateRepository planDateRepository;

    @Autowired
    private PlanFactoryRepository planFactoryRepository;

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

        // When
        PlanDate savedPlanDate = planDateRepository.save(planDate);
        Optional<PlanDate> foundPlanDate = planDateRepository.findById(savedPlanDate.getId());

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

        // When
        planDateRepository.save(planDate1);
        planDateRepository.save(planDate2);
        List<PlanDate> allPlanDates = planDateRepository.findAll();

        // Then
        assertTrue(allPlanDates.size() >= 2);
        assertTrue(allPlanDates.stream().anyMatch(pd -> "Session 1".equals(pd.getDescription())));
        assertTrue(allPlanDates.stream().anyMatch(pd -> "Session 2".equals(pd.getDescription())));
    }

    @Test
    void testUpdatePlanDate() {
        // Given
        PlanDate planDate = new PlanDate();
        planDate.setDescription("Original Description");
        planDate.setStartDate(1640995200000L);
        planDate.setEndDate(1640998800000L);
        planDate.setLateArrival(10);

        PlanDate savedPlanDate = planDateRepository.save(planDate);

        // When
        savedPlanDate.setDescription("Updated Description");
        savedPlanDate.setLateArrival(20);
        savedPlanDate.setRoom("Updated Room");
        PlanDate updatedPlanDate = planDateRepository.save(savedPlanDate);

        // Then
        assertEquals("Updated Description", updatedPlanDate.getDescription());
        assertEquals(20, updatedPlanDate.getLateArrival());
        assertEquals("Updated Room", updatedPlanDate.getRoom());
    }

    @Test
    void testDeletePlanDate() {
        // Given
        PlanDate planDate = new PlanDate();
        planDate.setDescription("Plan Date to Delete");
        planDate.setStartDate(1640995200000L);
        planDate.setEndDate(1640998800000L);

        PlanDate savedPlanDate = planDateRepository.save(planDate);
        String planDateId = savedPlanDate.getId();

        // When
        planDateRepository.deleteById(planDateId);
        Optional<PlanDate> deletedPlanDate = planDateRepository.findById(planDateId);

        // Then
        assertFalse(deletedPlanDate.isPresent());
    }

    @Test
    void testSavePlanDateWithPlanFactory() {
        // Given
        PlanFactory planFactory = new PlanFactory();
        PlanFactory savedPlanFactory = planFactoryRepository.save(planFactory);

        PlanDate planDate = new PlanDate();
        planDate.setDescription("Session with Factory");
        planDate.setStartDate(1640995200000L);
        planDate.setEndDate(1640998800000L);
        planDate.setPlanFactory(savedPlanFactory);

        // When
        PlanDate savedPlanDate = planDateRepository.save(planDate);
        Optional<PlanDate> foundPlanDate = planDateRepository.findById(savedPlanDate.getId());

        // Then
        assertTrue(foundPlanDate.isPresent());
        assertNotNull(foundPlanDate.get().getPlanFactory());
        assertEquals(savedPlanFactory.getId(), foundPlanDate.get().getPlanFactory().getId());
    }
}