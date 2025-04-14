package udpm.hn.studentattendance.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.FacilityLocation;

@Repository
public interface FacilityLocationRepository extends JpaRepository<FacilityLocation, String> {
}
