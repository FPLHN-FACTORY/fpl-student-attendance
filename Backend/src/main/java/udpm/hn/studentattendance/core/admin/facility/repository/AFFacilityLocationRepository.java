package udpm.hn.studentattendance.core.admin.facility.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFFilterFacilityLocationRequest;
import udpm.hn.studentattendance.core.admin.facility.model.response.AFFacilityLocationResponse;
import udpm.hn.studentattendance.repositories.FacilityLocationRepository;

@Repository
public interface AFFacilityLocationRepository extends FacilityLocationRepository {

    @Query(value = """
        SELECT 
            ROW_NUMBER() OVER (ORDER BY fl.created_at DESC) as orderNumber,
            fl.id,
            fl.name,
            fl.status,
            fl.latitude,
            fl.longitude,
            fl.radius
        FROM facility_location fl
        JOIN facility f ON fl.id_facility = f.id
        WHERE 
            f.id = :#{#request.idFacility} AND
            (NULLIF(TRIM(:#{#request.keyword}), '') IS NULL OR BINARY fl.name LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%')) AND
            (:#{#request.status} IS NULL OR fl.status = :#{#request.status})
        ORDER BY fl.status DESC, fl.created_at DESC
    """, countQuery = """
        SELECT 
            COUNT(DISTINCT fl.id)
        FROM facility_location fl
        JOIN facility f ON fl.id_facility = f.id
        WHERE 
            f.id = :#{#request.idFacility} AND
            (NULLIF(TRIM(:#{#request.keyword}), '') IS NULL OR BINARY fl.name LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%')) AND
            (:#{#request.status} IS NULL OR fl.status = :#{#request.status})
    """, nativeQuery = true)
    Page<AFFacilityLocationResponse> getAllByFilter(Pageable pageable, AFFilterFacilityLocationRequest request);

    @Query(value = """
        SELECT
            CASE WHEN COUNT(*) > 0 THEN 'TRUE' ELSE 'FALSE' END 
        FROM facility_location
        WHERE 
            status = 1 AND
            name = :name AND
            id_facility = :idFacility AND
            (:idFacilityLocation IS NULL OR id != :idFacilityLocation)
    """, nativeQuery = true)
    boolean isExistsLocation(String name, String idFacility, String idFacilityLocation);

}
