package udpm.hn.studentattendance.core.admin.project.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.project.model.response.SubjectResponse;
import udpm.hn.studentattendance.repositories.ProjectRepository;

import java.util.List;

@Repository
public interface AdminSubjectManagementRepository extends ProjectRepository {

    @Query(value = """
                SELECT
                    ROW_NUMBER() OVER (ORDER BY sf.created_at DESC) AS indexs,
                    sf.id AS id,
                    s.name AS name
                FROM subject_facility sf
                LEFT JOIN subject s ON sf.id_subject = s.id
                WHERE (
                    :facilityId IS NULL OR sf.id_facility = :facilityId
                ) AND sf.status = 1
                ORDER BY sf.created_at DESC
            """, nativeQuery = true)
    List<SubjectResponse> getSubjectFacility(@Param("facilityId") String facilityId);

    @Query(value = """
                SELECT
                    ROW_NUMBER() OVER (ORDER BY created_at DESC) AS indexs,
                    id AS id,
                    name AS name
                FROM subject
                WHERE status = 1
            """, nativeQuery = true)
    List<SubjectResponse> getSubjectFacilityAdd();

    @Query(value = """
                SELECT
                    sf.id
                FROM subject_facility sf
                JOIN subject s ON sf.id_subject = s.id
                join facility f ON sf.id_facility = f.id
                where s.code = :codeSubject and f.code = :codeFacility
            """, nativeQuery = true)
    String getSubjectFacilityImport(@Param("codeSubject") String code1, @Param("codeFacility") String code2);
}
