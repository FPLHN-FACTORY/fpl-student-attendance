package udpm.hn.studentattendance.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.FacilityShift;

@Repository
public interface FacilityShiftRepository extends JpaRepository<FacilityShift, String> {
}
