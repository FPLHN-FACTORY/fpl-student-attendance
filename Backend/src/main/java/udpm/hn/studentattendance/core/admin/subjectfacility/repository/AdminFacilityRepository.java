package udpm.hn.studentattendance.core.admin.subjectfacility.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.AdminSubjectFacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.response.AdminFacilityResponse;
import udpm.hn.studentattendance.repositories.ProjectRepository;

import java.util.List;

@Repository
public interface AdminFacilityRepository extends ProjectRepository {

    @Query(value = """
                SELECT 
                    ROW_NUMBER() OVER (ORDER BY created_at DESC) AS indexs,
                    id AS id,
                    name AS name
                FROM facility
                WHERE  status = 1
                ORDER BY created_at DESC
            """, nativeQuery = true)
    List<AdminFacilityResponse> getFacility();

    @Query(value = """
            SELECT f.id, f.name
            FROM facility f
            LEFT JOIN subject_facility sf
              ON f.id = sf.id_facility
              AND sf.id_subject = :#{#request.subjectId}
            WHERE sf.id_facility IS NULL;
            """, nativeQuery = true)
    List<AdminFacilityResponse> getListFacility(@Param("request") AdminSubjectFacilitySearchRequest request);
}
