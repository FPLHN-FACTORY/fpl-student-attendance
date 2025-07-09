package udpm.hn.studentattendance.repositories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import udpm.hn.studentattendance.entities.Plan;
import udpm.hn.studentattendance.entities.PlanFactory;
import udpm.hn.studentattendance.entities.Semester;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ExtendWith(SpringExtension.class)
class PlanRepositoryTest {

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private PlanFactoryRepository planFactoryRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Test
    void testSaveAndFindById() {
        // Given
        Plan plan = new Plan();
        plan.setName("Spring 2024 Plan");
        plan.setDescription("Academic plan for Spring 2024");

        // When
        Plan savedPlan = planRepository.save(plan);
        Optional<Plan> foundPlan = planRepository.findById(savedPlan.getId());

        // Then
        assertTrue(foundPlan.isPresent());
        assertEquals("Spring 2024 Plan", foundPlan.get().getName());
        assertEquals("Academic plan for Spring 2024", foundPlan.get().getDescription());
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

        // When
        planRepository.save(plan1);
        planRepository.save(plan2);
        List<Plan> allPlans = planRepository.findAll();

        // Then
        assertTrue(allPlans.size() >= 2);
        assertTrue(allPlans.stream().anyMatch(p -> "Plan 1".equals(p.getName())));
        assertTrue(allPlans.stream().anyMatch(p -> "Plan 2".equals(p.getName())));
    }

    @Test
    void testUpdatePlan() {
        // Given
        Plan plan = new Plan();
        plan.setName("Original Name");
        plan.setDescription("Original Description");

        Plan savedPlan = planRepository.save(plan);

        // When
        savedPlan.setName("Updated Name");
        savedPlan.setDescription("Updated Description");
        Plan updatedPlan = planRepository.save(savedPlan);

        // Then
        assertEquals("Updated Name", updatedPlan.getName());
        assertEquals("Updated Description", updatedPlan.getDescription());
    }

    @Test
    void testDeletePlan() {
        // Given
        Plan plan = new Plan();
        plan.setName("Plan to Delete");
        plan.setDescription("Will be deleted");

        Plan savedPlan = planRepository.save(plan);
        String planId = savedPlan.getId();

        // When
        planRepository.deleteById(planId);
        Optional<Plan> deletedPlan = planRepository.findById(planId);

        // Then
        assertFalse(deletedPlan.isPresent());
    }
}