package udpm.hn.studentattendance.core.admin.facility.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import udpm.hn.studentattendance.entities.FacilityIP;
import udpm.hn.studentattendance.infrastructure.config.TestDatabaseConfig;

import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.IPType;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TestDatabaseConfig.class)
@ActiveProfiles("test")
class AFFacilityIPRepositoryTest {
    @Autowired
    private AFFacilityIPRepository afFacilityIPRepository;

    @Test
    void testAFFacilityIPRepositoryExists() {
        assertNotNull(afFacilityIPRepository);
    }

    @Test
    void testFindAll() {
        var result = afFacilityIPRepository.findAll();
        assertNotNull(result);
    }

    @Test
    void testFindById() {
        FacilityIP ip = new FacilityIP();
        ip.setIp("192.168.1.1");
        ip.setType(IPType.IPV4);
        ip.setStatus(EntityStatus.ACTIVE);
        FacilityIP saved = afFacilityIPRepository.save(ip);
        var result = afFacilityIPRepository.findById(saved.getId());
        assertTrue(result.isPresent());
        assertEquals("192.168.1.1", result.get().getIp());
    }

    @Test
    void testSave() {
        FacilityIP ip = new FacilityIP();
        ip.setIp("10.0.0.1");
        ip.setType(IPType.IPV4);
        ip.setStatus(EntityStatus.ACTIVE);
        FacilityIP saved = afFacilityIPRepository.save(ip);
        assertNotNull(saved.getId());
        assertEquals("10.0.0.1", saved.getIp());
    }

    @Test
    void testDelete() {
        FacilityIP ip = new FacilityIP();
        ip.setIp("172.16.0.1");
        ip.setType(IPType.IPV4);
        ip.setStatus(EntityStatus.ACTIVE);
        FacilityIP saved = afFacilityIPRepository.save(ip);
        afFacilityIPRepository.deleteById(saved.getId());
        assertFalse(afFacilityIPRepository.findById(saved.getId()).isPresent());
    }
}
