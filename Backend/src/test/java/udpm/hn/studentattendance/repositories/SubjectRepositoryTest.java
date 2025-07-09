package udpm.hn.studentattendance.repositories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import udpm.hn.studentattendance.entities.Subject;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ExtendWith(SpringExtension.class)
class SubjectRepositoryTest {

    @Autowired
    private SubjectRepository subjectRepository;

    @Test
    void testSaveAndFindById() {
        // Given
        Subject subject = new Subject();
        subject.setCode("SUB001");
        subject.setName("Java Programming");

        // When
        Subject savedSubject = subjectRepository.save(subject);
        Optional<Subject> foundSubject = subjectRepository.findById(savedSubject.getId());

        // Then
        assertTrue(foundSubject.isPresent());
        assertEquals("SUB001", foundSubject.get().getCode());
        assertEquals("Java Programming", foundSubject.get().getName());
    }

    @Test
    void testSaveAndFindAll() {
        // Given
        Subject subject1 = new Subject();
        subject1.setCode("SUB001");
        subject1.setName("Java Programming");

        Subject subject2 = new Subject();
        subject2.setCode("SUB002");
        subject2.setName("Database Management");

        // When
        subjectRepository.save(subject1);
        subjectRepository.save(subject2);
        List<Subject> allSubjects = subjectRepository.findAll();

        // Then
        assertTrue(allSubjects.size() >= 2);
        assertTrue(allSubjects.stream().anyMatch(s -> "SUB001".equals(s.getCode())));
        assertTrue(allSubjects.stream().anyMatch(s -> "SUB002".equals(s.getCode())));
    }

    @Test
    void testUpdateSubject() {
        // Given
        Subject subject = new Subject();
        subject.setCode("SUB001");
        subject.setName("Original Name");

        Subject savedSubject = subjectRepository.save(subject);

        // When
        savedSubject.setName("Updated Name");
        Subject updatedSubject = subjectRepository.save(savedSubject);

        // Then
        assertEquals("Updated Name", updatedSubject.getName());
        assertEquals("SUB001", updatedSubject.getCode());
    }

    @Test
    void testDeleteSubject() {
        // Given
        Subject subject = new Subject();
        subject.setCode("SUB001");
        subject.setName("Subject to Delete");

        Subject savedSubject = subjectRepository.save(subject);
        String subjectId = savedSubject.getId();

        // When
        subjectRepository.deleteById(subjectId);
        Optional<Subject> deletedSubject = subjectRepository.findById(subjectId);

        // Then
        assertFalse(deletedSubject.isPresent());
    }
}