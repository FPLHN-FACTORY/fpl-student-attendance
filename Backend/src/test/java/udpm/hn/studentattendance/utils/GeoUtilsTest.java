package udpm.hn.studentattendance.utils;

import org.junit.jupiter.api.Test;
import udpm.hn.studentattendance.entities.FacilityLocation;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class GeoUtilsTest {
    @Test
    void testIsWithinRadius_true() {
        // Hà Nội: 21.0285, 105.8542; gần đó
        assertTrue(GeoUtils.isWithinRadius(21.0285, 105.8542, 21.0286, 105.8543, 20));
    }

    @Test
    void testIsWithinRadius_false() {
        // Hà Nội và TP.HCM
        assertFalse(GeoUtils.isWithinRadius(21.0285, 105.8542, 10.7769, 106.7009, 1000));
    }

    @Test
    void testIsAllowedLocation_emptyList() {
        List<FacilityLocation> lst = new ArrayList<>();
        assertTrue(GeoUtils.isAllowedLocation(lst, 0, 0));
    }

    @Test
    void testIsAllowedLocation_true() {
        FacilityLocation loc = new FacilityLocation();
        loc.setLatitude(21.0285);
        loc.setLongitude(105.8542);
        loc.setRadius(100);
        List<FacilityLocation> lst = List.of(loc);
        assertTrue(GeoUtils.isAllowedLocation(lst, 21.0286, 105.8543));
    }

    @Test
    void testIsAllowedLocation_false() {
        FacilityLocation loc = new FacilityLocation();
        loc.setLatitude(21.0285);
        loc.setLongitude(105.8542);
        loc.setRadius(10);
        List<FacilityLocation> lst = List.of(loc);
        assertFalse(GeoUtils.isAllowedLocation(lst, 10.7769, 106.7009));
    }
}