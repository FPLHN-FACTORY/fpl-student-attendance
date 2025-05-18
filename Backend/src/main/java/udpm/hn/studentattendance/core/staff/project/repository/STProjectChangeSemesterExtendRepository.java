package udpm.hn.studentattendance.core.staff.project.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.repositories.ProjectRepository;

@Repository
public interface STProjectChangeSemesterExtendRepository extends ProjectRepository {
    @Modifying
    @Transactional
    @Query(
            value = """
                    DELETE a, pd, pf, p 
                    FROM plan p 
                    LEFT JOIN plan_factory pf ON pf.id_plan = p.id 
                    LEFT JOIN plan_date pd ON pd.id_plan_factory = pf.id 
                    LEFT JOIN attendance a ON a.id_plan_date = pd.id 
                    WHERE p.id_project = :projectId
                    """,
            nativeQuery = true
    )
    void deleteAllByProjectId(String projectId);
}
