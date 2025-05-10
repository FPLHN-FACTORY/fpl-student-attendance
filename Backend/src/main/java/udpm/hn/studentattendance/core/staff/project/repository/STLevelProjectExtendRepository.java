package udpm.hn.studentattendance.core.staff.project.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.staff.project.model.response.USLevelProjectResponse;
import udpm.hn.studentattendance.entities.LevelProject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.LevelProjectRepository;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

@Repository
public interface STLevelProjectExtendRepository extends LevelProjectRepository {

    @Query(value = """
                SELECT
                    ROW_NUMBER() OVER (ORDER BY created_at DESC) AS indexs,
                    id AS id,
                    name AS name,
                    code AS code
                FROM level_project
                where status = 1
                ORDER BY created_at DESC
            """, nativeQuery = true)
    List<USLevelProjectResponse> getLevelProject();
    @Query(value = """
        SELECT 
        lp
        FROM LevelProject lp
        WHERE 
        lp.status = :status
        AND 
        lp.code = :code
        """)
    LevelProject getAllLevelProjectByCode(EntityStatus status, String code);
}
