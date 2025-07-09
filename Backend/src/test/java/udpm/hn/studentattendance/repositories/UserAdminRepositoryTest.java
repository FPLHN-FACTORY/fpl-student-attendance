package udpm.hn.studentattendance.repositories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import udpm.hn.studentattendance.entities.UserAdmin;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ExtendWith(SpringExtension.class)
class UserAdminRepositoryTest {

    @Autowired
    private UserAdminRepository userAdminRepository;

    @Test
    void testSaveAndFindById() {
        // Given
        UserAdmin admin = new UserAdmin();
        admin.setCode("ADM001");
        admin.setName("Admin User");
        admin.setEmail("admin@fpt.edu.vn");
        admin.setImage("admin_image.jpg");

        // When
        UserAdmin savedAdmin = userAdminRepository.save(admin);
        Optional<UserAdmin> foundAdmin = userAdminRepository.findById(savedAdmin.getId());

        // Then
        assertTrue(foundAdmin.isPresent());
        assertEquals("ADM001", foundAdmin.get().getCode());
        assertEquals("Admin User", foundAdmin.get().getName());
        assertEquals("admin@fpt.edu.vn", foundAdmin.get().getEmail());
        assertEquals("admin_image.jpg", foundAdmin.get().getImage());
    }

    @Test
    void testSaveAndFindAll() {
        // Given
        UserAdmin admin1 = new UserAdmin();
        admin1.setCode("ADM001");
        admin1.setName("Admin 1");
        admin1.setEmail("admin1@fpt.edu.vn");

        UserAdmin admin2 = new UserAdmin();
        admin2.setCode("ADM002");
        admin2.setName("Admin 2");
        admin2.setEmail("admin2@fpt.edu.vn");

        // When
        userAdminRepository.save(admin1);
        userAdminRepository.save(admin2);
        List<UserAdmin> allAdmins = userAdminRepository.findAll();

        // Then
        assertTrue(allAdmins.size() >= 2);
        assertTrue(allAdmins.stream().anyMatch(a -> "ADM001".equals(a.getCode())));
        assertTrue(allAdmins.stream().anyMatch(a -> "ADM002".equals(a.getCode())));
    }

    @Test
    void testUpdateAdmin() {
        // Given
        UserAdmin admin = new UserAdmin();
        admin.setCode("ADM001");
        admin.setName("Original Name");
        admin.setEmail("original@fpt.edu.vn");

        UserAdmin savedAdmin = userAdminRepository.save(admin);

        // When
        savedAdmin.setName("Updated Name");
        savedAdmin.setEmail("updated@fpt.edu.vn");
        UserAdmin updatedAdmin = userAdminRepository.save(savedAdmin);

        // Then
        assertEquals("Updated Name", updatedAdmin.getName());
        assertEquals("updated@fpt.edu.vn", updatedAdmin.getEmail());
    }

    @Test
    void testDeleteAdmin() {
        // Given
        UserAdmin admin = new UserAdmin();
        admin.setCode("ADM001");
        admin.setName("Admin to Delete");
        admin.setEmail("delete@fpt.edu.vn");

        UserAdmin savedAdmin = userAdminRepository.save(admin);
        String adminId = savedAdmin.getId();

        // When
        userAdminRepository.deleteById(adminId);
        Optional<UserAdmin> deletedAdmin = userAdminRepository.findById(adminId);

        // Then
        assertFalse(deletedAdmin.isPresent());
    }
}