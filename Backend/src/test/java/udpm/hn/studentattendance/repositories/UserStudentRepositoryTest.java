package udpm.hn.studentattendance.repositories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.infrastructure.config.TestDatabaseConfig;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.context.annotation.Import;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@Import(TestDatabaseConfig.class)
class UserStudentRepositoryTest {

    @Autowired
    private UserStudentRepository userStudentRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    @Test
    void testSaveAndFindById() {
        // Given
        UserStudent student = new UserStudent();
        student.setCode("ST001");
        student.setName("Nguyen Van A");
        student.setEmail("student@fpt.edu.vn");
        student.setImage("image.jpg");
        student.setFaceEmbedding("face_embedding_data");

        // When
        UserStudent savedStudent = userStudentRepository.save(student);
        Optional<UserStudent> foundStudent = userStudentRepository.findById(savedStudent.getId());

        // Then
        assertTrue(foundStudent.isPresent());
        assertEquals("ST001", foundStudent.get().getCode());
        assertEquals("Nguyen Van A", foundStudent.get().getName());
        assertEquals("student@fpt.edu.vn", foundStudent.get().getEmail());
        assertEquals("image.jpg", foundStudent.get().getImage());
        assertEquals("face_embedding_data", foundStudent.get().getFaceEmbedding());
    }

    @Test
    void testSaveAndFindAll() {
        // Given
        UserStudent student1 = new UserStudent();
        student1.setCode("ST001");
        student1.setName("Student 1");
        student1.setEmail("student1@fpt.edu.vn");

        UserStudent student2 = new UserStudent();
        student2.setCode("ST002");
        student2.setName("Student 2");
        student2.setEmail("student2@fpt.edu.vn");

        // When
        userStudentRepository.save(student1);
        userStudentRepository.save(student2);
        List<UserStudent> allStudents = userStudentRepository.findAll();

        // Then
        assertTrue(allStudents.size() >= 2);
        assertTrue(allStudents.stream().anyMatch(s -> "ST001".equals(s.getCode())));
        assertTrue(allStudents.stream().anyMatch(s -> "ST002".equals(s.getCode())));
    }

    @Test
    void testUpdateStudent() {
        // Given
        UserStudent student = new UserStudent();
        student.setCode("ST001");
        student.setName("Original Name");
        student.setEmail("original@fpt.edu.vn");

        UserStudent savedStudent = userStudentRepository.save(student);

        // When
        savedStudent.setName("Updated Name");
        savedStudent.setEmail("updated@fpt.edu.vn");
        UserStudent updatedStudent = userStudentRepository.save(savedStudent);

        // Then
        assertEquals("Updated Name", updatedStudent.getName());
        assertEquals("updated@fpt.edu.vn", updatedStudent.getEmail());
    }

    @Test
    void testDeleteStudent() {
        // Given
        UserStudent student = new UserStudent();
        student.setCode("ST001");
        student.setName("Student to Delete");
        student.setEmail("delete@fpt.edu.vn");

        UserStudent savedStudent = userStudentRepository.save(student);
        String studentId = savedStudent.getId();

        // When
        userStudentRepository.deleteById(studentId);
        Optional<UserStudent> deletedStudent = userStudentRepository.findById(studentId);

        // Then
        assertFalse(deletedStudent.isPresent());
    }

    @Test
    void testSaveStudentWithFacility() {
        // Given
        Facility facility = new Facility();
        facility.setCode("FAC001");
        facility.setName("FPT HCM");
        facility.setPosition(1);
        Facility savedFacility = facilityRepository.save(facility);

        UserStudent student = new UserStudent();
        student.setCode("ST001");
        student.setName("Student with Facility");
        student.setEmail("student@fpt.edu.vn");
        student.setFacility(savedFacility);

        // When
        UserStudent savedStudent = userStudentRepository.save(student);
        Optional<UserStudent> foundStudent = userStudentRepository.findById(savedStudent.getId());

        // Then
        assertTrue(foundStudent.isPresent());
        assertNotNull(foundStudent.get().getFacility());
        assertEquals("FAC001", foundStudent.get().getFacility().getCode());
        assertEquals("FPT HCM", foundStudent.get().getFacility().getName());
    }
}