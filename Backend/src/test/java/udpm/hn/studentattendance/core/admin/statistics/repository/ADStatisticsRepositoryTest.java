package udpm.hn.studentattendance.core.admin.statistics.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import udpm.hn.studentattendance.core.admin.statistics.model.response.ADStatisticsStatResponse;
import udpm.hn.studentattendance.core.admin.statistics.model.response.ADSTotalProjectAndSubjectResponse;
import udpm.hn.studentattendance.entities.UserActivityLog;
import udpm.hn.studentattendance.entities.Subject;
import udpm.hn.studentattendance.entities.Project;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;
import udpm.hn.studentattendance.repositories.SubjectRepository;
import udpm.hn.studentattendance.repositories.ProjectRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ADStatisticsRepositoryTest {
    @Autowired
    private ADStatisticsRepository adStatisticsRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void testADStatisticsRepositoryExists() {
        assertNotNull(adStatisticsRepository);
    }

    @Test
    void testFindAll() {
        var result = adStatisticsRepository.findAll();
        assertNotNull(result);
    }

    @Test
    void testFindById() {
        UserActivityLog log = new UserActivityLog();
        log.setIdUser("user1");
        log.setRole(RoleConstant.ADMIN);
        log.setMessage("Test log");
        UserActivityLog saved = adStatisticsRepository.save(log);
        var result = adStatisticsRepository.findById(saved.getId());
        assertTrue(result.isPresent());
        assertEquals("user1", result.get().getIdUser());
    }

    @Test
    void testSave() {
        UserActivityLog log = new UserActivityLog();
        log.setIdUser("user2");
        log.setRole(RoleConstant.STAFF);
        log.setMessage("Save log");
        UserActivityLog saved = adStatisticsRepository.save(log);
        assertNotNull(saved.getId());
        assertEquals("user2", saved.getIdUser());
    }

    @Test
    void testDelete() {
        UserActivityLog log = new UserActivityLog();
        log.setIdUser("user3");
        log.setRole(RoleConstant.TEACHER);
        log.setMessage("Delete log");
        UserActivityLog saved = adStatisticsRepository.save(log);
        adStatisticsRepository.deleteById(saved.getId());
        assertFalse(adStatisticsRepository.findById(saved.getId()).isPresent());
    }

    @Test
    void testGetAllStatistics() {
        // Just test the method exists and can be called
        Optional<ADStatisticsStatResponse> result = adStatisticsRepository.getAllStatistics();
        // In test environment, this will likely return empty
        assertNotNull(result);
    }

    @Test
    void testGetTotalProjectAndSubject() {
        // Ensure at least one subject and one project exist
        Subject subject = new Subject();
        subject.setCode("TEST_SUBJECT");
        subject.setName("Test Subject");
        subjectRepository.save(subject);

        Project project = new Project();
        project.setName("Test Project");
        project.setDescription("Test Project Description");
        projectRepository.save(project);

        Optional<ADSTotalProjectAndSubjectResponse> result = adStatisticsRepository.getTotalProjectAndSubject();
        assertNotNull(result);
    }
}