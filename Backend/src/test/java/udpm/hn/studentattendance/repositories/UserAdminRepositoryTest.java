package udpm.hn.studentattendance.repositories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import udpm.hn.studentattendance.entities.UserAdmin;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAdminRepositoryTest {

    @Mock
    private UserAdminRepository userAdminRepository;

    @Test
    void testSaveAndFindById() {
        // Given
        UserAdmin admin = new UserAdmin();
        admin.setCode("ADM001");
        admin.setName("Admin User");
        admin.setEmail("admin@fpt.edu.vn");
        admin.setImage("admin_image.jpg");

        // Mock behavior
        when(userAdminRepository.save(any(UserAdmin.class))).thenReturn(admin);
        when(userAdminRepository.findById(anyString())).thenReturn(Optional.of(admin));

        // When
        UserAdmin savedAdmin = userAdminRepository.save(admin);
        Optional<UserAdmin> foundAdmin = userAdminRepository.findById("mock-id");

        // Then
        assertTrue(foundAdmin.isPresent());
        assertEquals("ADM001", foundAdmin.get().getCode());
        assertEquals("Admin User", foundAdmin.get().getName());
        assertEquals("admin@fpt.edu.vn", foundAdmin.get().getEmail());
        assertEquals("admin_image.jpg", foundAdmin.get().getImage());
        verify(userAdminRepository).save(any(UserAdmin.class));
        verify(userAdminRepository).findById(anyString());
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

        List<UserAdmin> admins = Arrays.asList(admin1, admin2);

        // Mock behavior
        when(userAdminRepository.findAll()).thenReturn(admins);
        when(userAdminRepository.save(any(UserAdmin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        userAdminRepository.save(admin1);
        userAdminRepository.save(admin2);
        List<UserAdmin> allAdmins = userAdminRepository.findAll();

        // Then
        assertEquals(2, allAdmins.size());
        assertTrue(allAdmins.stream().anyMatch(a -> "ADM001".equals(a.getCode())));
        assertTrue(allAdmins.stream().anyMatch(a -> "ADM002".equals(a.getCode())));
        verify(userAdminRepository, times(2)).save(any(UserAdmin.class));
        verify(userAdminRepository).findAll();
    }

    @Test
    void testUpdateAdmin() {
        // Given
        UserAdmin admin = new UserAdmin();
        admin.setCode("ADM001");
        admin.setName("Original Name");
        admin.setEmail("original@fpt.edu.vn");

        UserAdmin updatedAdmin = new UserAdmin();
        updatedAdmin.setCode("ADM001");
        updatedAdmin.setName("Updated Name");
        updatedAdmin.setEmail("updated@fpt.edu.vn");

        // Mock behavior
        when(userAdminRepository.save(any(UserAdmin.class))).thenReturn(admin).thenReturn(updatedAdmin);

        // When
        UserAdmin savedAdmin = userAdminRepository.save(admin);
        savedAdmin.setName("Updated Name");
        savedAdmin.setEmail("updated@fpt.edu.vn");
        UserAdmin resultAdmin = userAdminRepository.save(savedAdmin);

        // Then
        assertEquals("Updated Name", resultAdmin.getName());
        assertEquals("updated@fpt.edu.vn", resultAdmin.getEmail());
        verify(userAdminRepository, times(2)).save(any(UserAdmin.class));
    }

    @Test
    void testDeleteAdmin() {
        // Given
        UserAdmin admin = new UserAdmin();
        admin.setCode("ADM001");
        admin.setName("Admin to Delete");
        admin.setEmail("delete@fpt.edu.vn");
        String adminId = "mock-id";

        // Mock behavior
        when(userAdminRepository.save(any(UserAdmin.class))).thenReturn(admin);
        doNothing().when(userAdminRepository).deleteById(anyString());
        when(userAdminRepository.findById(adminId)).thenReturn(Optional.empty());

        // When
        UserAdmin savedAdmin = userAdminRepository.save(admin);
        userAdminRepository.deleteById(adminId);
        Optional<UserAdmin> deletedAdmin = userAdminRepository.findById(adminId);

        // Then
        assertFalse(deletedAdmin.isPresent());
        verify(userAdminRepository).save(any(UserAdmin.class));
        verify(userAdminRepository).deleteById(anyString());
        verify(userAdminRepository).findById(anyString());
    }
}
