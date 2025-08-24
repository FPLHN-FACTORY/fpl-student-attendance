package udpm.hn.studentattendance.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.FacilityIP;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacilityIPRepository extends JpaRepository<FacilityIP, String> {

    List<FacilityIP> findByFacility(Facility facility);

    Optional<FacilityIP> findByIp(String ip);

    Optional<FacilityIP> findByFacilityAndIp(Facility facility, String ip);

    List<FacilityIP> findByStatus(EntityStatus status);
}
