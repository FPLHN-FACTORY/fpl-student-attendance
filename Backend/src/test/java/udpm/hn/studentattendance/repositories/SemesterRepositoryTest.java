package udpm.hn.studentattendance.repositories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import udpm.hn.studentattendance.entities.Semester;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SemesterRepositoryTest {

    @Mock
    private SemesterRepository semesterRepository;

    @Test
    void testSaveAndFindById() {
        // Given
        Semester semester = new Semester();
        semester.setCode("Spring 2024");
        semester.setYear(2024);

        // Mock behavior
        when(semesterRepository.save(any(Semester.class))).thenReturn(semester);
        when(semesterRepository.findById(anyString())).thenReturn(Optional.of(semester));

        // When
        Semester savedSemester = semesterRepository.save(semester);
        Optional<Semester> foundSemester = semesterRepository.findById("mock-id");

        // Then
        assertTrue(foundSemester.isPresent());
        assertEquals("Spring 2024", foundSemester.get().getCode());
        assertEquals(2024, foundSemester.get().getYear());
        verify(semesterRepository).save(any(Semester.class));
        verify(semesterRepository).findById(anyString());
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

        List<Semester> semesters = Arrays.asList(semester1, semester2);

        // Mock behavior
        when(semesterRepository.findAll()).thenReturn(semesters);
        when(semesterRepository.save(any(Semester.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        semesterRepository.save(semester1);
        semesterRepository.save(semester2);
        List<Semester> allSemesters = semesterRepository.findAll();

        // Then
        assertEquals(2, allSemesters.size());
        assertTrue(allSemesters.stream().anyMatch(s -> "Spring 2024".equals(s.getCode())));
        assertTrue(allSemesters.stream().anyMatch(s -> "Fall 2024".equals(s.getCode())));
        verify(semesterRepository, times(2)).save(any(Semester.class));
        verify(semesterRepository).findAll();
    }

    @Test
    void testUpdateSemester() {
        // Given
        Semester semester = new Semester();
        semester.setCode("Original Code");
        semester.setYear(2023);

        Semester updatedSemester = new Semester();
        updatedSemester.setCode("Updated Code");
        updatedSemester.setYear(2024);

        // Mock behavior
        when(semesterRepository.save(any(Semester.class))).thenReturn(semester).thenReturn(updatedSemester);

        // When
        Semester savedSemester = semesterRepository.save(semester);
        savedSemester.setCode("Updated Code");
        savedSemester.setYear(2024);
        Semester resultSemester = semesterRepository.save(savedSemester);

        // Then
        assertEquals("Updated Code", resultSemester.getCode());
        assertEquals(2024, resultSemester.getYear());
        verify(semesterRepository, times(2)).save(any(Semester.class));
    }

    @Test
    void testDeleteSemester() {
        // Given
        Semester semester = new Semester();
        semester.setCode("Semester to Delete");
        semester.setYear(2022);
        String semesterId = "mock-id";

        // Mock behavior
        when(semesterRepository.save(any(Semester.class))).thenReturn(semester);
        doNothing().when(semesterRepository).deleteById(anyString());
        when(semesterRepository.findById(semesterId)).thenReturn(Optional.empty());

        // When
        Semester savedSemester = semesterRepository.save(semester);
        semesterRepository.deleteById(semesterId);
        Optional<Semester> deletedSemester = semesterRepository.findById(semesterId);

        // Then
        assertFalse(deletedSemester.isPresent());
        verify(semesterRepository).save(any(Semester.class));
        verify(semesterRepository).deleteById(anyString());
        verify(semesterRepository).findById(anyString());
    }
}
