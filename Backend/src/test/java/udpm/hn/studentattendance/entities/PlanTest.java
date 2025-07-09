package udpm.hn.studentattendance.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlanTest {
    @Test
    void testNoArgsConstructor() {
        Plan plan = new Plan();
        assertNull(plan.getName());
        assertNull(plan.getDescription());
        assertNull(plan.getFromDate());
        assertNull(plan.getToDate());
        assertEquals(0, plan.getMaxLateArrival());
        assertNull(plan.getProject());
    }

    @Test
    void testAllArgsConstructor() {
        Project project = new Project();
        project.setId("1");
        Plan plan = new Plan("Plan 1", "Description", 1000L, 2000L, 5, project);
        assertEquals("Plan 1", plan.getName());
        assertEquals("Description", plan.getDescription());
        assertEquals(1000L, plan.getFromDate());
        assertEquals(2000L, plan.getToDate());
        assertEquals(5, plan.getMaxLateArrival());
        assertEquals(project, plan.getProject());
    }

    @Test
    void testSettersAndGetters() {
        Plan plan = new Plan();
        Project project = new Project();
        project.setId("1");
        plan.setName("Plan 1");
        plan.setDescription("Description");
        plan.setFromDate(1000L);
        plan.setToDate(2000L);
        plan.setMaxLateArrival(5);
        plan.setProject(project);
        assertEquals("Plan 1", plan.getName());
        assertEquals("Description", plan.getDescription());
        assertEquals(1000L, plan.getFromDate());
        assertEquals(2000L, plan.getToDate());
        assertEquals(5, plan.getMaxLateArrival());
        assertEquals(project, plan.getProject());
    }

    @Test
    void testEqualsAndHashCode() {
        Project project = new Project();
        project.setId("1");
        Plan p1 = new Plan();
        p1.setId("1");
        p1.setName("Plan 1");
        p1.setDescription("Description");
        p1.setFromDate(1000L);
        p1.setToDate(2000L);
        p1.setMaxLateArrival(5);
        p1.setProject(project);

        Plan p2 = new Plan();
        p2.setId("1");
        p2.setName("Plan 1");
        p2.setDescription("Description");
        p2.setFromDate(1000L);
        p2.setToDate(2000L);
        p2.setMaxLateArrival(5);
        p2.setProject(project);

        Plan p3 = new Plan();
        p3.setId("2");
        p3.setName("Plan 1");
        p3.setDescription("Description");
        p3.setFromDate(1000L);
        p3.setToDate(2000L);
        p3.setMaxLateArrival(5);
        p3.setProject(project);

        assertEquals(p1, p2);
        assertNotEquals(p1, p3);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1.hashCode(), p3.hashCode());
    }

    @Test
    void testToString() {
        Project project = new Project();
        project.setId("1");
        Plan plan = new Plan();
        plan.setId("1");
        plan.setName("Plan 1");
        plan.setDescription("Description");
        plan.setFromDate(1000L);
        plan.setToDate(2000L);
        plan.setMaxLateArrival(5);
        plan.setProject(project);
        String toString = plan.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Plan"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("name=Plan 1"));
        assertTrue(toString.contains("description=Description"));
        assertTrue(toString.contains("fromDate=1000"));
        assertTrue(toString.contains("toDate=2000"));
        assertTrue(toString.contains("maxLateArrival=5"));
    }

    @Test
    void testEqualsWithNull() {
        Plan plan = new Plan();
        plan.setId("1");
        assertNotEquals(null, plan);
    }

    @Test
    void testEqualsWithDifferentClass() {
        Plan plan = new Plan();
        plan.setId("1");
        Object other = new Object();
        assertNotEquals(plan, other);
    }

    @Test
    void testEqualsWithSameObject() {
        Plan plan = new Plan();
        plan.setId("1");
        assertEquals(plan, plan);
    }
}