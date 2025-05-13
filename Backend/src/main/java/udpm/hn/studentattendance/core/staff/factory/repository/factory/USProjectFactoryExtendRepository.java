package udpm.hn.studentattendance.core.staff.factory.repository.factory;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.staff.factory.model.response.USProjectFactoryResponse;
import udpm.hn.studentattendance.entities.Project;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.ProjectRepository;

import java.util.List;

@Repository
public interface USProjectFactoryExtendRepository extends ProjectRepository {
        @Query(value = """
                            SELECT
                            p.id,
                            p.name as projectName,
                            lp.name as levelProjectName,
                            s.code as semesterCode
                            FROM
                            project p
                            LEFT JOIN
                            subject_facility sf on p.id_subject_facility = sf.id
                            LEFT JOIN
                            facility f on f.id = sf.id_facility
                            LEFT JOIN
                            level_project lp on lp.id = p.id_level_project
                            LEFT JOIN 
                            semester s ON s.id = p.id_semester
                            WHERE
                            p.status = 1
                            AND sf.status = 1
                            AND f.status = 1
                            AND lp.status = 1
                            AND f.id = :facilityId
                        """, nativeQuery = true)
        List<USProjectFactoryResponse> getAllProject(String facilityId);
}
