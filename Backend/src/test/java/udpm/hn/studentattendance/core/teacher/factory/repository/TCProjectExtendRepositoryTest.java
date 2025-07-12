package udpm.hn.studentattendance.core.teacher.factory.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import udpm.hn.studentattendance.entities.Project;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TCProjectExtendRepositoryTest {
    
    @Mock
    private TCProjectExtendRepository tcProjectExtendRepository;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testTCProjectExtendRepositoryExists() {
        assertNotNull(tcProjectExtendRepository);
    }

    @Test
    void testGetAllProjectName() {
        // Given
        String facilityId = "test-facility-id";
        List<Project> mockProjects = new ArrayList<>();
        Project mockProject = mock(Project.class);
        mockProjects.add(mockProject);
        
        // When
        when(tcProjectExtendRepository.getAllProjectName(facilityId)).thenReturn(mockProjects);
        
        // Then
        List<Project> result = tcProjectExtendRepository.getAllProjectName(facilityId);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(tcProjectExtendRepository, times(1)).getAllProjectName(facilityId);
    }
    
    @Test
    void testGetAllProjectNameWithEmptyResult() {
        // Given
        String facilityId = "test-facility-id";
        List<Project> emptyList = new ArrayList<>();
        
        // When
        when(tcProjectExtendRepository.getAllProjectName(facilityId)).thenReturn(emptyList);
        
        // Then
        List<Project> result = tcProjectExtendRepository.getAllProjectName(facilityId);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(tcProjectExtendRepository, times(1)).getAllProjectName(facilityId);
    }
}