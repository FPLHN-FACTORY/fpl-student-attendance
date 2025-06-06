package udpm.hn.studentattendance.core.admin.subjectfacility.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.ADSubjectFacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.response.ADFacilityResponse;
import udpm.hn.studentattendance.repositories.FacilityRepository;
import udpm.hn.studentattendance.repositories.ProjectRepository;

import java.util.List;

@Repository
public interface ADFacilityRepository extends FacilityRepository {

    @Query(value = """
                SELECT
                    id AS id,
                    name AS name
                FROM facility
                WHERE status = 1
                ORDER BY created_at DESC
            """, nativeQuery = true)
    List<ADFacilityResponse> getFacility();

    @Query(value = """
            SELECT f.id, f.name
            FROM facility f
            WHERE
                f.status = 1
            """, nativeQuery = true)
    List<ADFacilityResponse> getListFacility(@Param("request") ADSubjectFacilitySearchRequest request);

}
