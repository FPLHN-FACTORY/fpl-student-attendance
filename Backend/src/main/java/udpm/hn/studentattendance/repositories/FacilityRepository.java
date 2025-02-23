package udpm.hn.studentattendance.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.Facility;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, String> {

}
