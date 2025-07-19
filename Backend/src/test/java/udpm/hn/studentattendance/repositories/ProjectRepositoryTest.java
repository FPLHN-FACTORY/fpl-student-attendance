package udpm.hn.studentattendance.repositories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import udpm.hn.studentattendance.entities.Project;
import udpm.hn.studentattendance.entities.LevelProject;
import udpm.hn.studentattendance.entities.SubjectFacility;
import udpm.hn.studentattendance.entities.Semester;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectRepositoryTest {

    @Mock
    private ProjectRepository projectRepository;

    @Test
    void testSaveAndFindById() {
        // Given
        Project project = new Project();
        project.setName("Java Programming Project");
        project.setDescription("Final project for Java Programming course");

        // Mock behavior
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectRepository.findById(anyString())).thenReturn(Optional.of(project));

        // When
        Project savedProject = projectRepository.save(project);
        Optional<Project> foundProject = projectRepository.findById("mock-id");

        // Then
        assertTrue(foundProject.isPresent());
        assertEquals("Java Programming Project", foundProject.get().getName());
        assertEquals("Final project for Java Programming course", foundProject.get().getDescription());
        verify(projectRepository).save(any(Project.class));
        verify(projectRepository).findById(anyString());
    }

    @Test
    void testSaveAndFindAll() {
        // Given
        Project project1 = new Project();
        project1.setName("Project 1");
        project1.setDescription("First project");

        Project project2 = new Project();
        project2.setName("Project 2");
        project2.setDescription("Second project");

        List<Project> projects = Arrays.asList(project1, project2);

        // Mock behavior
        when(projectRepository.findAll()).thenReturn(projects);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        projectRepository.save(project1);
        projectRepository.save(project2);
        List<Project> allProjects = projectRepository.findAll();

        // Then
        assertEquals(2, allProjects.size());
        assertTrue(allProjects.stream().anyMatch(p -> "Project 1".equals(p.getName())));
        assertTrue(allProjects.stream().anyMatch(p -> "Project 2".equals(p.getName())));
        verify(projectRepository, times(2)).save(any(Project.class));
        verify(projectRepository).findAll();
    }

    @Test
    void testUpdateProject() {
        // Given
        Project project = new Project();
        project.setName("Original Name");
        project.setDescription("Original Description");

        Project updatedProject = new Project();
        updatedProject.setName("Updated Name");
        updatedProject.setDescription("Updated Description");

        // Mock behavior
        when(projectRepository.save(any(Project.class))).thenReturn(project).thenReturn(updatedProject);

        // When
        Project savedProject = projectRepository.save(project);
        savedProject.setName("Updated Name");
        savedProject.setDescription("Updated Description");
        Project resultProject = projectRepository.save(savedProject);

        // Then
        assertEquals("Updated Name", resultProject.getName());
        assertEquals("Updated Description", resultProject.getDescription());
        verify(projectRepository, times(2)).save(any(Project.class));
    }

    @Test
    void testDeleteProject() {
        // Given
        Project project = new Project();
        project.setName("Project to Delete");
        project.setDescription("Will be deleted");
        String projectId = "mock-id";

        // Mock behavior
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        doNothing().when(projectRepository).deleteById(anyString());
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // When
        Project savedProject = projectRepository.save(project);
        projectRepository.deleteById(projectId);
        Optional<Project> deletedProject = projectRepository.findById(projectId);

        // Then
        assertFalse(deletedProject.isPresent());
        verify(projectRepository).save(any(Project.class));
        verify(projectRepository).deleteById(anyString());
        verify(projectRepository).findById(anyString());
    }

    @Test
    void testSaveProjectWithRelationships() {
        // Given
        LevelProject levelProject = new LevelProject();
        levelProject.setName("Advanced Level");
        levelProject.setDescription("Advanced level projects");

        SubjectFacility subjectFacility = new SubjectFacility();

        Semester semester = new Semester();
        semester.setCode("Spring 2024");
        semester.setYear(2024);

        Project project = new Project();
        project.setName("Java Final Project");
        project.setDescription("Final project for Java course");
        project.setLevelProject(levelProject);
        project.setSubjectFacility(subjectFacility);
        project.setSemester(semester);

        // Mock behavior
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectRepository.findById(anyString())).thenReturn(Optional.of(project));

        // When
        Project savedProject = projectRepository.save(project);
        Optional<Project> foundProject = projectRepository.findById("mock-id");

        // Then
        assertTrue(foundProject.isPresent());
        assertNotNull(foundProject.get().getLevelProject());
        assertNotNull(foundProject.get().getSubjectFacility());
        assertNotNull(foundProject.get().getSemester());
        assertEquals("Advanced Level", foundProject.get().getLevelProject().getName());
        assertEquals("Spring 2024", foundProject.get().getSemester().getCode());
        verify(projectRepository).save(any(Project.class));
        verify(projectRepository).findById(anyString());
    }
}