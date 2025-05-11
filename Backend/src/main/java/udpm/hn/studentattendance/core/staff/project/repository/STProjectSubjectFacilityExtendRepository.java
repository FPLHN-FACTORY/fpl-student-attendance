package udpm.hn.studentattendance.core.staff.project.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.staff.project.model.response.USSubjectResponse;
import udpm.hn.studentattendance.entities.Subject;
import udpm.hn.studentattendance.entities.SubjectFacility;
import udpm.hn.studentattendance.repositories.ProjectRepository;
import udpm.hn.studentattendance.repositories.SubjectFacilityRepository;

import java.util.List;

@Repository
public interface STProjectSubjectFacilityExtendRepository extends SubjectFacilityRepository {

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
                AND s.status = 1
                ORDER BY sf.created_at DESC
            """, nativeQuery = true)
    List<USSubjectResponse> getSubjectFacility(@Param("facilityId") String facilityId);

    @Query(value = """
            SELECT 
            sf
            FROM SubjectFacility sf 
            LEFT JOIN Subject sb ON sf.subject.id = sb.id
            LEFT JOIN Facility f ON f.id = sf.facility.id
            WHERE 
            f.id = :facilityId
            AND sf.id = :subjectFacilityId
            """)
    SubjectFacility getSubjectFacilityByName(String facilityId, String subjectFacilityId);
}
