package udpm.hn.studentattendance.repositories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import udpm.hn.studentattendance.entities.Project;
import udpm.hn.studentattendance.entities.LevelProject;
import udpm.hn.studentattendance.entities.SubjectFacility;
import udpm.hn.studentattendance.entities.Semester;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ExtendWith(SpringExtension.class)
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private LevelProjectRepository levelProjectRepository;

    @Autowired
    private SubjectFacilityRepository subjectFacilityRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Test
    void testSaveAndFindById() {
        // Given
        Project project = new Project();
        project.setName("Java Programming Project");
        project.setDescription("Final project for Java Programming course");

        // When
        Project savedProject = projectRepository.save(project);
        Optional<Project> foundProject = projectRepository.findById(savedProject.getId());

        // Then
        assertTrue(foundProject.isPresent());
        assertEquals("Java Programming Project", foundProject.get().getName());
        assertEquals("Final project for Java Programming course", foundProject.get().getDescription());
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

        // When
        projectRepository.save(project1);
        projectRepository.save(project2);
        List<Project> allProjects = projectRepository.findAll();

        // Then
        assertTrue(allProjects.size() >= 2);
        assertTrue(allProjects.stream().anyMatch(p -> "Project 1".equals(p.getName())));
        assertTrue(allProjects.stream().anyMatch(p -> "Project 2".equals(p.getName())));
    }

    @Test
    void testUpdateProject() {
        // Given
        Project project = new Project();
        project.setName("Original Name");
        project.setDescription("Original Description");

        Project savedProject = projectRepository.save(project);

        // When
        savedProject.setName("Updated Name");
        savedProject.setDescription("Updated Description");
        Project updatedProject = projectRepository.save(savedProject);

        // Then
        assertEquals("Updated Name", updatedProject.getName());
        assertEquals("Updated Description", updatedProject.getDescription());
    }

    @Test
    void testDeleteProject() {
        // Given
        Project project = new Project();
        project.setName("Project to Delete");
        project.setDescription("Will be deleted");

        Project savedProject = projectRepository.save(project);
        String projectId = savedProject.getId();

        // When
        projectRepository.deleteById(projectId);
        Optional<Project> deletedProject = projectRepository.findById(projectId);

        // Then
        assertFalse(deletedProject.isPresent());
    }

    @Test
    void testSaveProjectWithRelationships() {
        // Given
        LevelProject levelProject = new LevelProject();
        levelProject.setName("Advanced Level");
        levelProject.setDescription("Advanced level projects");
        LevelProject savedLevelProject = levelProjectRepository.save(levelProject);

        SubjectFacility subjectFacility = new SubjectFacility();
        SubjectFacility savedSubjectFacility = subjectFacilityRepository.save(subjectFacility);

        Semester semester = new Semester();
        semester.setCode("Spring 2024");
        semester.setYear(2024);
        Semester savedSemester = semesterRepository.save(semester);

        Project project = new Project();
        project.setName("Java Final Project");
        project.setDescription("Final project for Java course");
        project.setLevelProject(savedLevelProject);
        project.setSubjectFacility(savedSubjectFacility);
        project.setSemester(savedSemester);

        // When
        Project savedProject = projectRepository.save(project);
        Optional<Project> foundProject = projectRepository.findById(savedProject.getId());

        // Then
        assertTrue(foundProject.isPresent());
        assertNotNull(foundProject.get().getLevelProject());
        assertNotNull(foundProject.get().getSubjectFacility());
        assertNotNull(foundProject.get().getSemester());
        assertEquals("Advanced Level", foundProject.get().getLevelProject().getName());
        assertEquals("Spring 2024", foundProject.get().getSemester().getCode());
    }
}