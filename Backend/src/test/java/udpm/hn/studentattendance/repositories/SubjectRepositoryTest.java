package udpm.hn.studentattendance.repositories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import udpm.hn.studentattendance.entities.Subject;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectRepositoryTest {

    @Mock
    private SubjectRepository subjectRepository;

    @Test
    void testSaveAndFindById() {
        // Given
        Subject subject = new Subject();
        subject.setCode("SUB001");
        subject.setName("Java Programming");

        // Mock behavior
        when(subjectRepository.save(any(Subject.class))).thenReturn(subject);
        when(subjectRepository.findById(anyString())).thenReturn(Optional.of(subject));

        // When
        Subject savedSubject = subjectRepository.save(subject);
        Optional<Subject> foundSubject = subjectRepository.findById("mock-id");

        // Then
        assertTrue(foundSubject.isPresent());
        assertEquals("SUB001", foundSubject.get().getCode());
        assertEquals("Java Programming", foundSubject.get().getName());
        verify(subjectRepository).save(any(Subject.class));
        verify(subjectRepository).findById(anyString());
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

        List<Subject> subjects = Arrays.asList(subject1, subject2);

        // Mock behavior
        when(subjectRepository.findAll()).thenReturn(subjects);
        when(subjectRepository.save(any(Subject.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        subjectRepository.save(subject1);
        subjectRepository.save(subject2);
        List<Subject> allSubjects = subjectRepository.findAll();

        // Then
        assertEquals(2, allSubjects.size());
        assertTrue(allSubjects.stream().anyMatch(s -> "SUB001".equals(s.getCode())));
        assertTrue(allSubjects.stream().anyMatch(s -> "SUB002".equals(s.getCode())));
        verify(subjectRepository, times(2)).save(any(Subject.class));
        verify(subjectRepository).findAll();
    }

    @Test
    void testUpdateSubject() {
        // Given
        Subject subject = new Subject();
        subject.setCode("SUB001");
        subject.setName("Original Name");

        Subject updatedSubject = new Subject();
        updatedSubject.setCode("SUB001");
        updatedSubject.setName("Updated Name");

        // Mock behavior
        when(subjectRepository.save(any(Subject.class))).thenReturn(subject).thenReturn(updatedSubject);

        // When
        Subject savedSubject = subjectRepository.save(subject);
        savedSubject.setName("Updated Name");
        Subject resultSubject = subjectRepository.save(savedSubject);

        // Then
        assertEquals("Updated Name", resultSubject.getName());
        assertEquals("SUB001", resultSubject.getCode());
        verify(subjectRepository, times(2)).save(any(Subject.class));
    }

    @Test
    void testDeleteSubject() {
        // Given
        Subject subject = new Subject();
        subject.setCode("SUB001");
        subject.setName("Subject to Delete");
        String subjectId = "mock-id";

        // Mock behavior
        when(subjectRepository.save(any(Subject.class))).thenReturn(subject);
        doNothing().when(subjectRepository).deleteById(anyString());
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        // When
        Subject savedSubject = subjectRepository.save(subject);
        subjectRepository.deleteById(subjectId);
        Optional<Subject> deletedSubject = subjectRepository.findById(subjectId);

        // Then
        assertFalse(deletedSubject.isPresent());
        verify(subjectRepository).save(any(Subject.class));
        verify(subjectRepository).deleteById(anyString());
        verify(subjectRepository).findById(anyString());
    }
}
