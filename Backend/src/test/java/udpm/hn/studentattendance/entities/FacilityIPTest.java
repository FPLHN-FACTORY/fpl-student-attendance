package udpm.hn.studentattendance.entities;

import org.junit.jupiter.api.Test;
import udpm.hn.studentattendance.infrastructure.constants.IPType;
import static org.junit.jupiter.api.Assertions.*;

class FacilityIPTest {
    @Test
    void testNoArgsConstructor() {
        FacilityIP facilityIP = new FacilityIP();
        assertNull(facilityIP.getType());
        assertNull(facilityIP.getIp());
        assertNull(facilityIP.getFacility());
    }

    @Test
    void testAllArgsConstructor() {
        Facility facility = new Facility();
        facility.setId("1");
        FacilityIP facilityIP = new FacilityIP(IPType.IPV4, "192.168.1.1", facility);
        assertEquals(IPType.IPV4, facilityIP.getType());
        assertEquals("192.168.1.1", facilityIP.getIp());
        assertEquals(facility, facilityIP.getFacility());
    }

    @Test
    void testSettersAndGetters() {
        FacilityIP facilityIP = new FacilityIP();
        Facility facility = new Facility();
        facility.setId("1");
        facilityIP.setType(IPType.IPV4);
        facilityIP.setIp("192.168.1.1");
        facilityIP.setFacility(facility);
        assertEquals(IPType.IPV4, facilityIP.getType());
        assertEquals("192.168.1.1", facilityIP.getIp());
        assertEquals(facility, facilityIP.getFacility());
    }

    @Test
    void testEqualsAndHashCode() {
        Facility facility = new Facility();
        facility.setId("1");
        FacilityIP ip1 = new FacilityIP();
        ip1.setId("1");
        ip1.setType(IPType.IPV4);
        ip1.setIp("192.168.1.1");
        ip1.setFacility(facility);

        FacilityIP ip2 = new FacilityIP();
        ip2.setId("1");
        ip2.setType(IPType.IPV4);
        ip2.setIp("192.168.1.1");
        ip2.setFacility(facility);

        FacilityIP ip3 = new FacilityIP();
        ip3.setId("2");
        ip3.setType(IPType.IPV4);
        ip3.setIp("192.168.1.1");
        ip3.setFacility(facility);

        assertEquals(ip1, ip2);
        assertNotEquals(ip1, ip3);
        assertEquals(ip1.hashCode(), ip2.hashCode());
        assertNotEquals(ip1.hashCode(), ip3.hashCode());
    }

    @Test
    void testToString() {
        Facility facility = new Facility();
        facility.setId("1");
        FacilityIP facilityIP = new FacilityIP();
        facilityIP.setId("1");
        facilityIP.setType(IPType.IPV4);
        facilityIP.setIp("192.168.1.1");
        facilityIP.setFacility(facility);
        String toString = facilityIP.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("FacilityIP"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("type=IPV4"));
        assertTrue(toString.contains("ip=192.168.1.1"));
    }

    @Test
    void testEqualsWithNull() {
        FacilityIP facilityIP = new FacilityIP();
        facilityIP.setId("1");
        assertNotEquals(null, facilityIP);
    }

    @Test
    void testEqualsWithDifferentClass() {
        FacilityIP facilityIP = new FacilityIP();
        facilityIP.setId("1");
        Object other = new Object();
        assertNotEquals(facilityIP, other);
    }

    @Test
    void testEqualsWithSameObject() {
        FacilityIP facilityIP = new FacilityIP();
        facilityIP.setId("1");
        assertEquals(facilityIP, facilityIP);
    }
}