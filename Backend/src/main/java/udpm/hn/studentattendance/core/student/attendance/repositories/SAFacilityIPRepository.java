package udpm.hn.studentattendance.core.student.attendance.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.repositories.FacilityIPRepository;

import java.util.Set;

@Repository
public interface SAFacilityIPRepository extends FacilityIPRepository {

    @Query(value = """
        SELECT 
            fi.ip
        FROM facility_ip fi
        JOIN facility f ON fi.id_facility = f.id
        WHERE
            f.status = 1 AND
            fi.status = 1 AND
            f.id = :idFacility
    """, nativeQuery = true)
    Set<String> getAllIP(String idFacility);

}
