package udpm.hn.studentattendance.core.admin.project.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.project.model.response.FacilityResponse;
import udpm.hn.studentattendance.core.admin.project.model.response.SubjectResponse;
import udpm.hn.studentattendance.repositories.ProjectRepository;

import java.util.List;

@Repository
public interface AdminFacilityBySubjectRepository extends ProjectRepository {

    @Query(value = """
                SELECT
                    ROW_NUMBER() OVER (ORDER BY sf.created_at DESC) AS indexs,
                    f.id AS id,
                    f.name AS name
                FROM facility f
                JOIN subject_facility sf ON f.id = sf.id_facility
                WHERE (
                    :subjectId IS NULL OR sf.id_subject= :subjectId
                ) AND f.status = 1
            """, nativeQuery = true)
    List<FacilityResponse> getFacility(@Param("subjectId") String subjectId);

}
