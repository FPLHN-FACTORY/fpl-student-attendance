package udpm.hn.studentattendance.core.staff.plan.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDLevelProjectResponse;
import udpm.hn.studentattendance.repositories.LevelProjectRepository;

import java.util.List;

@Repository
public interface SPDLevelProjectRepository extends LevelProjectRepository {

    @Query(value = """
                SELECT
                    id,
                    name,
                    code
                FROM level_project
                WHERE
                    status = 1
                ORDER BY name ASC
            """, nativeQuery = true)
    List<SPDLevelProjectResponse> getAll();

}
