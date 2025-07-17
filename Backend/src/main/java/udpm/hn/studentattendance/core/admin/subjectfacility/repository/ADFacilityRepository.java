package udpm.hn.studentattendance.core.admin.subjectfacility.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.ADSubjectFacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.response.ADFacilityResponse;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.FacilityRepository;
import udpm.hn.studentattendance.repositories.ProjectRepository;

import javax.swing.text.html.parser.Entity;
import java.util.List;

@Repository
public interface ADFacilityRepository extends FacilityRepository {

    @Query(value = """
                SELECT
                        f.id AS id,
                        f.name AS name
                    FROM facility f
                    WHERE
                        f.status = 1
                        AND NOT EXISTS (
                            SELECT 1
                            FROM subject_facility sf
                            JOIN subject sb ON sf.id_subject = sb.id
                            WHERE sf.id_facility = f.id
                            AND sf.id_subject = :idSubject
                            AND sf.status = 1
                            AND sb.status = 1
                        )
                    ORDER BY f.created_at DESC
            """, nativeQuery = true)
    List<ADFacilityResponse> getFacility(String idSubject);

    @Query(value = """
            SELECT f 
            FROM Facility f
            WHERE f.status = :status
            """
    )
    List<Facility> getFacilities(EntityStatus status);

}
