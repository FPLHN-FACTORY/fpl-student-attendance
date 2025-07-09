package udpm.hn.studentattendance.repositories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import udpm.hn.studentattendance.entities.Role;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ExtendWith(SpringExtension.class)
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void testSaveAndFindById() {
        // Given
        Role role = new Role();
        // No setName or setDescription methods available

        // When
        Role savedRole = roleRepository.save(role);
        Optional<Role> foundRole = roleRepository.findById(savedRole.getId());

        // Then
        assertTrue(foundRole.isPresent());
    }
}