package udpm.hn.studentattendance.core.admin.userstaff.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;
import udpm.hn.studentattendance.entities.UserStaff;

@DataJpaTest
@ActiveProfiles("test")
class ADStaffExtendRepositoryTest {
    @Autowired
    private ADStaffExtendRepository adStaffExtendRepository;

    @Test
    void testADStaffExtendRepositoryExists() {
        assertNotNull(adStaffExtendRepository);
    }

    @Test
    void testFindAll() {
        UserStaff staff = new UserStaff();
        staff.setName("Nguyen Van A");
        staff.setCode("STAFF001");
        staff.setEmailFe("a.fe@fpt.edu.vn");
        staff.setEmailFpt("a.fpt@fpt.edu.vn");
        adStaffExtendRepository.save(staff);
        var result = adStaffExtendRepository.findAll();
        assertNotNull(result);
        assertTrue(result.stream().anyMatch(s -> "STAFF001".equals(s.getCode())));
    }

    @Test
    void testFindById() {
        UserStaff staff = new UserStaff();
        staff.setName("Nguyen Van B");
        staff.setCode("STAFF002");
        staff.setEmailFe("b.fe@fpt.edu.vn");
        staff.setEmailFpt("b.fpt@fpt.edu.vn");
        UserStaff saved = adStaffExtendRepository.save(staff);
        var result = adStaffExtendRepository.findById(saved.getId());
        assertTrue(result.isPresent());
        assertEquals("STAFF002", result.get().getCode());
    }

    @Test
    void testSave() {
        UserStaff staff = new UserStaff();
        staff.setName("Nguyen Van C");
        staff.setCode("STAFF003");
        staff.setEmailFe("c.fe@fpt.edu.vn");
        staff.setEmailFpt("c.fpt@fpt.edu.vn");
        UserStaff saved = adStaffExtendRepository.save(staff);
        assertNotNull(saved.getId());
        assertEquals("STAFF003", saved.getCode());
    }

    @Test
    void testDelete() {
        UserStaff staff = new UserStaff();
        staff.setName("Nguyen Van D");
        staff.setCode("STAFF004");
        staff.setEmailFe("d.fe@fpt.edu.vn");
        staff.setEmailFpt("d.fpt@fpt.edu.vn");
        UserStaff saved = adStaffExtendRepository.save(staff);
        adStaffExtendRepository.deleteById(saved.getId());
        assertFalse(adStaffExtendRepository.findById(saved.getId()).isPresent());
    }
}