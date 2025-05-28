package udpm.hn.studentattendance.core.admin.facility.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFFilterFacilityIPRequest;
import udpm.hn.studentattendance.core.admin.facility.model.response.AFFacilityIPResponse;
import udpm.hn.studentattendance.repositories.FacilityIPRepository;

@Repository
public interface AFFacilityIPRepository extends FacilityIPRepository {

    @Query(value = """
                SELECT
                    ROW_NUMBER() OVER (ORDER BY fi.created_at DESC) as orderNumber,
                    fi.id,
                    fi.ip,
                    fi.status,
                    fi.type
                FROM facility_ip fi
                JOIN facility f ON fi.id_facility = f.id
                WHERE
                    f.id = :#{#request.idFacility} AND
                    (NULLIF(TRIM(:#{#request.keyword}), '') IS NULL OR fi.ip LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%')) AND
                    (:#{#request.status} IS NULL OR fi.status = :#{#request.status}) AND
                    (:#{#request.type} IS NULL OR fi.type = :#{#request.type})
                ORDER BY fi.status DESC, fi.created_at DESC
            """, countQuery = """
                SELECT
                    COUNT(DISTINCT fi.id)
                FROM facility_ip fi
                JOIN facility f ON fi.id_facility = f.id
                WHERE
                    f.id = :#{#request.idFacility} AND
                    (NULLIF(TRIM(:#{#request.keyword}), '') IS NULL OR fi.ip LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%')) AND
                    (:#{#request.status} IS NULL OR fi.status = :#{#request.status}) AND
                    (:#{#request.type} IS NULL OR fi.type = :#{#request.type})
            """, nativeQuery = true)
    Page<AFFacilityIPResponse> getAllByFilter(Pageable pageable, AFFilterFacilityIPRequest request);

    @Query(value = """
                SELECT
                    CASE WHEN COUNT(*) > 0 THEN 'TRUE' ELSE 'FALSE' END
                FROM facility_ip
                WHERE
                    status = 1 AND
                    id_facility = :idFacility AND
                    ip = :ip AND
                    type = :type AND
                    (:idFacilityIP IS NULL OR id != :idFacilityIP)
            """, nativeQuery = true)
    boolean isExistsIP(String ip, int type, String idFacility, String idFacilityIP);

}
