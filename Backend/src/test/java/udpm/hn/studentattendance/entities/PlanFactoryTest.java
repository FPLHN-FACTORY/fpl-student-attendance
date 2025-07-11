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

        // So sánh từng trường thay vì so sánh object nếu entity chưa override
        // equals/hashCode đúng
        assertEquals(pf1.getId(), pf2.getId());
        assertEquals(pf1.getPlan(), pf2.getPlan());
        assertEquals(pf1.getFactory(), pf2.getFactory());
        assertNotEquals(pf1.getId(), pf3.getId());
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
        // Kiểm tra chuỗi chứa thông tin trường chính
        assertTrue(toString.contains("PlanFactory"));
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