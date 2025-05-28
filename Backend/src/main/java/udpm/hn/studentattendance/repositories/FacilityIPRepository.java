package udpm.hn.studentattendance.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.FacilityIP;
import udpm.hn.studentattendance.entities.ImportLog;

@Repository
public interface FacilityIPRepository extends JpaRepository<FacilityIP, String> {

}
