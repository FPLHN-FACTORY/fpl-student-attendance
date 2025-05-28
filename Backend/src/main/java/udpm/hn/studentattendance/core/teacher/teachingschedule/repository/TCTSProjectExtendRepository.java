package udpm.hn.studentattendance.core.teacher.teachingschedule.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.Project;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.ProjectRepository;

import java.util.List;

@Repository
public interface TCTSProjectExtendRepository extends ProjectRepository {
    @Query(value = """
            SELECT
            p
            FROM Project p
            LEFT JOIN Factory f ON f.project.id = p.id
            LEFT JOIN UserStaff us ON us.id = f.userStaff.id
            WHERE us.id = :userId
            AND p.status = :status
            """)
    List<Project> getAllProject(String userId, EntityStatus status);
}
