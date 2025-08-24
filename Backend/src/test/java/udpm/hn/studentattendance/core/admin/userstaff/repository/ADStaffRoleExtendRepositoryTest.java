package udpm.hn.studentattendance.core.admin.userstaff.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;
import udpm.hn.studentattendance.entities.Role;
import udpm.hn.studentattendance.infrastructure.config.TestDatabaseConfig;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.infrastructure.config.TestDatabaseConfig;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;
import udpm.hn.studentattendance.repositories.UserStaffRepository;

@DataJpaTest
@Import(TestDatabaseConfig.class)
@ActiveProfiles("test")
class ADStaffRoleExtendRepositoryTest {
    @Autowired
    private ADStaffRoleExtendRepository adStaffRoleExtendRepository;

    @Autowired
    private UserStaffRepository userStaffRepository;

    @Test
    void testADStaffRoleExtendRepositoryExists() {
        assertNotNull(adStaffRoleExtendRepository);
    }

    @Test
    void testFindAll() {
        UserStaff staff = new UserStaff();
        staff.setName("Nguyen Van E");
        staff.setCode("STAFF005");
        staff.setEmailFe("e.fe@fpt.edu.vn");
        staff.setEmailFpt("e.fpt@fpt.edu.vn");
        UserStaff savedStaff = userStaffRepository.save(staff);

        Role role = new Role();
        role.setCode(RoleConstant.STAFF);
        role.setUserStaff(savedStaff);
        adStaffRoleExtendRepository.save(role);
        var result = adStaffRoleExtendRepository.findAll();
        assertNotNull(result);
        assertTrue(result.stream().anyMatch(r -> r.getCode() == RoleConstant.STAFF));
    }

    @Test
    void testFindById() {
        UserStaff staff = new UserStaff();
        staff.setName("Nguyen Van F");
        staff.setCode("STAFF006");
        staff.setEmailFe("f.fe@fpt.edu.vn");
        staff.setEmailFpt("f.fpt@fpt.edu.vn");
        UserStaff savedStaff = userStaffRepository.save(staff);

        Role role = new Role();
        role.setCode(RoleConstant.STAFF);
        role.setUserStaff(savedStaff);
        Role saved = adStaffRoleExtendRepository.save(role);
        var result = adStaffRoleExtendRepository.findById(saved.getId());
        assertTrue(result.isPresent());
        assertEquals(RoleConstant.STAFF, result.get().getCode());
    }

    @Test
    void testSave() {
        UserStaff staff = new UserStaff();
        staff.setName("Nguyen Van G");
        staff.setCode("STAFF007");
        staff.setEmailFe("g.fe@fpt.edu.vn");
        staff.setEmailFpt("g.fpt@fpt.edu.vn");
        UserStaff savedStaff = userStaffRepository.save(staff);

        Role role = new Role();
        role.setCode(RoleConstant.STAFF);
        role.setUserStaff(savedStaff);
        Role saved = adStaffRoleExtendRepository.save(role);
        assertNotNull(saved.getId());
        assertEquals(RoleConstant.STAFF, saved.getCode());
    }

    @Test
    void testDelete() {
        UserStaff staff = new UserStaff();
        staff.setName("Nguyen Van H");
        staff.setCode("STAFF008");
        staff.setEmailFe("h.fe@fpt.edu.vn");
        staff.setEmailFpt("h.fpt@fpt.edu.vn");
        UserStaff savedStaff = userStaffRepository.save(staff);

        Role role = new Role();
        role.setCode(RoleConstant.STAFF);
        role.setUserStaff(savedStaff);
        Role saved = adStaffRoleExtendRepository.save(role);
        adStaffRoleExtendRepository.deleteById(saved.getId());
        assertFalse(adStaffRoleExtendRepository.findById(saved.getId()).isPresent());
    }
}
