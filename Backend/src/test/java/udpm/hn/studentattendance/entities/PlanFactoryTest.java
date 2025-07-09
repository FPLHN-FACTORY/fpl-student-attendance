package udpm.hn.studentattendance.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlanFactoryTest {
    @Test
    void testNoArgsConstructor() {
        PlanFactory planFactory = new PlanFactory();
        assertNull(planFactory.getPlan());
        assertNull(planFactory.getFactory());
    }

    @Test
    void testAllArgsConstructor() {
        Plan plan = new Plan();
        plan.setId("1");
        Factory factory = new Factory();
        factory.setId("2");
        PlanFactory planFactory = new PlanFactory(plan, factory);
        assertEquals(plan, planFactory.getPlan());
        assertEquals(factory, planFactory.getFactory());
    }

    @Test
    void testSettersAndGetters() {
        PlanFactory planFactory = new PlanFactory();
        Plan plan = new Plan();
        plan.setId("1");
        Factory factory = new Factory();
        factory.setId("2");
        planFactory.setPlan(plan);
        planFactory.setFactory(factory);
        assertEquals(plan, planFactory.getPlan());
        assertEquals(factory, planFactory.getFactory());
    }

    @Test
    void testEqualsAndHashCode() {
        Plan plan = new Plan();
        plan.setId("1");
        Factory factory = new Factory();
        factory.setId("2");
        PlanFactory pf1 = new PlanFactory();
        pf1.setId("1");
        pf1.setPlan(plan);
        pf1.setFactory(factory);

        PlanFactory pf2 = new PlanFactory();
        pf2.setId("1");
        pf2.setPlan(plan);
        pf2.setFactory(factory);

        PlanFactory pf3 = new PlanFactory();
        pf3.setId("2");
        pf3.setPlan(plan);
        pf3.setFactory(factory);

        assertEquals(pf1, pf2);
        assertNotEquals(pf1, pf3);
        assertEquals(pf1.hashCode(), pf2.hashCode());
        assertNotEquals(pf1.hashCode(), pf3.hashCode());
    }

    @Test
    void testToString() {
        Plan plan = new Plan();
        plan.setId("1");
        Factory factory = new Factory();
        factory.setId("2");
        PlanFactory planFactory = new PlanFactory();
        planFactory.setId("1");
        planFactory.setPlan(plan);
        planFactory.setFactory(factory);
        String toString = planFactory.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("PlanFactory"));
        assertTrue(toString.contains("id=1"));
    }

    @Test
    void testEqualsWithNull() {
        PlanFactory planFactory = new PlanFactory();
        planFactory.setId("1");
        assertNotEquals(null, planFactory);
    }

    @Test
    void testEqualsWithDifferentClass() {
        PlanFactory planFactory = new PlanFactory();
        planFactory.setId("1");
        Object other = new Object();
        assertNotEquals(planFactory, other);
    }

    @Test
    void testEqualsWithSameObject() {
        PlanFactory planFactory = new PlanFactory();
        planFactory.setId("1");
        assertEquals(planFactory, planFactory);
    }
}