package udpm.hn.studentattendance.repositories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import udpm.hn.studentattendance.entities.Role;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;
import udpm.hn.studentattendance.infrastructure.config.TestDatabaseConfig;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@Import(TestDatabaseConfig.class)
class UserStaffRepositoryTest {

    @Autowired
    private UserStaffRepository userStaffRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void testSaveAndFindById() {
        // Given
        UserStaff staff = new UserStaff();
        staff.setCode("SF001");
        staff.setName("Nguyen Van Staff");
        staff.setEmailFe("staff@fe.edu.vn");
        staff.setEmailFpt("staff@fpt.edu.vn");
        staff.setImage("staff_image.jpg");

        // When
        UserStaff savedStaff = userStaffRepository.save(staff);
        Optional<UserStaff> foundStaff = userStaffRepository.findById(savedStaff.getId());

        // Then
        assertTrue(foundStaff.isPresent());
        assertEquals("SF001", foundStaff.get().getCode());
        assertEquals("Nguyen Van Staff", foundStaff.get().getName());
        assertEquals("staff@fe.edu.vn", foundStaff.get().getEmailFe());
        assertEquals("staff@fpt.edu.vn", foundStaff.get().getEmailFpt());
        assertEquals("staff_image.jpg", foundStaff.get().getImage());
    }

    @Test
    void testSaveAndFindAll() {
        // Given
        UserStaff staff1 = new UserStaff();
        staff1.setCode("SF001");
        staff1.setName("Staff 1");
        staff1.setEmailFe("staff1@fe.edu.vn");

        UserStaff staff2 = new UserStaff();
        staff2.setCode("SF002");
        staff2.setName("Staff 2");
        staff2.setEmailFe("staff2@fe.edu.vn");

        // When
        userStaffRepository.save(staff1);
        userStaffRepository.save(staff2);
        List<UserStaff> allStaff = userStaffRepository.findAll();

        // Then
        assertTrue(allStaff.size() >= 2);
        assertTrue(allStaff.stream().anyMatch(s -> "SF001".equals(s.getCode())));
        assertTrue(allStaff.stream().anyMatch(s -> "SF002".equals(s.getCode())));
    }

    @Test
    void testUpdateStaff() {
        // Given
        UserStaff staff = new UserStaff();
        staff.setCode("SF001");
        staff.setName("Original Name");
        staff.setEmailFe("original@fe.edu.vn");

        UserStaff savedStaff = userStaffRepository.save(staff);

        // When
        savedStaff.setName("Updated Name");
        savedStaff.setEmailFe("updated@fe.edu.vn");
        UserStaff updatedStaff = userStaffRepository.save(savedStaff);

        // Then
        assertEquals("Updated Name", updatedStaff.getName());
        assertEquals("updated@fe.edu.vn", updatedStaff.getEmailFe());
    }

    @Test
    void testDeleteStaff() {
        // Given
        UserStaff staff = new UserStaff();
        staff.setCode("SF001");
        staff.setName("Staff to Delete");
        staff.setEmailFe("delete@fe.edu.vn");

        UserStaff savedStaff = userStaffRepository.save(staff);
        String staffId = savedStaff.getId();

        // When
        userStaffRepository.deleteById(staffId);
        Optional<UserStaff> deletedStaff = userStaffRepository.findById(staffId);

        // Then
        assertFalse(deletedStaff.isPresent());
    }
}