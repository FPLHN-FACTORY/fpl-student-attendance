package udpm.hn.studentattendance.core.staff.project.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.staff.project.model.response.USSemesterResponse;
import udpm.hn.studentattendance.repositories.ProjectRepository;

import java.util.List;

@Repository
public interface Staff_SemesterManagementRepository extends ProjectRepository {

    @Query(value = """
                SELECT
                    ROW_NUMBER() OVER (ORDER BY s.created_at DESC) AS indexs,
                    s.id AS id,
                    s.name AS name,
                    s.code AS code
                FROM semester s
                WHERE s.status = 1
                ORDER BY s.created_at DESC
            """, nativeQuery = true)
    List<USSemesterResponse> getSemesters();
}
