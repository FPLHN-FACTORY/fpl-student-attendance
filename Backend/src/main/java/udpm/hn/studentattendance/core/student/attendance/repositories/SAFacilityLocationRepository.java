package udpm.hn.studentattendance.core.student.attendance.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.FacilityLocation;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.FacilityLocationRepository;

import java.util.List;
import java.util.Set;

@Repository
public interface SAFacilityLocationRepository extends FacilityLocationRepository {

    @Query(value = """
        SELECT 
            fl.*
        FROM facility_location fl
        JOIN facility f ON fl.id_facility = f.id
        WHERE
            f.status = 1 AND
            fl.status = 1 AND
            f.id = :idFacility
    """, nativeQuery = true)
    List<FacilityLocation> getAllList(String idFacility);

}
