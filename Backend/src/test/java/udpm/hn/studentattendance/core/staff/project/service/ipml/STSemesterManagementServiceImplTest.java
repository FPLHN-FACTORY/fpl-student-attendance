package udpm.hn.studentattendance.core.staff.project.service.ipml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import udpm.hn.studentattendance.core.staff.project.model.response.USSemesterResponse;
import udpm.hn.studentattendance.core.staff.project.repository.STProjectSemesterExtendRepository;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class STSemesterManagementServiceImplTest {
    @Mock
    private STProjectSemesterExtendRepository repository;
    @InjectMocks
    private STSemesterManagementServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetComboboxSemester() {
        List<USSemesterResponse> mockSemesters = Collections.emptyList();
        when(repository.getSemesters()).thenReturn(mockSemesters);
        List<USSemesterResponse> result = service.getComboboxSemester();
        assertNotNull(result);
        verify(repository, times(1)).getSemesters();
    }

    @Test
    void testGetSemester() {
        List<Semester> mockSemesters = Collections.emptyList();
        when(repository.getAllSemester(EntityStatus.ACTIVE)).thenReturn(mockSemesters);
        List<Semester> result = service.getSemester();
        assertNotNull(result);
        verify(repository, times(1)).getAllSemester(EntityStatus.ACTIVE);
    }
}