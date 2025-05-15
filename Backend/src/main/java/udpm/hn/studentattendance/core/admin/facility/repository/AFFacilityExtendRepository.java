package udpm.hn.studentattendance.core.admin.facility.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFFacilitySearchRequest;
import udpm.hn.studentattendance.core.admin.facility.model.response.AFFacilityResponse;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.repositories.FacilityRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AFFacilityExtendRepository extends FacilityRepository {
    @Query(value = """
            SELECT
                f.position AS facilityIndex,
                f.id AS id,
                f.code AS facilityCode,
                f.name AS facilityName,
                f.status AS facilityStatus,
                (SELECT
                    COALESCE(MAX(position), 0)
                FROM Facility ) AS maxPosition
            FROM Facility f
            WHERE (:#{#request.name} IS NULL
                   OR f.code LIKE CONCAT('%', TRIM(:#{#request.q}), '%')
                   OR f.name LIKE CONCAT('%', TRIM(:#{#request.name}), '%'))
              AND (:#{#request.status} IS NULL OR f.status = (:#{#request.status}))
            ORDER BY f.position ASC
            """, countQuery = """
            SELECT COUNT(f.id)
            FROM Facility f
            WHERE (:#{#request.name} IS NULL
                   OR f.code LIKE CONCAT('%', TRIM(:#{#request.q}), '%')
                   OR f.name LIKE CONCAT('%', TRIM(:#{#request.name}), '%'))
              AND (:#{#request.status} IS NULL OR f.status = (:#{#request.status}))
            """)
    Page<AFFacilityResponse> getAllFacility(Pageable pageable, AFFacilitySearchRequest request);

    @Query(value = """
            SELECT COUNT(f) > 0
            FROM Facility f
            WHERE TRIM(f.name) = TRIM(:name) AND TRIM(f.code) = TRIM(:code)
               """)
    boolean existByName(String name, String code);

    @Query(value = """
            SELECT
            f.id as id,
            f.code as facilityCode,
            f.name as facilityName,
            f.status as facilityStatus
            FROM Facility f
            WHERE TRIM(f.id) = TRIM(:facilityId)
            """)
    Optional<AFFacilityResponse> getDetailFacilityById(String facilityId);

    @Query(value = """
            SELECT
            COALESCE(MAX(position), 0)
            FROM facility
            """, nativeQuery = true)
    Integer getLastPosition();

    @Transactional
    @Modifying
    @Query(value = """
                UPDATE facility
                SET position = position + 1
                WHERE
                    position = :position AND
                    id != :idFacility
            """, nativeQuery = true)
    Integer updatePositionPreUp(int position, String idFacility);

    @Transactional
    @Modifying
    @Query(value = """
                UPDATE facility
                SET position = position - 1
                WHERE
                    position = :position AND
                    id != :idFacility
            """, nativeQuery = true)
    Integer updatePositionNextDown(int position, String idFacility);

    Optional<Facility> findByName(String nameFacility);

    @Query(value = """
        SELECT
            DISTINCT COALESCE(us.email, ust.email_fe) AS email
        From facility f
        LEFT JOIN role r ON r.id_facility = f.id
        LEFT JOIN user_staff ust ON ust.id = r.id_user_staff
        LEFT JOIN user_student us ON us.id_facility = f.id
        WHERE
            f.id = :idFacility AND
            ust.status = 1 AND
            us.status = 1 AND
            ust.email_fe IS NOT NULL AND
            us.email IS NOT NULL
    """, nativeQuery = true)
    List<String> getListEmailUserDisableFacility(String idFacility);

    @Query(value = """
                SELECT
                    CASE WHEN COUNT(*) > 0 THEN 'TRUE' ELSE 'FALSE' END
                FROM facility
                WHERE
                    status = 1 AND
                    name LIKE :name AND
                    id != :idFacility
            """, nativeQuery = true)
    boolean isExistsByName(String name, String idFacility);

}
