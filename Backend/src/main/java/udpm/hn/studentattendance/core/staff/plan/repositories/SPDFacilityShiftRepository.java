package udpm.hn.studentattendance.core.staff.plan.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.FacilityShift;
import udpm.hn.studentattendance.repositories.FacilityShiftRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SPDFacilityShiftRepository extends FacilityShiftRepository {

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

    @Query(value = """
                SELECT fs.shift
                FROM facility_shift fs
                JOIN facility f ON fs.id_facility = f.id
                WHERE
                    f.status = 1 AND
                    fs.status = 1 AND
                    f.id = :idFacility
                ORDER BY fs.shift ASC
            """, nativeQuery = true)
    List<Integer> getAllShift(String idFacility);

    @Query(value = """
                SELECT fs.*
                FROM facility_shift fs
                JOIN facility f ON fs.id_facility = f.id
                WHERE
                    f.status = 1 AND
                    fs.status = 1 AND
                    f.id = :idFacility AND
                    fs.shift = :shift
                LIMIT 1
            """, nativeQuery = true)
    Optional<FacilityShift> getOneById(int shift, String idFacility);

}
