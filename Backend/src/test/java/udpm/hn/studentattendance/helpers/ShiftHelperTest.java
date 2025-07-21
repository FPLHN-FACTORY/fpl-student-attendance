package udpm.hn.studentattendance.helpers;

import org.junit.jupiter.api.Test;
import udpm.hn.studentattendance.entities.FacilityShift;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ShiftHelperTest {
    @Test
    void canInstantiate() {
        ShiftHelper helper = new ShiftHelper();
        assertThat(helper).isNotNull();
    }

    @Test
    void testGetShiftTimeStartWithTimestamp() {
        long now = System.currentTimeMillis();
        LocalTime start = LocalTime.of(8, 0);
        Long result = ShiftHelper.getShiftTimeStart(now, start);
        assertThat(result).isNotNull();
        assertThat(result).isGreaterThan(0L);
    }

    @Test
    void testGetShiftTimeStartWithLocalDate() {
        LocalDate date = LocalDate.now();
        LocalTime start = LocalTime.of(8, 0);
        Long result = ShiftHelper.getShiftTimeStart(date, start);
        assertThat(result).isNotNull();
        assertThat(result).isGreaterThan(0L);
    }

    @Test
    void testGetShiftTimeEndWithTimestamp() {
        long now = System.currentTimeMillis();
        LocalTime end = LocalTime.of(10, 0);
        Long result = ShiftHelper.getShiftTimeEnd(now, end);
        assertThat(result).isNotNull();
        assertThat(result).isGreaterThan(0L);
    }

    @Test
    void testGetShiftTimeEndWithLocalDate() {
        LocalDate date = LocalDate.now();
        LocalTime end = LocalTime.of(10, 0);
        Long result = ShiftHelper.getShiftTimeEnd(date, end);
        assertThat(result).isNotNull();
        assertThat(result).isGreaterThan(0L);
    }

    @Test
    void testGetDiffTimeWithInts() {
        int diff = ShiftHelper.getDiffTime(8, 0, 10, 0);
        assertThat(diff).isEqualTo(2 * 60 * 60 * 1000);
    }

    @Test
    void testGetDiffTimeWithFacilityShift() {
        FacilityShift shift = new FacilityShift();
        shift.setFromHour(8);
        shift.setFromMinute(0);
        shift.setToHour(9);
        shift.setToMinute(30);
        int diff = ShiftHelper.getDiffTime(shift);
        assertThat(diff).isEqualTo((1 * 60 * 60 + 30 * 60) * 1000);
    }

    @Test
    void testFindConsecutiveShiftNullOrEmpty() {
        assertThat(ShiftHelper.findConsecutiveShift(null)).isEmpty();
        assertThat(ShiftHelper.findConsecutiveShift(Collections.emptyList())).isEmpty();
    }

    @Test
    void testFindConsecutiveShiftSingle() {
        List<List<Integer>> result = ShiftHelper.findConsecutiveShift(Arrays.asList(2));
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).containsExactly(2);
    }

    @Test
    void testFindConsecutiveShiftMultipleGroups() {
        List<List<Integer>> result = ShiftHelper.findConsecutiveShift(Arrays.asList(1, 2, 4, 5, 7));
        assertThat(result).hasSize(3);
        assertThat(result.get(0)).containsExactly(1, 2);
        assertThat(result.get(1)).containsExactly(4, 5);
        assertThat(result.get(2)).containsExactly(7);
    }

    @Test
    void testGetShiftsStringNullOrEmpty() {
        assertThat(ShiftHelper.getShiftsString(null)).isEqualTo("Không xác định");
        assertThat(ShiftHelper.getShiftsString(Collections.emptyList())).isEqualTo("Không xác định");
    }

    @Test
    void testGetShiftsStringSingle() {
        assertThat(ShiftHelper.getShiftsString(Arrays.asList(3))).isEqualTo("Ca 3");
    }

    @Test
    void testGetShiftsStringConsecutive() {
        assertThat(ShiftHelper.getShiftsString(Arrays.asList(1, 2, 3))).isEqualTo("Ca 1-3");
    }

    @Test
    void testGetShiftsStringMultipleGroups() {
        assertThat(ShiftHelper.getShiftsString(Arrays.asList(1, 2, 4, 5, 7))).isEqualTo("Ca 1-2, Ca 4-5, Ca 7");
    }
}
