package udpm.hn.studentattendance.core.admin.facility.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.facility.model.request.FacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.facility.model.response.AdFacilityResponse;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.repositories.FacilityRepository;

import java.util.Optional;

@Repository
public interface AdFacilityRepository extends FacilityRepository {
    @Query(
            value = """
                    SELECT 
                        f.id AS id,
                        f.code AS facilityCode,
                        f.name AS facilityName,
                        f.status AS facilityStatus
                    FROM Facility f
                    WHERE (:#{#request.name} IS NULL OR f.code LIKE CONCAT('%', TRIM(:#{#request.q}), '%') OR f.name LIKE CONCAT('%', TRIM(:#{#request.name}), '%'))
                      AND (:#{#request.status} IS NULL OR f.status = (:#{#request.status}))
                    ORDER BY f.createdAt DESC
                    """,
            countQuery = """
                    SELECT COUNT(f.id)
                    FROM Facility f
                    WHERE (:#{#request.name} IS NULL OR f.code LIKE CONCAT('%', TRIM(:#{#request.q}), '%') OR f.name LIKE CONCAT('%', TRIM(:#{#request.name}), '%'))
                      AND (:#{#request.status} IS NULL OR f.status = (:#{#request.status}))
                    """
    )
    Page<AdFacilityResponse> getAllFacility(Pageable pageable, FacilitySearchRequest request);

    @Query(value = """
            SELECT COUNT(f) > 0
            FROM Facility f
            WHERE TRIM(f.name) = TRIM(:name) AND TRIM(f.code) = TRIM(:code)
               """)
    boolean existByName(String name, String code);

    @Query(value = """
            SELECT 
            f.id as facilityId,
            f.code as facilityCode,
            f.name as facilityName,
            f.status as facilityStatus
            FROM Facility f
            WHERE TRIM(f.id) = TRIM(:facilityId)
            """)
    Optional<AdFacilityResponse> getDetailFacilityById(String facilityId);

    boolean existsByNameAndId(String name, String id);
    Optional<Facility> findByName(String nameFacility);
}
