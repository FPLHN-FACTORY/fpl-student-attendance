package udpm.hn.studentattendance.core.admin.project.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.project.model.response.LevelProjectResponse;
import udpm.hn.studentattendance.entities.LevelProject;
import udpm.hn.studentattendance.repositories.LevelProjectRepository;

import java.util.List;

@Repository
public interface AdminLevelProjectManagementRepository extends LevelProjectRepository {

    @Query(value = """
                SELECT
                    ROW_NUMBER() OVER (ORDER BY created_at DESC) AS indexs,
                    id AS id,
                    name AS name
                FROM level_project
                where status = 1
                ORDER BY created_at DESC
            """, nativeQuery = true)
    List<LevelProjectResponse> getLevelProject();

    @Query(value = """
                SELECT
                    *
                FROM level_project
                where code = :code
            """, nativeQuery = true)
    LevelProject getLevelProject(@Param("code") String code);

}
