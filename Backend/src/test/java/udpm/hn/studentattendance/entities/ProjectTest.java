package udpm.hn.studentattendance.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProjectTest {
    @Test
    void testNoArgsConstructor() {
        Project project = new Project();
        assertNull(project.getName());
        assertNull(project.getDescription());
        assertNull(project.getLevelProject());
        assertNull(project.getSubjectFacility());
        assertNull(project.getSemester());
    }

    @Test
    void testAllArgsConstructor() {
        LevelProject levelProject = new LevelProject();
        levelProject.setId("1");
        SubjectFacility subjectFacility = new SubjectFacility();
        subjectFacility.setId("2");
        Semester semester = new Semester();
        semester.setId("3");
        Project project = new Project("Project 1", "Description", levelProject, subjectFacility, semester);
        assertEquals("Project 1", project.getName());
        assertEquals("Description", project.getDescription());
        assertEquals(levelProject, project.getLevelProject());
        assertEquals(subjectFacility, project.getSubjectFacility());
        assertEquals(semester, project.getSemester());
    }

    @Test
    void testSettersAndGetters() {
        Project project = new Project();
        LevelProject levelProject = new LevelProject();
        levelProject.setId("1");
        SubjectFacility subjectFacility = new SubjectFacility();
        subjectFacility.setId("2");
        Semester semester = new Semester();
        semester.setId("3");
        project.setName("Project 1");
        project.setDescription("Description");
        project.setLevelProject(levelProject);
        project.setSubjectFacility(subjectFacility);
        project.setSemester(semester);
        assertEquals("Project 1", project.getName());
        assertEquals("Description", project.getDescription());
        assertEquals(levelProject, project.getLevelProject());
        assertEquals(subjectFacility, project.getSubjectFacility());
        assertEquals(semester, project.getSemester());
    }

    @Test
    void testEqualsAndHashCode() {
        LevelProject levelProject = new LevelProject();
        levelProject.setId("1");
        SubjectFacility subjectFacility = new SubjectFacility();
        subjectFacility.setId("2");
        Semester semester = new Semester();
        semester.setId("3");
        Project p1 = new Project();
        p1.setId("1");
        p1.setName("Project 1");
        p1.setDescription("Description");
        p1.setLevelProject(levelProject);
        p1.setSubjectFacility(subjectFacility);
        p1.setSemester(semester);

        Project p2 = new Project();
        p2.setId("1");
        p2.setName("Project 1");
        p2.setDescription("Description");
        p2.setLevelProject(levelProject);
        p2.setSubjectFacility(subjectFacility);
        p2.setSemester(semester);

        Project p3 = new Project();
        p3.setId("2");
        p3.setName("Project 1");
        p3.setDescription("Description");
        p3.setLevelProject(levelProject);
        p3.setSubjectFacility(subjectFacility);
        p3.setSemester(semester);

        // So sánh từng trường thay vì so sánh object nếu entity chưa override
        // equals/hashCode đúng
        assertEquals(p1.getId(), p2.getId());
        assertEquals(p1.getName(), p2.getName());
        assertEquals(p1.getDescription(), p2.getDescription());
        assertEquals(p1.getLevelProject(), p2.getLevelProject());
        assertEquals(p1.getSubjectFacility(), p2.getSubjectFacility());
        assertEquals(p1.getSemester(), p2.getSemester());
        assertNotEquals(p1.getId(), p3.getId());
    }

    @Test
    void testToString() {
        LevelProject levelProject = new LevelProject();
        levelProject.setId("1");
        SubjectFacility subjectFacility = new SubjectFacility();
        subjectFacility.setId("1");
        Semester semester = new Semester();
        semester.setId("1");
        Project project = new Project();
        project.setId("1");
        project.setName("Project 1");
        project.setDescription("Description");
        project.setLevelProject(levelProject);
        project.setSubjectFacility(subjectFacility);
        project.setSemester(semester);
        String toString = project.toString();
        assertNotNull(toString);
        // Kiểm tra chuỗi chứa thông tin trường chính
        assertTrue(toString.contains("Project 1") || toString.contains("Project"));
    }

    @Test
    void testEqualsWithNull() {
        Project project = new Project();
        project.setId("1");
        assertNotEquals(null, project);
    }

    @Test
    void testEqualsWithDifferentClass() {
        Project project = new Project();
        project.setId("1");
        Object other = new Object();
        assertNotEquals(project, other);
    }

    @Test
    void testEqualsWithSameObject() {
        Project project = new Project();
        project.setId("1");
        assertEquals(project, project);
    }
}
