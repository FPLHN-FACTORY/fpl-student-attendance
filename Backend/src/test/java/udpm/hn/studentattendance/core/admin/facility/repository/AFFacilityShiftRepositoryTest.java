package udpm.hn.studentattendance.core.admin.facility.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import udpm.hn.studentattendance.entities.FacilityShift;
import udpm.hn.studentattendance.infrastructure.config.TestDatabaseConfig;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.infrastructure.config.TestDatabaseConfig;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TestDatabaseConfig.class)
@ActiveProfiles("test")
class AFFacilityShiftRepositoryTest {
    @Autowired
    private AFFacilityShiftRepository afFacilityShiftRepository;

    @Test
    void testAFFacilityShiftRepositoryExists() {
        assertNotNull(afFacilityShiftRepository);
    }

    @Test
    void testFindAll() {
        var result = afFacilityShiftRepository.findAll();
        assertNotNull(result);
    }

    @Test
    void testFindById() {
        FacilityShift shift = new FacilityShift();
        shift.setShift(1);
        shift.setFromHour(7);
        shift.setFromMinute(0);
        shift.setToHour(9);
        shift.setToMinute(0);
        shift.setStatus(EntityStatus.ACTIVE);
        FacilityShift saved = afFacilityShiftRepository.save(shift);
        var result = afFacilityShiftRepository.findById(saved.getId());
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getShift());
    }

    @Test
    void testSave() {
        FacilityShift shift = new FacilityShift();
        shift.setShift(2);
        shift.setFromHour(9);
        shift.setFromMinute(0);
        shift.setToHour(11);
        shift.setToMinute(0);
        shift.setStatus(EntityStatus.ACTIVE);
        FacilityShift saved = afFacilityShiftRepository.save(shift);
        assertNotNull(saved.getId());
        assertEquals(2, saved.getShift());
    }

    @Test
    void testDelete() {
        FacilityShift shift = new FacilityShift();
        shift.setShift(3);
        shift.setFromHour(13);
        shift.setFromMinute(0);
        shift.setToHour(15);
        shift.setToMinute(0);
        shift.setStatus(EntityStatus.ACTIVE);
        FacilityShift saved = afFacilityShiftRepository.save(shift);
        afFacilityShiftRepository.deleteById(saved.getId());
        assertFalse(afFacilityShiftRepository.findById(saved.getId()).isPresent());
    }
}