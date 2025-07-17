package udpm.hn.studentattendance.core.teacher.statistics.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.teacher.statistics.model.response.TSChartSubjectFacilityResponse;
import udpm.hn.studentattendance.repositories.SubjectFacilityRepository;

import java.util.List;

@Repository
public interface TSSubjectFacilityRepository extends SubjectFacilityRepository {

    @Query(value = """
        SELECT
            s.name AS label,
            COUNT(DISTINCT CASE WHEN f.id_user_staff = :idUserStaff THEN p.id END) AS totalProject,
            COUNT(DISTINCT CASE WHEN f.id_user_staff = :idUserStaff THEN us.id END) AS totalStudent
        FROM subject_facility sf
        JOIN subject s ON sf.id_subject = s.id
        LEFT JOIN project p ON sf.id = p.id_subject_facility AND p.status = 1
        LEFT JOIN factory f ON p.id = f.id_project AND f.status = 1
        LEFT JOIN user_student_factory usf ON f.id = usf.id_factory AND usf.status = 1
        LEFT JOIN user_student us ON usf.id_user_student = us.id AND us.status = 1
        WHERE
            sf.id_facility = :idFacility AND
            EXISTS(
                SELECT 1
                FROM semester ss
                WHERE
                     ss.id = p.id_semester AND
                     ss.status = 1 AND
                     ss.id = :idSemester
            )
        GROUP BY sf.id, s.name
        ORDER BY s.name ASC
    """, nativeQuery = true)
    List<TSChartSubjectFacilityResponse> getStats(String idSemester, String idFacility, String idUserStaff);

}
