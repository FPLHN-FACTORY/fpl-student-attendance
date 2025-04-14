package udpm.hn.studentattendance.core.student.attendance.repositories;

import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.FacilityLocation;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.FacilityLocationRepository;

import java.util.List;

@Repository
public interface SAFacilityLocationRepository extends FacilityLocationRepository {

    List<FacilityLocation> findAllByFacility_IdAndStatus(String idFacility, EntityStatus status);

}
