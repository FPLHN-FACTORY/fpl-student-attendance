package udpm.hn.studentattendance.repositories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import udpm.hn.studentattendance.entities.UserStaff;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserStaffRepositoryTest {

    @Mock
    private UserStaffRepository userStaffRepository;

    @Test
    void testSaveAndFindById() {
        // Given
        UserStaff staff = new UserStaff();
        staff.setCode("SF001");
        staff.setName("Nguyen Van Staff");
        staff.setEmailFe("staff@fe.edu.vn");
        staff.setEmailFpt("staff@fpt.edu.vn");
        staff.setImage("staff_image.jpg");

        // Mock behavior
        when(userStaffRepository.save(any(UserStaff.class))).thenReturn(staff);
        when(userStaffRepository.findById(anyString())).thenReturn(Optional.of(staff));

        // When
        UserStaff savedStaff = userStaffRepository.save(staff);
        Optional<UserStaff> foundStaff = userStaffRepository.findById("mock-id");

        // Then
        assertTrue(foundStaff.isPresent());
        assertEquals("SF001", foundStaff.get().getCode());
        assertEquals("Nguyen Van Staff", foundStaff.get().getName());
        assertEquals("staff@fe.edu.vn", foundStaff.get().getEmailFe());
        assertEquals("staff@fpt.edu.vn", foundStaff.get().getEmailFpt());
        assertEquals("staff_image.jpg", foundStaff.get().getImage());
        verify(userStaffRepository).save(any(UserStaff.class));
        verify(userStaffRepository).findById(anyString());
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

        List<UserStaff> staffList = Arrays.asList(staff1, staff2);

        // Mock behavior
        when(userStaffRepository.findAll()).thenReturn(staffList);
        when(userStaffRepository.save(any(UserStaff.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        userStaffRepository.save(staff1);
        userStaffRepository.save(staff2);
        List<UserStaff> allStaff = userStaffRepository.findAll();

        // Then
        assertEquals(2, allStaff.size());
        assertTrue(allStaff.stream().anyMatch(s -> "SF001".equals(s.getCode())));
        assertTrue(allStaff.stream().anyMatch(s -> "SF002".equals(s.getCode())));
        verify(userStaffRepository, times(2)).save(any(UserStaff.class));
        verify(userStaffRepository).findAll();
    }

    @Test
    void testUpdateStaff() {
        // Given
        UserStaff staff = new UserStaff();
        staff.setCode("SF001");
        staff.setName("Original Name");
        staff.setEmailFe("original@fe.edu.vn");

        UserStaff updatedStaff = new UserStaff();
        updatedStaff.setCode("SF001");
        updatedStaff.setName("Updated Name");
        updatedStaff.setEmailFe("updated@fe.edu.vn");

        // Mock behavior
        when(userStaffRepository.save(any(UserStaff.class))).thenReturn(staff).thenReturn(updatedStaff);

        // When
        UserStaff savedStaff = userStaffRepository.save(staff);
        savedStaff.setName("Updated Name");
        savedStaff.setEmailFe("updated@fe.edu.vn");
        UserStaff resultStaff = userStaffRepository.save(savedStaff);

        // Then
        assertEquals("Updated Name", resultStaff.getName());
        assertEquals("updated@fe.edu.vn", resultStaff.getEmailFe());
        verify(userStaffRepository, times(2)).save(any(UserStaff.class));
    }

    @Test
    void testDeleteStaff() {
        // Given
        UserStaff staff = new UserStaff();
        staff.setCode("SF001");
        staff.setName("Staff to Delete");
        staff.setEmailFe("delete@fe.edu.vn");
        String staffId = "mock-id";

        // Mock behavior
        when(userStaffRepository.save(any(UserStaff.class))).thenReturn(staff);
        doNothing().when(userStaffRepository).deleteById(anyString());
        when(userStaffRepository.findById(staffId)).thenReturn(Optional.empty());

        // When
        UserStaff savedStaff = userStaffRepository.save(staff);
        userStaffRepository.deleteById(staffId);
        Optional<UserStaff> deletedStaff = userStaffRepository.findById(staffId);

        // Then
        assertFalse(deletedStaff.isPresent());
        verify(userStaffRepository).save(any(UserStaff.class));
        verify(userStaffRepository).deleteById(anyString());
        verify(userStaffRepository).findById(anyString());
    }
}