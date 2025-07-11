package udpm.hn.studentattendance.repositories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.infrastructure.config.TestDatabaseConfig;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.context.annotation.Import;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@Import(TestDatabaseConfig.class)
class SemesterRepositoryTest {

    @Autowired
    private SemesterRepository semesterRepository;

    @Test
    void testSaveAndFindById() {
        // Given
        Semester semester = new Semester();
        semester.setCode("Spring 2024");
        semester.setYear(2024);

        // When
        Semester savedSemester = semesterRepository.save(semester);
        Optional<Semester> foundSemester = semesterRepository.findById(savedSemester.getId());

        // Then
        assertTrue(foundSemester.isPresent());
        assertEquals("Spring 2024", foundSemester.get().getCode());
        assertEquals(2024, foundSemester.get().getYear());
    }

    @Test
    void testSaveAndFindAll() {
        // Given
        Semester semester1 = new Semester();
        semester1.setCode("Spring 2024");
        semester1.setYear(2024);

        Semester semester2 = new Semester();
        semester2.setCode("Fall 2024");
        semester2.setYear(2024);

        // When
        semesterRepository.save(semester1);
        semesterRepository.save(semester2);
        List<Semester> allSemesters = semesterRepository.findAll();

        // Then
        assertTrue(allSemesters.size() >= 2);
        assertTrue(allSemesters.stream().anyMatch(s -> "Spring 2024".equals(s.getCode())));
        assertTrue(allSemesters.stream().anyMatch(s -> "Fall 2024".equals(s.getCode())));
    }

    @Test
    void testUpdateSemester() {
        // Given
        Semester semester = new Semester();
        semester.setCode("Original Code");
        semester.setYear(2023);

        Semester savedSemester = semesterRepository.save(semester);

        // When
        savedSemester.setCode("Updated Code");
        savedSemester.setYear(2024);
        Semester updatedSemester = semesterRepository.save(savedSemester);

        // Then
        assertEquals("Updated Code", updatedSemester.getCode());
        assertEquals(2024, updatedSemester.getYear());
    }

    @Test
    void testDeleteSemester() {
        // Given
        Semester semester = new Semester();
        semester.setCode("Semester to Delete");
        semester.setYear(2022);

        Semester savedSemester = semesterRepository.save(semester);
        String semesterId = savedSemester.getId();

        // When
        semesterRepository.deleteById(semesterId);
        Optional<Semester> deletedSemester = semesterRepository.findById(semesterId);

        // Then
        assertFalse(deletedSemester.isPresent());
    }
}