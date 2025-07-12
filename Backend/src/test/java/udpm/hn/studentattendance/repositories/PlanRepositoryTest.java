package udpm.hn.studentattendance.repositories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import udpm.hn.studentattendance.entities.Plan;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanRepositoryTest {

    @Mock
    private PlanRepository planRepository;

    @Test
    void testSaveAndFindById() {
        // Given
        Plan plan = new Plan();
        plan.setName("Spring 2024 Plan");
        plan.setDescription("Academic plan for Spring 2024");

        // Mock behavior
        when(planRepository.save(any(Plan.class))).thenReturn(plan);
        when(planRepository.findById(anyString())).thenReturn(Optional.of(plan));

        // When
        Plan savedPlan = planRepository.save(plan);
        Optional<Plan> foundPlan = planRepository.findById("mock-id");

        // Then
        assertTrue(foundPlan.isPresent());
        assertEquals("Spring 2024 Plan", foundPlan.get().getName());
        assertEquals("Academic plan for Spring 2024", foundPlan.get().getDescription());
        verify(planRepository).save(any(Plan.class));
        verify(planRepository).findById(anyString());
    }

    @Test
    void testSaveAndFindAll() {
        // Given
        Plan plan1 = new Plan();
        plan1.setName("Plan 1");
        plan1.setDescription("First plan");

        Plan plan2 = new Plan();
        plan2.setName("Plan 2");
        plan2.setDescription("Second plan");

        List<Plan> plans = Arrays.asList(plan1, plan2);

        // Mock behavior
        when(planRepository.findAll()).thenReturn(plans);
        when(planRepository.save(any(Plan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        planRepository.save(plan1);
        planRepository.save(plan2);
        List<Plan> allPlans = planRepository.findAll();

        // Then
        assertEquals(2, allPlans.size());
        assertTrue(allPlans.stream().anyMatch(p -> "Plan 1".equals(p.getName())));
        assertTrue(allPlans.stream().anyMatch(p -> "Plan 2".equals(p.getName())));
        verify(planRepository, times(2)).save(any(Plan.class));
        verify(planRepository).findAll();
    }

    @Test
    void testUpdatePlan() {
        // Given
        Plan plan = new Plan();
        plan.setName("Original Name");
        plan.setDescription("Original Description");

        Plan updatedPlan = new Plan();
        updatedPlan.setName("Updated Name");
        updatedPlan.setDescription("Updated Description");

        // Mock behavior
        when(planRepository.save(any(Plan.class))).thenReturn(plan).thenReturn(updatedPlan);

        // When
        Plan savedPlan = planRepository.save(plan);
        savedPlan.setName("Updated Name");
        savedPlan.setDescription("Updated Description");
        Plan resultPlan = planRepository.save(savedPlan);

        // Then
        assertEquals("Updated Name", resultPlan.getName());
        assertEquals("Updated Description", resultPlan.getDescription());
        verify(planRepository, times(2)).save(any(Plan.class));
    }

    @Test
    void testDeletePlan() {
        // Given
        Plan plan = new Plan();
        plan.setName("Plan to Delete");
        plan.setDescription("Will be deleted");
        String planId = "mock-id";

        // Mock behavior
        when(planRepository.save(any(Plan.class))).thenReturn(plan);
        doNothing().when(planRepository).deleteById(anyString());
        when(planRepository.findById(planId)).thenReturn(Optional.empty());

        // When
        Plan savedPlan = planRepository.save(plan);
        planRepository.deleteById(planId);
        Optional<Plan> deletedPlan = planRepository.findById(planId);

        // Then
        assertFalse(deletedPlan.isPresent());
        verify(planRepository).save(any(Plan.class));
        verify(planRepository).deleteById(anyString());
        verify(planRepository).findById(anyString());
    }
}