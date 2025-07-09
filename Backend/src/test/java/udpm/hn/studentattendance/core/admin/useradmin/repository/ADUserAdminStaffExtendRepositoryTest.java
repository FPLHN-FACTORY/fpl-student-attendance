package udpm.hn.studentattendance.core.admin.useradmin.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import udpm.hn.studentattendance.entities.UserStaff;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ADUserAdminStaffExtendRepositoryTest {
    @Autowired
    private ADUserAdminStaffExtendRepository adUserAdminStaffExtendRepository;

    @Test
    void testADUserAdminStaffExtendRepositoryExists() {
        assertNotNull(adUserAdminStaffExtendRepository);
    }

    @Test
    void testFindAll() {
        var result = adUserAdminStaffExtendRepository.findAll();
        assertNotNull(result);
    }

    @Test
    void testFindById() {
        UserStaff staff = new UserStaff();
        staff.setCode("S001");
        staff.setEmailFe("staff1@fe.edu.vn");
        staff.setName("Nguyen Van Staff");
        staff.setImage("staff1.png");
        UserStaff saved = adUserAdminStaffExtendRepository.save(staff);
        var result = adUserAdminStaffExtendRepository.findById(saved.getId());
        assertTrue(result.isPresent());
        assertEquals("S001", result.get().getCode());
    }

    @Test
    void testSave() {
        UserStaff staff = new UserStaff();
        staff.setCode("S002");
        staff.setEmailFe("staff2@fe.edu.vn");
        staff.setName("Nguyen Van Staff 2");
        staff.setImage("staff2.png");
        UserStaff saved = adUserAdminStaffExtendRepository.save(staff);
        assertNotNull(saved.getId());
        assertEquals("S002", saved.getCode());
    }

    @Test
    void testDelete() {
        UserStaff staff = new UserStaff();
        staff.setCode("S003");
        staff.setEmailFe("staff3@fe.edu.vn");
        staff.setName("Nguyen Van Staff 3");
        staff.setImage("staff3.png");
        UserStaff saved = adUserAdminStaffExtendRepository.save(staff);
        adUserAdminStaffExtendRepository.deleteById(saved.getId());
        assertFalse(adUserAdminStaffExtendRepository.findById(saved.getId()).isPresent());
    }
}