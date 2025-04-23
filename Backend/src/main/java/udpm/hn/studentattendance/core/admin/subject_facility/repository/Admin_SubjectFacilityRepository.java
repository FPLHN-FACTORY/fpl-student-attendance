package udpm.hn.studentattendance.core.admin.subject_facility.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.subject_facility.model.request.Admin_SubjectFacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.subject_facility.model.response.Admin_SubjectFacilityResponse;
import udpm.hn.studentattendance.repositories.SubjectFacilityRepository;

@Repository
public interface Admin_SubjectFacilityRepository extends SubjectFacilityRepository {

    @Query(value = """
                    SELECT 
                        ROW_NUMBER() OVER (ORDER BY sf.created_at DESC) AS indexs,
                        sf.id AS id,
                        s.name AS subjectName,
                        f.name AS facilityName,
                        sf.status AS status
                    FROM subject_facility sf
                    LEFT JOIN subject s ON sf.id_subject = s.id
                    LEFT JOIN facility f ON sf.id_facility = f.id
                    WHERE (
                        (:#{#request.name} IS NULL OR s.name LIKE CONCAT('%', :#{#request.name}, '%'))
                        AND s.id = :#{#request.subjectId}
                    )
                    AND (:#{#request.facilityId} IS NULL OR sf.id_facility = :#{#request.facilityId})
                    AND (:#{#request.status} IS NULL OR sf.status = :#{#request.status})
                    ORDER BY sf.created_at DESC
            """, countQuery = """
                    SELECT COUNT(*)
                    FROM subject_facility sf
                    LEFT JOIN subject s ON sf.id_subject = s.id
                    LEFT JOIN facility f ON sf.id_facility = f.id
                    WHERE (
                        (:#{#request.name} IS NULL OR s.name LIKE CONCAT('%', :#{#request.name}, '%'))
                        AND s.id = :#{#request.subjectId}
                    )
                    AND (:#{#request.facilityId} IS NULL OR sf.id_facility = :#{#request.facilityId})
                    AND (:#{#request.status} IS NULL OR sf.status = :#{#request.status})
            """, nativeQuery = true)
    Page<Admin_SubjectFacilityResponse> getAll(Pageable pageable, @Param("request") Admin_SubjectFacilitySearchRequest request);


}
