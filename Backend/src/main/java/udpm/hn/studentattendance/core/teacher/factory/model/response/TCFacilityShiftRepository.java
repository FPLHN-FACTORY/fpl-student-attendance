package udpm.hn.studentattendance.core.teacher.factory.model.response;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.FacilityShift;
import udpm.hn.studentattendance.repositories.FacilityShiftRepository;

import java.util.List;

@Repository
public interface TCFacilityShiftRepository extends FacilityShiftRepository {

    @Query(value = """
                SELECT fs.*
                FROM facility_shift fs
                JOIN facility f ON fs.id_facility = f.id
                WHERE
                    f.status = 1 AND
                    fs.status = 1 AND
                    f.id = :idFacility
                ORDER BY fs.shift ASC
            """, nativeQuery = true)
    List<FacilityShift> getAllList(String idFacility);

}
