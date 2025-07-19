package udpm.hn.studentattendance.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserStudentRepositoryTest {

    @Mock
    private UserStudentRepository userStudentRepository;

    @Mock
    private FacilityRepository facilityRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveAndFindById() {
        // Given
        UserStudent student = new UserStudent();
        student.setCode("ST001");
        student.setName("Nguyen Van A");
        student.setEmail("student@fpt.edu.vn");
        student.setImage("image.jpg");
        student.setFaceEmbedding("face_embedding_data");

        UserStudent savedStudent = new UserStudent();
        savedStudent.setId("student-1");
        savedStudent.setCode("ST001");
        savedStudent.setName("Nguyen Van A");
        savedStudent.setEmail("student@fpt.edu.vn");
        savedStudent.setImage("image.jpg");
        savedStudent.setFaceEmbedding("face_embedding_data");

        when(userStudentRepository.save(any(UserStudent.class))).thenReturn(savedStudent);
        when(userStudentRepository.findById("student-1")).thenReturn(Optional.of(savedStudent));

        // When
        UserStudent result = userStudentRepository.save(student);
        Optional<UserStudent> foundStudent = userStudentRepository.findById(result.getId());

        // Then
        assertTrue(foundStudent.isPresent());
        assertEquals("ST001", foundStudent.get().getCode());
        assertEquals("Nguyen Van A", foundStudent.get().getName());
        assertEquals("student@fpt.edu.vn", foundStudent.get().getEmail());
        assertEquals("image.jpg", foundStudent.get().getImage());
        assertEquals("face_embedding_data", foundStudent.get().getFaceEmbedding());

        verify(userStudentRepository).save(any(UserStudent.class));
        verify(userStudentRepository).findById("student-1");
    }

    @Test
    void testSaveAndFindAll() {
        // Given
        UserStudent student1 = new UserStudent();
        student1.setId("student-1");
        student1.setCode("ST001");
        student1.setName("Student 1");
        student1.setEmail("student1@fpt.edu.vn");

        UserStudent student2 = new UserStudent();
        student2.setId("student-2");
        student2.setCode("ST002");
        student2.setName("Student 2");
        student2.setEmail("student2@fpt.edu.vn");

        List<UserStudent> allStudents = List.of(student1, student2);

        when(userStudentRepository.save(any(UserStudent.class))).thenReturn(student1).thenReturn(student2);
        when(userStudentRepository.findAll()).thenReturn(allStudents);

        // When
        userStudentRepository.save(student1);
        userStudentRepository.save(student2);
        List<UserStudent> result = userStudentRepository.findAll();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(s -> "ST001".equals(s.getCode())));
        assertTrue(result.stream().anyMatch(s -> "ST002".equals(s.getCode())));

        verify(userStudentRepository, times(2)).save(any(UserStudent.class));
        verify(userStudentRepository).findAll();
    }

    @Test
    void testUpdateStudent() {
        // Given
        UserStudent student = new UserStudent();
        student.setId("student-1");
        student.setCode("ST001");
        student.setName("Original Name");
        student.setEmail("original@fpt.edu.vn");

        UserStudent updatedStudent = new UserStudent();
        updatedStudent.setId("student-1");
        updatedStudent.setCode("ST001");
        updatedStudent.setName("Updated Name");
        updatedStudent.setEmail("updated@fpt.edu.vn");

        when(userStudentRepository.save(any(UserStudent.class))).thenReturn(student).thenReturn(updatedStudent);

        // When
        UserStudent savedStudent = userStudentRepository.save(student);
        savedStudent.setName("Updated Name");
        savedStudent.setEmail("updated@fpt.edu.vn");
        UserStudent result = userStudentRepository.save(savedStudent);

        // Then
        assertEquals("Updated Name", result.getName());
        assertEquals("updated@fpt.edu.vn", result.getEmail());

        verify(userStudentRepository, times(2)).save(any(UserStudent.class));
    }

    @Test
    void testDeleteStudent() {
        // Given
        String studentId = "student-1";
        doNothing().when(userStudentRepository).deleteById(studentId);
        when(userStudentRepository.findById(studentId)).thenReturn(Optional.empty());

        // When
        userStudentRepository.deleteById(studentId);
        Optional<UserStudent> deletedStudent = userStudentRepository.findById(studentId);

        // Then
        assertFalse(deletedStudent.isPresent());

        verify(userStudentRepository).deleteById(studentId);
        verify(userStudentRepository).findById(studentId);
    }

    @Test
    void testSaveStudentWithFacility() {
        // Given
        Facility facility = new Facility();
        facility.setId("facility-1");
        facility.setCode("FAC001");
        facility.setName("FPT HCM");
        facility.setPosition(1);

        UserStudent student = new UserStudent();
        student.setId("student-1");
        student.setCode("ST001");
        student.setName("Student with Facility");
        student.setEmail("student@fpt.edu.vn");
        student.setFacility(facility);

        when(userStudentRepository.save(any(UserStudent.class))).thenReturn(student);
        when(userStudentRepository.findById("student-1")).thenReturn(Optional.of(student));

        // When
        UserStudent savedStudent = userStudentRepository.save(student);
        Optional<UserStudent> foundStudent = userStudentRepository.findById(savedStudent.getId());

        // Then
        assertTrue(foundStudent.isPresent());
        assertNotNull(foundStudent.get().getFacility());
        assertEquals("FAC001", foundStudent.get().getFacility().getCode());
        assertEquals("FPT HCM", foundStudent.get().getFacility().getName());

        verify(userStudentRepository).save(any(UserStudent.class));
        verify(userStudentRepository).findById("student-1");
    }
}