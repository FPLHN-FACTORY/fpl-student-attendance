package udpm.hn.studentattendance.core.admin.subjectfacility.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.request.ADSubjectFacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.subjectfacility.model.response.ADSubjectFacilityResponse;
import udpm.hn.studentattendance.repositories.SubjectFacilityRepository;

import java.util.Optional;

@Repository
public interface ADSubjectFacilityRepository extends SubjectFacilityRepository {

    @Query(value = """
                    SELECT
                        ROW_NUMBER() OVER (ORDER BY sf.created_at DESC) AS orderNumber,
                        sf.id AS id,
                        s.id AS subjectId,
                        s.name AS subjectName,
                        f.id AS facilityId,
                        f.name AS facilityName,
                        sf.status AS status,
                        sf.created_at,
                        sf.updated_at
                    FROM subject_facility sf
                    LEFT JOIN subject s ON sf.id_subject = s.id
                    LEFT JOIN facility f ON sf.id_facility = f.id
                    WHERE
                        f.status = 1 AND
                    (
                        (:#{#request.name} IS NULL OR TRIM(s.name) LIKE CONCAT('%', TRIM(:#{#request.name}), '%'))
                        AND s.id = :#{#request.subjectId}
                    )
                    AND (:#{#request.facilityId} IS NULL OR sf.id_facility = :#{#request.facilityId})
                    AND (:#{#request.status} IS NULL OR sf.status = :#{#request.status})
                    ORDER BY sf.status DESC, sf.created_at DESC
            """, countQuery = """
                    SELECT COUNT(*)
                    FROM subject_facility sf
                    LEFT JOIN subject s ON sf.id_subject = s.id
                    LEFT JOIN facility f ON sf.id_facility = f.id
                    WHERE
                        f.status = 1 AND
                    (
                        (:#{#request.name} IS NULL OR TRIM(s.name) LIKE CONCAT('%', TRIM(:#{#request.name}), '%'))
                        AND s.id = :#{#request.subjectId}
                    )
                    AND (:#{#request.facilityId} IS NULL OR sf.id_facility = :#{#request.facilityId})
                    AND (:#{#request.status} IS NULL OR sf.status = :#{#request.status})
            """, nativeQuery = true)
    Page<ADSubjectFacilityResponse> getAll(Pageable pageable, @Param("request") ADSubjectFacilitySearchRequest request);

    @Query(value = """
                    SELECT
                        1 AS orderNumber,
                        sf.id AS id,
                        s.id AS subjectId,
                        s.name AS subjectName,
                        f.id AS facilityId,
                        f.name AS facilityName,
                        sf.status AS status,
                        sf.created_at,
                        sf.updated_at
                    FROM subject_facility sf
                    LEFT JOIN subject s ON sf.id_subject = s.id
                    LEFT JOIN facility f ON sf.id_facility = f.id
                    WHERE
                        sf.id = :id
            """, nativeQuery = true)
    Optional<ADSubjectFacilityResponse> getOneById(String id);

    @Query(value = """
                SELECT
                    CASE WHEN COUNT(*) > 0 THEN 'TRUE' ELSE 'FALSE' END
                FROM subject_facility
                WHERE
                    id_facility = :idFacility AND
                    id_subject = :idSubject AND
                    (:idSubjectFacility IS NULL OR id != :idSubjectFacility)
            """, nativeQuery = true)
    boolean isExistsSubjectFacility(String idFacility, String idSubject, String idSubjectFacility);

}
