package udpm.hn.studentattendance.core.admin.userstaff.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.infrastructure.config.TestDatabaseConfig;

@DataJpaTest
@Import(TestDatabaseConfig.class)
@ActiveProfiles("test")
class ADStaffFacilityExtendRepositoryTest {
    @Autowired
    private ADStaffFacilityExtendRepository adStaffFacilityExtendRepository;

    @Test
    void testADStaffFacilityExtendRepositoryExists() {
        assertNotNull(adStaffFacilityExtendRepository);
    }

    @Test
    void testFindAll() {
        Facility facility = new Facility();
        facility.setName("CÆ¡ sá»Ÿ 1");
        facility.setCode("CS001");
        facility.setPosition(1);
        adStaffFacilityExtendRepository.save(facility);
        var result = adStaffFacilityExtendRepository.findAll();
        assertNotNull(result);
        assertTrue(result.stream().anyMatch(f -> "CS001".equals(f.getCode())));
    }

    @Test
    void testFindById() {
        Facility facility = new Facility();
        facility.setName("CÆ¡ sá»Ÿ 2");
        facility.setCode("CS002");
        facility.setPosition(2);
        Facility saved = adStaffFacilityExtendRepository.save(facility);
        var result = adStaffFacilityExtendRepository.findById(saved.getId());
        assertTrue(result.isPresent());
        assertEquals("CS002", result.get().getCode());
    }

    @Test
    void testSave() {
        Facility facility = new Facility();
        facility.setName("CÆ¡ sá»Ÿ 3");
        facility.setCode("CS003");
        facility.setPosition(3);
        Facility saved = adStaffFacilityExtendRepository.save(facility);
        assertNotNull(saved.getId());
        assertEquals("CS003", saved.getCode());
    }

    @Test
    void testDelete() {
        Facility facility = new Facility();
        facility.setName("CÆ¡ sá»Ÿ 4");
        facility.setCode("CS004");
        facility.setPosition(4);
        Facility saved = adStaffFacilityExtendRepository.save(facility);
        adStaffFacilityExtendRepository.deleteById(saved.getId());
        assertFalse(adStaffFacilityExtendRepository.findById(saved.getId()).isPresent());
    }
}