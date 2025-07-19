package udpm.hn.studentattendance.core.teacher.factory.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.Project;
import udpm.hn.studentattendance.repositories.ProjectRepository;

import java.util.List;

@Repository
public interface TCProjectExtendRepository extends ProjectRepository {
    @Query(value = """
            SELECT
                 p.*
                 FROM
                 project p
                 JOIN subject_facility sf ON sf.id = p.id_subject_facility
                 JOIN facility f ON f.id = sf.id_facility
                 WHERE
                 p.status = 1
                 AND
                 f.status = 1
                 AND
                 sf.status = 1
                 AND
                 f.id = :facilityId
            """, nativeQuery = true)
    List<Project> getAllProjectName(String facilityId);
}
