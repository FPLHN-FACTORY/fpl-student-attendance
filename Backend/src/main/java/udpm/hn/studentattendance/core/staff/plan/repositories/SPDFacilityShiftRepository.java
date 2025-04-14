package udpm.hn.studentattendance.core.staff.plan.repositories;

import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.FacilityShift;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.FacilityShiftRepository;

import java.util.List;
import java.util.Optional;


@Repository
public interface SPDFacilityShiftRepository extends FacilityShiftRepository {

    List<FacilityShift> findAllByFacility_IdAndStatusOrderByShiftAsc(String idFacility, EntityStatus status);

    Optional<FacilityShift> findByShiftAndFacility_Id(int shift, String idFacility);

}
