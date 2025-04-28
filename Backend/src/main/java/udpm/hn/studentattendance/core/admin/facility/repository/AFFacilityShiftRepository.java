package udpm.hn.studentattendance.core.admin.facility.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.facility.model.request.AFFilterFacilityShiftRequest;
import udpm.hn.studentattendance.core.admin.facility.model.response.AFFacilityShiftResponse;
import udpm.hn.studentattendance.repositories.FacilityShiftRepository;

@Repository
public interface AFFacilityShiftRepository extends FacilityShiftRepository {

    @Query(value = """
                SELECT
                    ROW_NUMBER() OVER (ORDER BY fs.shift ASC) as orderNumber,
                    fs.id,
                    fs.shift,
                    fs.status,
                    fs.from_hour,
                    fs.from_minute,
                    fs.to_hour,
                    fs.to_minute,
                    CONCAT(fs.from_hour, ':', fs.from_minute) AS startTime,
                    CONCAT(fs.to_hour, ':', fs.to_minute) AS endTime
                FROM facility_shift fs
                JOIN facility f ON fs.id_facility = f.id
                WHERE
                    f.id = :#{#request.idFacility} AND
                    (:#{#request.shift} IS NULL OR fs.shift = :#{#request.shift}) AND
                    (:#{#request.status} IS NULL OR fs.status = :#{#request.status})
                ORDER BY fs.status DESC, fs.shift ASC
            """, countQuery = """
                SELECT
                    COUNT(DISTINCT fs.id)
                FROM facility_shift fs
                JOIN facility f ON fs.id_facility = f.id
                WHERE
                    f.id = :#{#request.idFacility} AND
                    (:#{#request.shift} IS NULL OR fs.shift = :#{#request.shift}) AND
                    (:#{#request.status} IS NULL OR fs.status = :#{#request.status})
            """, nativeQuery = true)
    Page<AFFacilityShiftResponse> getAllByFilter(Pageable pageable, AFFilterFacilityShiftRequest request);

    @Query(value = """
                SELECT
                    CASE WHEN COUNT(*) > 0 THEN 'TRUE' ELSE 'FALSE' END
                FROM facility_shift
                WHERE
                    status = 1 AND
                    shift = :shift AND
                    id_facility = :idFacility AND
                    (:idFacilityShift IS NULL OR id != :idFacilityShift)
            """, nativeQuery = true)
    boolean isExistsShift(int shift, String idFacility, String idFacilityShift);

    @Query(value = """
                SELECT
                    CASE WHEN COUNT(*) > 0 THEN 'TRUE' ELSE 'FALSE' END
                FROM facility_shift
                WHERE
                    status = 1 AND
                    id_facility = :idFacility AND
                    NOT (
                        (:to_hour * 60 + :to_minute) <= (from_hour * 60 + from_minute)
                        OR
                        (:from_hour * 60 + :from_minute) >= (to_hour * 60 + to_minute)
                    ) AND
                    (:idFacilityShift IS NULL OR id != :idFacilityShift)
            """, nativeQuery = true)
    boolean isExistsTime(String idFacility, int from_hour, int from_minute, int to_hour, int to_minute,
            String idFacilityShift);
    @Query(value = """
                SELECT
                    CASE WHEN COUNT(*) > 0 THEN 'TRUE' ELSE 'FALSE' END
                FROM facility_shift
                WHERE
                    status = 1 AND
                    id_facility = :idFacility AND
                    from_hour = :from_hour AND
                    from_minute = :from_minute AND
                    to_hour = :to_hour AND
                    to_minute = :to_minute AND
                    (:idFacilityShift IS NULL OR id != :idFacilityShift)
            """, nativeQuery = true)
    boolean isExistsTimeV2(String idFacility, int from_hour, int from_minute, int to_hour, int to_minute,
                         String idFacilityShift);
}
